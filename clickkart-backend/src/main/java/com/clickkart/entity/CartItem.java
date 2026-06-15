package com.clickkart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Mapped to DB table: cart_items
 *
 * One row per product line inside a cart.
 * Price is deliberately NOT stored here — the cart always shows the current
 * product price. Price is only snapshotted into order_items.price_at_purchase
 * at the moment of checkout.
 *
 * DB constraints:
 *   CHECK (quantity >= 1)                — mirrored by @Min(1) on quantity
 *   UNIQUE (cart_id, product_id)        — prevents duplicate product rows in cart
 *   FK cart_id  ON DELETE CASCADE       — checkout clears cart → items auto-deleted
 *   FK product_id ON DELETE CASCADE     — deleted product removed from all carts
 *
 * Relationships:
 *   cart_items -> cart     (ManyToOne — FK: cart_id)
 *   cart_items -> products (ManyToOne — FK: product_id)
 */
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_cart_items_cart_product",
                columnNames = {"cart_id", "product_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

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

    /**
     * Number of units of this product in the cart.
     * DB CHECK (quantity >= 1) mirrored here.
     * Setting quantity to 0 is not valid — remove the CartItem instead.
     */
    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

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
     * ManyToOne: many cart items belong to one cart.
     * LAZY — parent cart is rarely needed when querying items alone.
     * ON DELETE CASCADE in DB: when the cart is cleared, all items go with it.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cart_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_items_cart")
    )
    private Cart cart;

    /**
     * ManyToOne: many cart items reference one product.
     * LAZY — product details loaded only when rendering cart line items.
     * ON DELETE CASCADE in DB: deleting a product removes it from all carts.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_items_product")
    )
    private Product product;
}
