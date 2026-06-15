package com.clickkart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapped to DB table: products
 *
 * The complete product catalog. Every item the customer sees on the storefront.
 *
 * DB constraints mirrored in Java:
 *   CHECK (price > 0)   → @DecimalMin("0.01") on price
 *   CHECK (stock >= 0)  → @Min(0) on stock
 *
 * Relationships:
 *   products -> categories   (ManyToOne  - FK: category_id)
 *   products -> cart_items   (OneToMany, mappedBy = "product")
 *   products -> wishlist_items (OneToMany, mappedBy = "product")
 *   products -> order_items  (OneToMany, mappedBy = "product")
 *
 * Soft-delete: is_active = false removes from storefront; historical
 * order_items rows still reference this product (ON DELETE RESTRICT).
 */
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_category_id", columnList = "category_id"),
                @Index(name = "idx_products_subcategory",  columnList = "subcategory"),
                @Index(name = "idx_products_price",        columnList = "price"),
                @Index(name = "idx_products_is_active",    columnList = "is_active"),
                @Index(name = "idx_products_cat_sub",      columnList = "category_id, subcategory")
                // FULLTEXT INDEX idx_products_fulltext (name, subcategory) is defined in DB only
                // — JPA does not have a FULLTEXT annotation; queries use @Query with native SQL
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

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
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Unit price in INR. Stored as DECIMAL(10,2) in MySQL.
     * BigDecimal is the correct Java type for monetary values —
     * never use float or double for currency.
     * The ₹ symbol is a frontend concern only.
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format invalid: max 8 integer digits, 2 decimal places")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotBlank(message = "Image URL is required")
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /**
     * Finer grouping within a category: tshirt, formal, jeans, joggers,
     * footwear, sarees, lehenga, kurtis, bottomwear, upperwear, etc.
     */
    @NotBlank(message = "Subcategory is required")
    @Size(max = 100)
    @Column(name = "subcategory", nullable = false, length = 100)
    private String subcategory;

    /** Long-form product description displayed on the product detail page. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Available inventory count.
     * Decremented atomically inside OrderService.checkout()
     * with a transactional UPDATE ... WHERE stock >= requiredQty.
     * The DB CHECK constraint (stock >= 0) is the last line of defence.
     */
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(name = "stock", nullable = false)
    @Builder.Default
    private Integer stock = 0;

    /**
     * Soft-delete flag.
     * FALSE = product removed from storefront but preserved in order_items history.
     * ON DELETE RESTRICT on order_items.product_id prevents hard-deleting products
     * that exist on real orders.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

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
     * ManyToOne: many products belong to one category.
     * LAZY — we join the category when explicitly needed (e.g., product detail page).
     * ForeignKey name matches the DB constraint: fk_products_category.
     * ON DELETE RESTRICT / ON UPDATE CASCADE in DB.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_category")
    )
    private Category category;

    /**
     * OneToMany: a product can appear in many carts.
     * Owned by CartItem (product_id FK is in cart_items table).
     * LAZY — not needed when loading a product by itself.
     */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<CartItem> cartItems;

    /**
     * OneToMany: a product can be wishlisted by many users.
     * ON DELETE CASCADE in DB: product deleted → removed from all wishlists.
     */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<WishlistItem> wishlistItems;

    /**
     * OneToMany: a product can appear in many order items (historical orders).
     * ON DELETE RESTRICT in DB: cannot delete a product on a real order.
     */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
}
