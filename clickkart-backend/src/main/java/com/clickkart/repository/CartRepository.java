package com.clickkart.repository;

import com.clickkart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for the {@link Cart} entity (table: cart).
 *
 * A cart is a singleton per user — the DB enforces this with UNIQUE KEY
 * uq_cart_user_id on the user_id column. Every method here either finds
 * or confirms the existence of that single cart row.
 *
 * Primary consumers:
 *   - CartService → getOrCreateCart(), addItem(), removeItem(), clear()
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // =========================================================================
    // Core cart lookup — most-called method in CartService
    // =========================================================================

    /**
     * Load the cart for a given user ID.
     *
     * JPQL path: cart.user.id — Hibernate generates:
     *   SELECT c.* FROM cart c WHERE c.user_id = ?
     *
     * Used by CartService.getOrCreateCart(userId):
     *   - If present → return it
     *   - If empty   → create a new Cart, save, and return
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Load the cart by the user's email address.
     * Useful when the service has the authenticated principal's email
     * but has not yet loaded the User entity.
     *
     * JPQL path: cart.user.email — Hibernate generates a JOIN to users.
     */
    Optional<Cart> findByUserEmail(String email);

    // =========================================================================
    // Existence check
    // =========================================================================

    /**
     * Check whether a cart already exists for a user.
     * Used by CartService before calling save() to avoid duplicate inserts
     * (the UNIQUE constraint would reject them, but this gives a cleaner error).
     */
    boolean existsByUserId(Long userId);

    // =========================================================================
    // Cart summary (for header badge — item count)
    // =========================================================================

    /**
     * Count the number of distinct product lines in a user's cart.
     * Used to display the cart item count badge in the navigation header.
     *
     * JPQL: counts cart_items rows, not total units.
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.user.id = :userId")
    long countItemsByUserId(@Param("userId") Long userId);
}
