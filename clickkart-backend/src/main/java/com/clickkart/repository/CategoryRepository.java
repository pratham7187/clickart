package com.clickkart.repository;

import com.clickkart.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link Category} entity (table: categories).
 *
 * Categories is a small, stable lookup table (typically 3–20 rows).
 * No pagination is needed here — the full list is always loaded.
 *
 * Primary consumers:
 *   - ProductService  → validate categoryId on product create/update
 *   - CategoryService → home page category grid
 *   - AdminService    → CRUD management of categories
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // =========================================================================
    // Storefront queries
    // =========================================================================

    /**
     * Home page: load all visible categories ordered by their display position.
     * is_active = false hides a category without deleting it or its products.
     * sortOrder determines the left-to-right sequence on the home page grid.
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * Load every category ordered by sortOrder, regardless of is_active status.
     * Used by the admin category management page.
     */
    List<Category> findAllByOrderBySortOrderAsc();

    // =========================================================================
    // Lookup by slug (name)
    // =========================================================================

    /**
     * Look up a category by its API slug (e.g., "men", "women", "kids").
     * Used when the frontend passes ?category=men in query params.
     * Returns Optional — caller handles "category not found" gracefully.
     */
    Optional<Category> findByName(String name);

    /**
     * Returns true if a category with this slug already exists.
     * Used by AdminCategoryService.create() to reject duplicates before INSERT.
     */
    boolean existsByName(String name);

    // =========================================================================
    // Active/inactive management
    // =========================================================================

    /**
     * All active categories — used by the product creation form dropdown
     * to list only categories customers can see.
     */
    List<Category> findByIsActiveTrue();
}
