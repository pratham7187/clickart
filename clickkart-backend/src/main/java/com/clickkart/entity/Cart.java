package com.clickkart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapped to DB table: cart
 *
 * Cart header. One row per user — enforced by UNIQUE KEY uq_cart_user_id.
 * Created lazily by CartService on the user's first "Add to Cart" action.
 * Cleared (all CartItems deleted via CASCADE) after a successful checkout.
 *
 * Relationships:
 *   cart -> users       (ManyToOne — FK: user_id)
 *   cart -> cart_items  (OneToMany, mappedBy = "cart", CASCADE = ALL)
 */
@Entity
@Table(
        name = "cart",
        uniqueConstraints = @UniqueConstraint(name = "uq_cart_user_id", columnNames = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

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
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Lifecycle Hooks
    // -------------------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * ManyToOne: each cart belongs to exactly one user.
     * The UNIQUE constraint on user_id in DB makes this effectively a 1:1.
     * LAZY fetch — user object loaded only when explicitly accessed.
     * ON DELETE CASCADE in DB: hard-deleting a user removes their cart.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_cart_user")
    )
    private User user;

    /**
     * OneToMany: one cart has many cart item lines.
     * cascade = ALL: saving/deleting the cart saves/deletes its items.
     * orphanRemoval = true: removing an item from the list deletes it from DB.
     * LAZY — item list is loaded only when the cart page is accessed.
     */
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<CartItem> items;
}
