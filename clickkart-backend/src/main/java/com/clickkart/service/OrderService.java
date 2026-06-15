package com.clickkart.service;

import com.clickkart.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service contract for order placement and management.
 *
 * Checkout flow (placeOrder):
 *   1. Load user and their cart items.
 *   2. Validate cart is not empty.
 *   3. For each cart item, atomically decrement product stock.
 *   4. Snapshot prices into OrderItem.priceAtPurchase.
 *   5. Persist Order + OrderItems in one transaction.
 *   6. Clear the cart.
 *
 * Cancellation rules:
 *   - PLACED and CONFIRMED orders can be cancelled.
 *   - SHIPPED and DELIVERED orders cannot be cancelled.
 *   - Cancellation restores stock for each item.
 */
public interface OrderService {

    /**
     * Processes checkout: validates stock, snapshots prices, persists the order,
     * decrements stock atomically, and clears the cart — all in one transaction.
     *
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if user or cart not found.
     * Throws {@link IllegalStateException} if the cart is empty.
     * Throws {@link com.clickkart.exception.StockUnavailableException} if any product's stock
     * is less than the requested quantity. The entire transaction rolls back.
     *
     * @param userId  the authenticated user placing the order
     * @param address the full delivery address (stored verbatim as a snapshot)
     * @param pincode the delivery pincode
     * @return the persisted Order entity with all OrderItems attached
     */
    Order placeOrder(Long userId, String address, String pincode);

    /**
     * Returns an order by its ID, scoped to the authenticated user.
     * Prevents horizontal privilege escalation — a user cannot retrieve
     * another user's order by guessing the order ID.
     *
     * Includes all OrderItems and their Products (JOIN FETCH — no N+1).
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if not found.
     *
     * @param userId  the authenticated user's ID
     * @param orderId the order ID
     * @return the order with items and products loaded
     */
    Order getOrderById(Long userId, Long orderId);

    /**
     * Returns all orders for a user, sorted newest first.
     * Used for the "My Orders" page.
     *
     * @param userId the authenticated user's ID
     * @return list of orders (items NOT eagerly loaded — use getOrderById for detail)
     */
    List<Order> getOrderHistory(Long userId);

    /**
     * Cancels an order.
     * Only PLACED or CONFIRMED orders can be cancelled.
     * Cancellation restores stock for every item in the order.
     *
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if order not found for user.
     * Throws {@link com.clickkart.exception.OrderCancellationException} if the order status
     * is SHIPPED or DELIVERED.
     *
     * @param userId  the authenticated user's ID
     * @param orderId the order to cancel
     */
    void cancelOrder(Long userId, Long orderId);

    /**
     * Returns all orders across all users (admin only), newest first. Paginated.
     *
     * @param pageable pagination and sorting parameters
     * @return page of all orders
     */
    Page<Order> getAllOrders(Pageable pageable);

    /**
     * Updates the lifecycle status of an order (admin only).
     * Used to advance: PLACED → CONFIRMED → SHIPPED → DELIVERED
     *
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if order not found.
     *
     * @param orderId   the order to update
     * @param newStatus the desired new status
     * @return the updated order
     */
    Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus);

    /**
     * Returns all orders with a specific status (admin only).
     *
     * @param status   the status to filter by
     * @param pageable pagination parameters
     * @return page of orders with that status
     */
    Page<Order> getOrdersByStatus(Order.OrderStatus status, Pageable pageable);
}
