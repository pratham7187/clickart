package com.clickkart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapped to DB table: orders
 *
 * Order header. One row is created per checkout event.
 * The products purchased are stored in order_items (child table).
 *
 * CRITICAL RULE: total_amount is an immutable snapshot of the cart total
 * at checkout time. It must NEVER be recalculated from current product prices
 * after the order is placed. Historical order totals must not change when an
 * admin updates a product's price.
 *
 * ON DELETE RESTRICT on user_id: orders are legal records.
 * Never hard-delete a user who has placed orders. Use User.isActive = false.
 *
 * Status lifecycle:
 *   PLACED -> CONFIRMED -> SHIPPED -> DELIVERED
 *                       -> CANCELLED
 *
 * Relationships:
 *   orders -> users       (ManyToOne  — FK: user_id)
 *   orders -> order_items (OneToMany, mappedBy = "order", CASCADE = ALL)
 */
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user_id",    columnList = "user_id"),
                @Index(name = "idx_orders_status",     columnList = "status"),
                @Index(name = "idx_orders_ordered_at", columnList = "ordered_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

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
     * Immutable snapshot of the cart total at the moment of checkout.
     * Equals the sum of (order_items.price_at_purchase × quantity) for all items.
     * Never recalculate this from current product prices.
     */
    @NotNull
    @DecimalMin(value = "0.01", message = "Order total must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotBlank(message = "Delivery pincode is required")
    @Size(max = 10)
    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @NotBlank(message = "Delivery address is required")
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    /**
     * Order lifecycle status. Mapped as a String matching the MySQL ENUM.
     * Managed exclusively by the Admin API — customers cannot update status.
     * Default: PLACED (set automatically by OrderService.checkout()).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20,
            columnDefinition = "ENUM('PLACED','CONFIRMED','SHIPPED','DELIVERED','CANCELLED') DEFAULT 'PLACED'")
    @Builder.Default
    private OrderStatus status = OrderStatus.PLACED;

    @Column(name = "ordered_at", nullable = false, updatable = false)
    private LocalDateTime orderedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Lifecycle Hooks
    // -------------------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        orderedAt = LocalDateTime.now();
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
     * ManyToOne: many orders belong to one user.
     * LAZY — user is rarely needed when loading order data.
     * ON DELETE RESTRICT in DB: cannot hard-delete a user who has orders.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_user")
    )
    private User user;

    /**
     * OneToMany: one order has 1..N order item lines (multi-product order).
     * cascade = ALL: saving an order saves its items in one transaction.
     * orphanRemoval = true: removing an item from the list deletes it from DB.
     * LAZY — item list loaded only when order detail page is requested.
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<OrderItem> items;

    // -------------------------------------------------------------------------
    // Order Status Enum
    // -------------------------------------------------------------------------
    public enum OrderStatus {
        PLACED,     // Customer completed checkout; awaiting admin confirmation
        CONFIRMED,  // Admin confirmed; order being prepared for dispatch
        SHIPPED,    // Dispatched from warehouse; in transit to customer
        DELIVERED,  // Successfully delivered
        CANCELLED   // Cancelled before shipping; refund to be processed
    }
}
