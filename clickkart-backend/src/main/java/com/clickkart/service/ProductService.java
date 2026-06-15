package com.clickkart.service;

import com.clickkart.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service contract for product catalog operations.
 *
 * All storefront-facing methods filter by is_active = true.
 * Admin methods (create, update, soft-delete) are handled by AdminProductService.
 */
public interface ProductService {

    /**
     * Returns a paginated list of all active products, newest first.
     * Used for the main storefront grid.
     *
     * @param pageable pagination and sorting parameters
     * @return page of active products
     */
    Page<Product> getAllProducts(Pageable pageable);

    /**
     * Returns a single product by its ID.
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if not found.
     *
     * @param id the product ID
     * @return the product entity
     */
    Product getProductById(Long id);

    /**
     * Returns all active products belonging to a specific top-level category.
     * Used by: GET /api/products?categoryId=1
     *
     * @param categoryId the category primary key
     * @return list of active products in that category
     */
    List<Product> getProductsByCategory(Integer categoryId);

    /**
     * Paginated version of getProductsByCategory().
     *
     * @param categoryId the category primary key
     * @param pageable   pagination parameters
     * @return page of active products in that category
     */
    Page<Product> getProductsByCategoryPageable(Integer categoryId, Pageable pageable);

    /**
     * Returns all active products matching a subcategory slug.
     * Used by subcategory filter tabs on category pages.
     *
     * @param subcategory subcategory slug (e.g., "tshirt", "sarees")
     * @return list of active products in that subcategory
     */
    List<Product> getProductsBySubcategory(String subcategory);

    /**
     * Combined filter: active products matching both category and subcategory.
     * Used when the user selects a subcategory tab on a specific category page.
     *
     * @param categoryId  the category primary key
     * @param subcategory subcategory slug
     * @return filtered product list
     */
    List<Product> getProductsByCategoryAndSubcategory(Integer categoryId, String subcategory);

    /**
     * Full-text keyword search across product name and subcategory.
     * Uses MySQL MATCH ... AGAINST (BOOLEAN MODE) via a native query.
     * Returns active products only.
     *
     * @param keyword the search term (sanitised by caller before passing)
     * @return list of matching active products
     */
    List<Product> searchProducts(String keyword);

    /**
     * Returns active products within the given price range, sorted by price ascending.
     *
     * @param minPrice inclusive lower bound
     * @param maxPrice inclusive upper bound
     * @return filtered and sorted product list
     */
    List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
}
