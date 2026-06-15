package com.clickkart.service.impl;

import com.clickkart.entity.CartItem;
import com.clickkart.entity.Order;
import com.clickkart.entity.OrderItem;
import com.clickkart.entity.User;
import com.clickkart.exception.OrderCancellationException;
import com.clickkart.exception.ResourceNotFoundException;
import com.clickkart.exception.StockUnavailableException;
import com.clickkart.repository.CartItemRepository;
import com.clickkart.repository.CartRepository;
import com.clickkart.repository.OrderItemRepository;
import com.clickkart.repository.OrderRepository;
import com.clickkart.repository.ProductRepository;
import com.clickkart.repository.UserRepository;
import com.clickkart.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link OrderService}.
 *
 * The placeOrder() method is the most critical operation in ClickKart.
 * It is fully atomic — if any step fails, the entire transaction rolls back:
 *   - No order row is persisted.
 *   - No stock is decremented.
 *   - The cart remains intact for the user to retry.
 *
 * Stock decrement strategy:
 *   ProductRepository.decrementStock() uses an atomic SQL UPDATE:
 *     UPDATE products SET stock = stock - qty WHERE id = ? AND stock >= qty
 *   If stock < qty, 0 rows are updated → StockUnavailableException is thrown.
 *   This handles concurrent checkouts correctly without application-level locking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository     orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository      cartRepository;
    private final CartItemRepository  cartItemRepository;
    private final ProductRepository   productRepository;
    private final UserRepository      userRepository;

    // =========================================================================
    // Checkout — the most critical method in the service layer
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Full atomic checkout flow:
     *   1.  Validate user exists.
     *   2.  Validate cart exists and is not empty.
     *   3.  Load all cart items with their products in one JOIN FETCH query.
     *   4.  For each cart item:
     *         a. Atomically decrement stock (SQL WHERE stock >= qty).
     *         b. If decrement returns 0 rows → throw StockUnavailableException.
     *         c. Snapshot the current price into priceAtPurchase.
     *         d. Accumulate the line total into the order total.
     *   5.  Build and persist Order + all OrderItems (cascade).
     *   6.  Clear cart items (JPQL bulk DELETE).
     *   7.  Return the saved Order.
     *
     * If any step throws, @Transactional rolls back the entire operation.
     * Stock decrements are reversed by the DB rollback — no manual restoration needed.
     */
    @Override
    @Transactional
    public Order placeOrder(Long userId, String address, String pincode) {
        // --- Validate inputs ---
        if (!StringUtils.hasText(address)) {
            throw new IllegalArgumentException("Delivery address is required.");
        }
        if (!StringUtils.hasText(pincode)) {
            throw new IllegalArgumentException("Delivery pincode is required.");
        }

        // --- Step 1: Load user ---
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // --- Step 2: Load cart ---
        var cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active cart found for user: " + userId));

        // --- Step 3: Load cart items with products (JOIN FETCH — single SQL query) ---
        List<CartItem> cartItems = cartItemRepository.findByCartIdWithProduct(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot place an order: your cart is empty.");
        }

        // --- Steps 4a–4d: Decrement stock, snapshot price, accumulate total ---
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount    = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            var product  = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            /*
             * Atomic stock decrement:
             *   UPDATE products SET stock = stock - qty
             *   WHERE id = ? AND stock >= qty
             *
             * Returns number of rows updated (0 = insufficient stock).
             * Under concurrent load, two requests cannot both succeed if only
             * qty units remain — the DB WHERE clause enforces this atomically.
             */
            int rowsUpdated = productRepository.decrementStock(product.getId(), quantity);
            if (rowsUpdated == 0) {
                throw new StockUnavailableException(product.getName(), quantity);
            }

            // Snapshot the price at the exact moment of checkout
            BigDecimal priceAtPurchase = product.getPrice();
            BigDecimal lineTotal       = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
            totalAmount                = totalAmount.add(lineTotal);

            orderItems.add(OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .priceAtPurchase(priceAtPurchase)
                    .build());
        }

        // --- Step 5: Build and persist Order with all OrderItems ---
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .address(address.trim())
                .pincode(pincode.trim())
                .status(Order.OrderStatus.PLACED)
                .build();

        // Set bidirectional relationship: OrderItem.order = order (drives the FK)
        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order); // cascade persists all OrderItems

        log.info("Order #{} placed for userId={}, total={}, items={}",
                savedOrder.getId(), userId, totalAmount, orderItems.size());

        // --- Step 6: Clear the cart (bulk JPQL DELETE — does not delete cart header) ---
        cartItemRepository.deleteAllByCartId(cart.getId());
        log.debug("Cart cleared after checkout: cartId={}", cart.getId());

        return savedOrder;
    }

    // =========================================================================
    // Order retrieval
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * The user_id scope prevents horizontal privilege escalation:
     * a user cannot retrieve another user's order by guessing the order ID.
     * Uses JOIN FETCH to load all OrderItems and Products in one SQL query.
     */
    @Override
    public Order getOrderById(Long userId, Long orderId) {
        log.debug("Fetching order #{} for userId={}", orderId, userId);
        return orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Order #%d not found for user %d", orderId, userId)));
    }

    /**
     * {@inheritDoc}
     *
     * Returns the order list without eager-loading items (header-only).
     * The "My Orders" list page only shows summary data (total, status, date).
     * Order detail (with items) is loaded via getOrderById() on demand.
     */
    @Override
    public List<Order> getOrderHistory(Long userId) {
        log.debug("Fetching order history for userId={}", userId);
        return orderRepository.findByUserIdOrderByOrderedAtDesc(userId);
    }

    // =========================================================================
    // Cancel order
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Cancellation is only allowed when status is PLACED or CONFIRMED.
     * Cancelled orders have their stock restored for each line item.
     *
     * Note: If a product has since been deleted (unlikely but possible),
     * incrementStock() is a no-op because the WHERE id = ? clause won't match.
     */
    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Order #%d not found for user %d", orderId, userId)));

        // Validate that the order can be cancelled
        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
            order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new OrderCancellationException(orderId, order.getStatus().name());
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new OrderCancellationException(
                    String.format("Order #%d is already cancelled.", orderId));
        }

        // Restore stock for each line item
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        items.forEach(item ->
                productRepository.incrementStock(item.getProduct().getId(), item.getQuantity())
        );

        // Update status via targeted JPQL UPDATE
        int updated = orderRepository.updateStatus(orderId, Order.OrderStatus.CANCELLED);
        if (updated == 0) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }

        log.info("Order #{} cancelled for userId={}, {} item(s) stock restored",
                orderId, userId, items.size());
    }

    // =========================================================================
    // Admin operations
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Admin dashboard: all orders across all users, newest first.
     */
    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        log.debug("Admin: fetching all orders, page={}", pageable.getPageNumber());
        return orderRepository.findAllByOrderByOrderedAtDesc(pageable);
    }

    /**
     * {@inheritDoc}
     *
     * Admin: advance order through lifecycle (PLACED → CONFIRMED → SHIPPED → DELIVERED).
     * A targeted JPQL UPDATE is used to avoid loading the entire Order entity.
     */
    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        int updated = orderRepository.updateStatus(orderId, newStatus);
        if (updated == 0) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }

        // Reload the updated order to return the fresh state
        Order refreshed = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        log.info("Admin: order #{} status updated to {}", orderId, newStatus);
        return refreshed;
    }

    /**
     * {@inheritDoc}
     *
     * Admin: filter orders by status (e.g., show all PLACED orders on the dashboard).
     */
    @Override
    public Page<Order> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        log.debug("Admin: fetching orders with status={}, page={}", status, pageable.getPageNumber());
        return orderRepository.findByStatusOrderByOrderedAtDesc(status, pageable);
    }
}
