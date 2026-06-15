package com.clickkart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mapped to DB table: order_items
 *
 * One row per product line within a multi-product order.
 * This entity is what makes ClickKart a real e-commerce system —
 * a single Order can reference N OrderItems.
 *
 * CRITICAL: price_at_purchase is an IMMUTABLE price snapshot captured at
 * checkout time. It must NEVER be updated after the row is inserted.
 * If an admin changes a product's price tomorrow, all historical order_items
 * still show exactly what the customer was charged. This is a legal and
 * accounting requirement (GST invoice integrity).
 *
 * DB constraints:
 *   CHECK (quantity >= 1)           — mirrored by @Min(1)
 *   CHECK (price_at_purchase > 0)  — mirrored by @DecimalMin("0.01")
 *   FK order_id   ON DELETE CASCADE  — delete order → line items deleted
 *   FK product_id ON DELETE RESTRICT — cannot delete product on a real order
 *
 * Relationships:
 *   order_items -> orders   (ManyToOne — FK: order_id)
 *   order_items -> products (ManyToOne — FK: product_id)
 */
@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order_id",   columnList = "order_id"),
                @Index(name = "idx_order_items_product_id", columnList = "product_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

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
     * Number of units of this product purchased in this order.
     * DB CHECK (quantity >= 1) mirrored here.
     */
    @NotNull
    @Min(value = 1, message = "Order item quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * THE MOST IMPORTANT COLUMN.
     *
     * The unit price of this product AT THE MOMENT OF CHECKOUT.
     * This is a snapshot — it is set once when the OrderItem is created
     * and must never be changed.
     *
     * Service layer responsibility:
     *   OrderItem.priceAtPurchase = cartItem.getProduct().getPrice()
     *   (copied from the live price at checkout time)
     *
     * Do not join to products.price for display — always use this field.
     */
    @NotNull
    @DecimalMin(value = "0.01", message = "Purchase price must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * ManyToOne: many order items belong to one order.
     * LAZY — parent order loaded only when explicitly needed.
     * ON DELETE CASCADE in DB: deleting an order removes all its line items.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_order")
    )
    private Order order;

    /**
     * ManyToOne: many order items reference one product.
     * LAZY — product details loaded when rendering the order detail / receipt.
     * ON DELETE RESTRICT in DB: cannot delete a product that is on a real order.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_product")
    )
    private Product product;

    // -------------------------------------------------------------------------
    // Derived helper (NOT persisted — convenience for receipt rendering)
    // -------------------------------------------------------------------------

    /**
     * Line total: quantity × priceAtPurchase.
     * Calculated in-memory for display purposes.
     * NOT a DB column — use for DTO mapping only.
     */
    @Transient
    public BigDecimal getLineTotal() {
        if (priceAtPurchase == null || quantity == null) return BigDecimal.ZERO;
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}
