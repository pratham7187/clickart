package com.clickkart.repository;

import com.clickkart.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for the {@link Product} entity (table: products).
 *
 * This is the most-queried repository in ClickKart.
 * The storefront filters by category, subcategory, price range, and keyword.
 * All storefront queries MUST include WHERE is_active = true.
 *
 * FULLTEXT search: MySQL's MATCH ... AGAINST cannot be expressed in JPQL.
 * It is implemented here using a native @Query so the DB uses its FULLTEXT
 * index (idx_products_fulltext on name + subcategory) instead of a slow LIKE scan.
 *
 * Primary consumers:
 *   - ProductService        → storefront listing, filtering, search
 *   - AdminProductService   → CRUD, stock updates
 *   - CartService           → load product before adding to cart
 *   - OrderService          → load product price at checkout for snapshot
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // =========================================================================
    // Storefront — active products only
    // =========================================================================

    /**
     * Full product listing for the storefront, newest first.
     * Paginated — the storefront grid always uses pages, never loads all rows.
     */
    Page<Product> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Non-paginated active products (used for small result sets / internal logic).
     */
    List<Product> findByIsActiveTrue();

    // =========================================================================
    // Filter by category
    // =========================================================================

    /**
     * Load active products belonging to a specific category.
     * JPQL traversal: product.category.id — Hibernate generates a JOIN automatically.
     *
     * Used by: GET /api/products?categoryId=1
     */
    List<Product> findByCategoryIdAndIsActiveTrue(Integer categoryId);

    /**
     * Paginated version — required for categories with many products.
     */
    Page<Product> findByCategoryIdAndIsActiveTrue(Integer categoryId, Pageable pageable);

    // =========================================================================
    // Filter by subcategory
    // =========================================================================

    /**
     * Load active products by subcategory slug (e.g., "tshirt", "sarees").
     * Case-sensitive — slugs are always lowercase in the DB.
     */
    List<Product> findBySubcategoryAndIsActiveTrue(String subcategory);

    /**
     * Combined filter: category + subcategory tab.
     * Used for the subcategory tabs visible on the Men's / Women's category pages.
     */
    List<Product> findByCategoryIdAndSubcategoryAndIsActiveTrue(
            Integer categoryId,
            String subcategory
    );

    /**
     * Paginated combined filter (category + subcategory).
     */
    Page<Product> findByCategoryIdAndSubcategoryAndIsActiveTrue(
            Integer categoryId,
            String subcategory,
            Pageable pageable
    );

    // =========================================================================
    // Price range filter
    // =========================================================================

    /**
     * Active products within a price range, sorted by price ascending.
     * Used for the "Price Range" filter on the storefront.
     *
     * @param minPrice inclusive lower bound (e.g., 0.00)
     * @param maxPrice inclusive upper bound (e.g., 999.99)
     */
    List<Product> findByIsActiveTrueAndPriceBetweenOrderByPriceAsc(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    /**
     * Price range filter scoped to a category.
     */
    List<Product> findByCategoryIdAndIsActiveTrueAndPriceBetweenOrderByPriceAsc(
            Integer categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    // =========================================================================
    // Keyword search — FULLTEXT (native query)
    // =========================================================================

    /**
     * Full-text search using MySQL's MATCH ... AGAINST with BOOLEAN MODE.
     *
     * Uses the FULLTEXT index: idx_products_fulltext (name, subcategory).
     * This is a native query because JPQL has no FULLTEXT equivalent.
     *
     * BOOLEAN MODE operators the caller can pass in the keyword string:
     *   "+tshirt"          → must contain 'tshirt'
     *   "men shirt"        → contains 'men' or 'shirt'
     *   "formal -blue"     → contains 'formal' but not 'blue'
     *
     * The service layer typically appends '*' for prefix matching:
     *   keyword = "kurti*"  → matches kurtis, kurti, etc.
     *
     * @param keyword the search term (passed to AGAINST — sanitise in service layer)
     */
    @Query(
        value  = "SELECT * FROM products " +
                 "WHERE MATCH(name, subcategory) AGAINST (:keyword IN BOOLEAN MODE) " +
                 "AND is_active = 1 " +
                 "ORDER BY price ASC",
        nativeQuery = true
    )
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Paginated version of the FULLTEXT search.
     * NOTE: Spring Data pagination with native @Query requires a countQuery.
     */
    @Query(
        value = "SELECT * FROM products " +
                "WHERE MATCH(name, subcategory) AGAINST (:keyword IN BOOLEAN MODE) " +
                "AND is_active = 1",
        countQuery = "SELECT COUNT(*) FROM products " +
                     "WHERE MATCH(name, subcategory) AGAINST (:keyword IN BOOLEAN MODE) " +
                     "AND is_active = 1",
        nativeQuery = true
    )
    Page<Product> searchByKeywordPageable(@Param("keyword") String keyword, Pageable pageable);

    // =========================================================================
    // Stock management (Admin / Checkout)
    // =========================================================================

    /**
     * Atomically decrement stock for a product.
     * Called inside OrderService.checkout() within a @Transactional method.
     *
     * The DB CHECK constraint (stock >= 0) provides a second safety net —
     * if the application has a concurrency bug, the DB rejects the update.
     *
     * @param productId the product to decrement
     * @param quantity  the number of units purchased
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :qty WHERE p.id = :id AND p.stock >= :qty")
    int decrementStock(@Param("id") Long productId, @Param("qty") int quantity);

    /**
     * Restore stock (e.g., order cancelled before shipping).
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :qty WHERE p.id = :id")
    void incrementStock(@Param("id") Long productId, @Param("qty") int quantity);

    // =========================================================================
    // Soft-delete (Admin)
    // =========================================================================

    /**
     * Soft-delete a product (set is_active = false).
     * ON DELETE RESTRICT on order_items.product_id prevents hard deletes for
     * products that appear on real orders — use this method instead.
     */
    @Modifying
    @Query("UPDATE Product p SET p.isActive = false WHERE p.id = :id")
    void softDeleteById(@Param("id") Long id);

    // =========================================================================
    // Admin dashboard stats
    // =========================================================================

    /** Total active products on the storefront. */
    long countByIsActiveTrue();

    /** Total out-of-stock products (stock = 0 but still active). */
    long countByIsActiveTrueAndStock(int stock);
}
