package com.clickkart.service.impl;

import com.clickkart.entity.Cart;
import com.clickkart.entity.CartItem;
import com.clickkart.entity.Product;
import com.clickkart.entity.User;
import com.clickkart.exception.ResourceNotFoundException;
import com.clickkart.exception.StockUnavailableException;
import com.clickkart.repository.CartItemRepository;
import com.clickkart.repository.CartRepository;
import com.clickkart.repository.ProductRepository;
import com.clickkart.repository.UserRepository;
import com.clickkart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link CartService}.
 *
 * Invariants:
 *   - One cart per user (UNIQUE on cart.user_id).
 *   - Cart is created lazily on the first add-to-cart.
 *   - Same product appears only once per cart (UNIQUE cart_id+product_id).
 *   - Price is NOT stored in the cart. It is snapshotted at checkout by OrderService.
 *   - Stock is validated before adding to cart (advisory check — DB is the final guard).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository     cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository  productRepository;
    private final UserRepository     userRepository;

    // =========================================================================
    // Cart retrieval (get-or-create)
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * If no cart exists for the user, a new empty cart is created and saved.
     * This lazy-creation pattern means the cart table only has rows for users
     * who have interacted with the store.
     */
    @Override
    @Transactional
    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Cart newCart = Cart.builder().user(user).build();
                    Cart saved = cartRepository.save(newCart);
                    log.info("Created new cart (id={}) for userId={}", saved.getId(), userId);
                    return saved;
                });
    }

    /**
     * {@inheritDoc}
     *
     * Uses JOIN FETCH to avoid N+1 queries: loads each CartItem together with
     * its Product in a single SQL query. Handles the case where the user has
     * no cart yet by returning an empty list.
     */
    @Override
    public List<CartItem> getCartItems(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            log.debug("No cart found for userId={}, returning empty list", userId);
            return Collections.emptyList();
        }
        return cartItemRepository.findByCartIdWithProduct(cartOpt.get().getId());
    }

    // =========================================================================
    // Add item
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Logic:
     *   1. Validate product exists and is active.
     *   2. Check that stock >= quantity requested.
     *   3. Get or create the cart.
     *   4. If the product is already in the cart → increment quantity,
     *      but cap the total at available stock.
     *   5. If the product is not yet in the cart → create a new CartItem.
     */
    @Override
    @Transactional
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        // 1. Validate product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        // 2. Stock advisory check (DB CHECK constraint is the hard guard at checkout)
        if (product.getStock() < quantity) {
            throw new StockUnavailableException(product.getName(), quantity);
        }

        // 3. Get or create cart
        Cart cart = getCart(userId);

        // 4. Check if product already in cart
        Optional<CartItem> existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;

            // Cap at available stock
            if (newQty > product.getStock()) {
                throw new StockUnavailableException(
                        String.format("Cannot add %d unit(s) of '%s'. Total would exceed available stock (%d).",
                                quantity, product.getName(), product.getStock())
                );
            }

            item.setQuantity(newQty);
            CartItem saved = cartItemRepository.save(item);
            log.info("Updated cart item quantity: cartId={}, productId={}, newQty={}", cart.getId(), productId, newQty);
            return saved;
        }

        // 5. New line item
        CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
        CartItem saved = cartItemRepository.save(newItem);
        log.info("Added product {} to cart {} with qty {}", productId, cart.getId(), quantity);
        return saved;
    }

    // =========================================================================
    // Update quantity
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Delegates to removeFromCart() when newQuantity <= 0,
     * ensuring the UNIQUE constraint is never violated by a zero-qty row.
     */
    @Override
    @Transactional
    public CartItem updateCartItemQuantity(Long userId, Long productId, Integer newQuantity) {
        // Delegate removal when quantity is set to zero
        if (newQuantity == null || newQuantity <= 0) {
            removeFromCart(userId, productId);
            return null;
        }

        // Validate product and stock
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (newQuantity > product.getStock()) {
            throw new StockUnavailableException(product.getName(), newQuantity);
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product " + productId + " is not in the cart"));

        item.setQuantity(newQuantity);
        CartItem saved = cartItemRepository.save(item);
        log.info("Cart item quantity updated: cartId={}, productId={}, qty={}", cart.getId(), productId, newQuantity);
        return saved;
    }

    // =========================================================================
    // Remove item
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Uses a targeted JPQL DELETE rather than a findById + delete pattern.
     * Idempotent: no-op if the product is not in the cart.
     * Requires @Transactional because the underlying repository uses @Modifying.
     */
    @Override
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
            log.info("Removed product {} from cart {}", productId, cart.getId());
        });
    }

    // =========================================================================
    // Clear cart
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Deletes all CartItem rows for the cart using a bulk JPQL DELETE.
     * The cart header row (cart table) is preserved — the user's cart ID
     * stays consistent across sessions and does not need to be recreated.
     * Called by OrderService.placeOrder() after successful order persistence.
     */
    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.deleteAllByCartId(cart.getId());
            log.info("Cart cleared for userId={}, cartId={}", userId, cart.getId());
        });
    }

    // =========================================================================
    // Item count (badge)
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Returns 0 if the user has no cart rather than throwing an exception.
     */
    @Override
    public int getCartItemCount(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> cartItemRepository.countByCartId(cart.getId()))
                .orElse(0);
    }
}
