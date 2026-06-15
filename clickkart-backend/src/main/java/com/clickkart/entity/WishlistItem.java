package com.clickkart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Mapped to DB table: wishlist_items
 *
 * A product a user has bookmarked/saved for later.
 * No quantity field — a wishlist is a "save for later" list, not a cart.
 *
 * DB constraints:
 *   UNIQUE (user_id, product_id) — a user can wishlist a product only once
 *   FK user_id    ON DELETE CASCADE — user deleted → wishlist entries deleted
 *   FK product_id ON DELETE CASCADE — product deleted → removed from wishlists
 *
 * Business rule:
 *   CartService.moveToCart() moves a wishlist item into the cart and removes
 *   it from the wishlist in a single @Transactional call.
 *
 * Relationships:
 *   wishlist_items -> users    (ManyToOne — FK: user_id)
 *   wishlist_items -> products (ManyToOne — FK: product_id)
 */
@Entity
@Table(
        name = "wishlist_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_wishlist_user_product",
                columnNames = {"user_id", "product_id"}
        ),
        indexes = @Index(name = "idx_wishlist_user_id", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem {

    // -------------------------------------------------------------------------
    // Primary Key
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // -------------------------------------------------------------------------
    // Columns
    // -------------------------------------------------------------------------
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    // -------------------------------------------------------------------------
    // Lifecycle Hooks
    // -------------------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * ManyToOne: many wishlist items belong to one user.
     * LAZY fetch — user loaded only when explicitly needed.
     * ON DELETE CASCADE in DB: user deleted → their wishlist items deleted.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_wishlist_user")
    )
    private User user;

    /**
     * ManyToOne: many wishlist items reference one product.
     * LAZY fetch — product details loaded when rendering the wishlist page.
     * ON DELETE CASCADE in DB: product deleted → removed from all wishlists.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_wishlist_product")
    )
    private Product product;
}
