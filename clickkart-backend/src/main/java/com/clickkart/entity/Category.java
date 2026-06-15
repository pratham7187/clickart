package com.clickkart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapped to DB table: categories
 *
 * Lookup table for product top-level categories.
 * Replaces a hardcoded ENUM — new categories can be added at runtime
 * via the Admin API with zero schema migration.
 *
 * Relationships:
 *   categories -> products  (OneToMany, mappedBy = "category")
 *   ON DELETE RESTRICT: a category with products cannot be deleted.
 */
@Entity
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uq_categories_name", columnNames = "name"),
        indexes = @Index(name = "idx_categories_sort", columnList = "sort_order")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    // -------------------------------------------------------------------------
    // Primary Key
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // -------------------------------------------------------------------------
    // Columns
    // -------------------------------------------------------------------------

    /**
     * API slug used in query params: "men", "women", "kids".
     * Globally unique — MySQL enforces this via uq_categories_name.
     */
    @NotBlank(message = "Category name (slug) is required")
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Human-readable UI label: "Mens Fashion", "Womens Fashion", etc.
     * Can be changed freely without affecting any FK relationships.
     */
    @NotBlank(message = "Display name is required")
    @Size(max = 150)
    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    /** Optional marketing copy shown on the category landing page. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Path to the hero/banner image shown on the home page category grid. */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Ascending display sequence on the home page (1 = first, 2 = second, …).
     * Allows admin to reorder categories without touching the API slug.
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Soft-hide flag. FALSE removes the category from the storefront
     * but does NOT delete its products.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // -------------------------------------------------------------------------
    // Lifecycle Hooks
    // -------------------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * One category has many products.
     * LAZY: product list is loaded only when explicitly accessed.
     * ON DELETE RESTRICT in DB: cannot delete a category that still has products.
     * mappedBy references the "category" field in Product entity.
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;
}
