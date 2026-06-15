package com.clickkart.repository;

import com.clickkart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link CartItem} entity (table: cart_items).
 *
 * CartItem is the most frequently written table during shopping — every
 * add/update/remove operation goes through this repository.
 *
 * Key DB constraint enforced here:
 *   UNIQUE (cart_id, product_id) → the same product appears only once per cart.
 *   The service layer must UPDATE quantity rather than INSERT a duplicate row.
 *
 * Primary consumers:
 *   - CartService → addItem, updateQuantity, removeItem, clearCart, getItems
 *   - OrderService → read items at checkout for price snapshot
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // =========================================================================
    // Load items for a cart
    // =========================================================================

    /**
     * Load all items in a cart.
     * JPQL path: cartItem.cart.id
     *
     * Called on every cart page load and at checkout.
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Load all items in a cart with product details pre-fetched (JOIN FETCH).
     * Avoids N+1 queries when rendering the cart page — each item's product
     * name, price, and image_url are needed immediately.
     */
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartIdWithProduct(@Param("cartId") Long cartId);

    // =========================================================================
    // Find specific item by cart + product (for add/update logic)
    // =========================================================================

    /**
     * Check whether a specific product is already in a cart.
     *
     * CartService.addItem() calls this first:
     *   - If present → increment quantity (UPDATE)
     *   - If absent  → insert new CartItem (INSERT)
     *
     * JPQL: cartItem.cart.id AND cartItem.product.id
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * True if the product is already in the cart.
     * Used before attempting an INSERT to give a cleaner error than a
     * DataIntegrityViolationException from the UNIQUE constraint.
     */
    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    // =========================================================================
    // Remove operations
    // =========================================================================

    /**
     * Remove a specific product from a cart.
     * Used by CartService.removeItem(cartId, productId).
     *
     * JPQL DELETE — @Modifying requires @Transactional on the calling service method.
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    void deleteByCartIdAndProductId(
            @Param("cartId")    Long cartId,
            @Param("productId") Long productId
    );

    /**
     * Clear all items from a cart.
     * Called by OrderService.checkout() after the order is persisted successfully.
     *
     * The DB ON DELETE CASCADE on cart_items.cart_id means deleting the cart row
     * also cascades to items — but this JPQL delete is used when only clearing
     * items without deleting the cart header itself.
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);

    // =========================================================================
    // Count / stats
    // =========================================================================

    /**
     * Count of distinct product lines in the cart.
     * Used for the cart item badge in the navigation header.
     */
    int countByCartId(Long cartId);

    /**
     * Sum of all quantities in the cart.
     * Used for displaying "X items in cart" (total units, not distinct lines).
     * Returns null if cart is empty — caller should handle with coalesce/default.
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    int sumQuantityByCartId(@Param("cartId") Long cartId);
}
