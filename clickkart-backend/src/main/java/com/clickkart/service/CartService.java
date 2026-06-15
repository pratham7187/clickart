package com.clickkart.service;

import com.clickkart.entity.Cart;
import com.clickkart.entity.CartItem;

import java.util.List;

/**
 * Service contract for shopping cart operations.
 *
 * Cart design rules (enforced here and in DB):
 *   - One active cart per user (UNIQUE uq_cart_user_id).
 *   - Cart is created lazily on the first add-to-cart action.
 *   - Price is NOT stored in the cart — it always reflects current product price.
 *   - Price is snapshotted into order_items at checkout time by OrderService.
 *   - The same product appears only once per cart (UNIQUE cart_id + product_id).
 *     Adding an already-present product increments quantity instead of inserting.
 *   - Cart is cleared (all CartItems deleted) after a successful checkout.
 */
public interface CartService {

    /**
     * Returns the cart for a user, creating a new empty cart if one does not exist yet.
     * This implements the get-or-create lazy pattern.
     *
     * @param userId the authenticated user's ID
     * @return the existing or newly created cart
     */
    Cart getCart(Long userId);

    /**
     * Returns all cart item lines for a user's cart, with product details pre-fetched.
     * Uses a JOIN FETCH query to avoid N+1 queries when rendering the cart page.
     *
     * @param userId the authenticated user's ID
     * @return list of CartItem entities with product loaded
     */
    List<CartItem> getCartItems(Long userId);

    /**
     * Adds a product to the user's cart.
     *
     * Business rules:
     *   - Product must be active (is_active = true).
     *   - Requested quantity must not exceed available stock.
     *   - If the product is already in the cart, the quantity is incremented
     *     (existing + requested). The DB UNIQUE constraint prevents duplicates.
     *   - If the product is not yet in the cart, a new CartItem row is inserted.
     *
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if product not found.
     * Throws {@link com.clickkart.exception.StockUnavailableException} if stock is insufficient.
     *
     * @param userId    the authenticated user's ID
     * @param productId the product to add
     * @param quantity  number of units to add (minimum 1)
     * @return the created or updated CartItem
     */
    CartItem addToCart(Long userId, Long productId, Integer quantity);

    /**
     * Sets the quantity of a specific product line in the cart to an absolute value.
     * If the new quantity is 0 or less, the item is removed instead.
     *
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if the item does not exist.
     * Throws {@link com.clickkart.exception.StockUnavailableException} if new quantity exceeds stock.
     *
     * @param userId      the authenticated user's ID
     * @param productId   the product whose quantity to change
     * @param newQuantity the desired quantity (≥ 1); pass 0 to remove the item
     * @return the updated CartItem, or null if the item was removed
     */
    CartItem updateCartItemQuantity(Long userId, Long productId, Integer newQuantity);

    /**
     * Removes a specific product line from the user's cart.
     * No-op if the product is not present (idempotent).
     *
     * @param userId    the authenticated user's ID
     * @param productId the product to remove
     */
    void removeFromCart(Long userId, Long productId);

    /**
     * Deletes all items from the user's cart (but keeps the cart header row).
     * Called by OrderService after a successful checkout.
     * Also exposed to allow the user to manually empty their cart.
     *
     * @param userId the authenticated user's ID
     */
    void clearCart(Long userId);

    /**
     * Returns the number of distinct product lines in the user's cart.
     * Used for the cart item count badge in the navigation header.
     *
     * @param userId the authenticated user's ID
     * @return number of distinct products in the cart (0 if no cart exists)
     */
    int getCartItemCount(Long userId);
}
