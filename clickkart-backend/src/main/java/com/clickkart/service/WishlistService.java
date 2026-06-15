package com.clickkart.service;

import com.clickkart.entity.WishlistItem;

import java.util.List;

/**
 * Service contract for wishlist (save for later) operations.
 *
 * The wishlist is a simple M:N bookmark between users and products.
 * No quantity is stored. The UNIQUE (user_id, product_id) constraint
 * prevents a product being added more than once.
 */
public interface WishlistService {

    /**
     * Returns all products in a user's wishlist, with product details pre-fetched.
     * JOIN FETCH is used to avoid N+1 queries on the wishlist page.
     *
     * @param userId the authenticated user's ID
     * @return list of WishlistItem entities with product loaded
     */
    List<WishlistItem> getWishlist(Long userId);

    /**
     * Adds a product to the user's wishlist.
     * Idempotent: if the product is already wishlisted, throws
     * {@link com.clickkart.exception.DuplicateResourceException} to give a clear message.
     *
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if product not found or not active.
     *
     * @param userId    the authenticated user's ID
     * @param productId the product to bookmark
     * @return the newly created WishlistItem
     */
    WishlistItem addToWishlist(Long userId, Long productId);

    /**
     * Removes a product from the user's wishlist.
     * Idempotent: no-op if the product is not in the wishlist.
     *
     * @param userId    the authenticated user's ID
     * @param productId the product to remove
     */
    void removeFromWishlist(Long userId, Long productId);

    /**
     * Returns true if the user has bookmarked a specific product.
     * Used to render the filled/unfilled heart icon on product cards.
     *
     * @param userId    the authenticated user's ID
     * @param productId the product to check
     * @return true if the product is in the user's wishlist
     */
    boolean isProductWishlisted(Long userId, Long productId);

    /**
     * Moves a product from the wishlist into the cart in a single atomic operation.
     * After the item is successfully added to the cart, it is removed from the wishlist.
     * If addToCart fails (e.g., insufficient stock), the wishlist entry is NOT removed.
     *
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if the wishlist item does not exist.
     * Throws {@link com.clickkart.exception.StockUnavailableException} if stock is insufficient.
     *
     * @param userId    the authenticated user's ID
     * @param productId the product to move
     * @param quantity  the quantity to add to the cart
     */
    void moveToCart(Long userId, Long productId, Integer quantity);
}
