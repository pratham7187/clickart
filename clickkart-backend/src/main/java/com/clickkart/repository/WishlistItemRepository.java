package com.clickkart.repository;

import com.clickkart.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link WishlistItem} entity (table: wishlist_items).
 *
 * The wishlist is a classic M:N join table between users and products.
 * A user can save any product once (UNIQUE(user_id, product_id)).
 * There is no quantity — it is a bookmark, not an order.
 *
 * Primary consumers:
 *   - WishlistService → addToWishlist, removeFromWishlist, getWishlist
 *   - CartService     → moveToCart (move wishlist item → cart item)
 */
@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    // =========================================================================
    // Load wishlist for a user
    // =========================================================================

    /**
     * Load all wishlist items for a user, with product details pre-fetched.
     * JOIN FETCH avoids N+1 queries when rendering the wishlist page —
     * each item's name, price, and image are needed immediately.
     *
     * JPQL path: wishlistItem.user.id
     */
    @Query("SELECT wi FROM WishlistItem wi JOIN FETCH wi.product WHERE wi.user.id = :userId")
    List<WishlistItem> findByUserIdWithProduct(@Param("userId") Long userId);

    /**
     * Simple list by userId without JOIN FETCH.
     * Used in cases where only the IDs are needed (e.g., building a
     * "is wishlisted?" check set for a product grid).
     */
    List<WishlistItem> findByUserId(Long userId);

    /**
     * Load wishlist by user email.
     * Useful when the service layer has the email from the JWT principal
     * and hasn't loaded the User entity yet.
     */
    List<WishlistItem> findByUserEmail(String email);

    // =========================================================================
    // Find specific item (add/remove idempotency checks)
    // =========================================================================

    /**
     * Find a specific wishlist entry for a user/product combination.
     * Used by WishlistService.addToWishlist() to check existence before INSERT,
     * giving a clear "already in wishlist" response rather than a DB exception
     * from the UNIQUE constraint.
     */
    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * True if the user has already wishlisted this product.
     * Used to render the filled/unfilled heart icon on the product card.
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // =========================================================================
    // Remove operations
    // =========================================================================

    /**
     * Remove a product from a user's wishlist.
     * Called by WishlistService.removeFromWishlist() and
     * CartService.moveToCart() (after adding to cart, remove from wishlist).
     *
     * @Modifying requires @Transactional on the calling service method.
     */
    @Modifying
    @Query("DELETE FROM WishlistItem wi WHERE wi.user.id = :userId AND wi.product.id = :productId")
    void deleteByUserIdAndProductId(
            @Param("userId")    Long userId,
            @Param("productId") Long productId
    );

    /**
     * Remove all wishlist entries for a user (used when an account is purged).
     */
    @Modifying
    @Query("DELETE FROM WishlistItem wi WHERE wi.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    // =========================================================================
    // Count
    // =========================================================================

    /** Number of items in a user's wishlist (for wishlist badge). */
    int countByUserId(Long userId);
}
