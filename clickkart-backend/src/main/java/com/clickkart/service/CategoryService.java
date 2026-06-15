package com.clickkart.service;

import com.clickkart.entity.Category;

import java.util.List;

/**
 * Service contract for product category operations.
 * Categories are a small, stable lookup table (3–20 rows).
 */
public interface CategoryService {

    /**
     * Returns all active categories ordered by sort_order ascending.
     * Used to populate the home page category grid and product-creation dropdowns.
     *
     * @return ordered list of active categories
     */
    List<Category> getAllCategories();

    /**
     * Returns all categories including inactive ones.
     * Used by the admin category management panel.
     *
     * @return all categories ordered by sort_order
     */
    List<Category> getAllCategoriesForAdmin();

    /**
     * Returns a single category by its integer ID.
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if not found.
     *
     * @param id the category primary key (INT in DB)
     * @return the category entity
     */
    Category getCategoryById(Integer id);

    /**
     * Returns a single category by its URL slug (e.g., "men", "women", "kids").
     * Used when the frontend sends ?category=men as a query parameter.
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if not found.
     *
     * @param name the category slug
     * @return the matching category entity
     */
    Category getCategoryByName(String name);
}
