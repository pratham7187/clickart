package com.clickkart.service.impl;

import com.clickkart.entity.Product;
import com.clickkart.exception.ResourceNotFoundException;
import com.clickkart.repository.ProductRepository;
import com.clickkart.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of {@link ProductService}.
 *
 * All public methods are read-only unless annotated with @Transactional.
 * Admin write operations (create, update, soft-delete) belong in AdminProductService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    // =========================================================================
    // Storefront listing
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Queries only active products (is_active = true) ordered by creation date
     * descending (newest products appear first on the storefront).
     */
    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        log.debug("Fetching all active products, page: {}", pageable.getPageNumber());
        return productRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
    }

    /**
     * {@inheritDoc}
     *
     * Throws {@link ResourceNotFoundException} with a descriptive message if the
     * product does not exist or has been soft-deleted.
     */
    @Override
    public Product getProductById(Long id) {
        log.debug("Fetching product by id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    // =========================================================================
    // Category and subcategory filters
    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> getProductsByCategory(Integer categoryId) {
        log.debug("Fetching active products for categoryId: {}", categoryId);
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Product> getProductsByCategoryPageable(Integer categoryId, Pageable pageable) {
        log.debug("Fetching active products for categoryId: {}, page: {}", categoryId, pageable.getPageNumber());
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> getProductsBySubcategory(String subcategory) {
        log.debug("Fetching active products for subcategory: {}", subcategory);
        return productRepository.findBySubcategoryAndIsActiveTrue(subcategory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> getProductsByCategoryAndSubcategory(Integer categoryId, String subcategory) {
        log.debug("Fetching active products for categoryId: {} and subcategory: {}", categoryId, subcategory);
        return productRepository.findByCategoryIdAndSubcategoryAndIsActiveTrue(categoryId, subcategory);
    }

    // =========================================================================
    // Full-text keyword search
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Uses MySQL MATCH ... AGAINST (BOOLEAN MODE) via a native query.
     * The keyword is trimmed before passing to the DB. If the keyword is blank,
     * all active products are returned instead of running an empty FULLTEXT query
     * (which would return zero results in MySQL BOOLEAN MODE).
     */
    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            log.debug("Empty search keyword — returning all active products");
            return productRepository.findByIsActiveTrue();
        }
        String trimmedKeyword = keyword.trim();
        log.debug("Full-text search for keyword: '{}'", trimmedKeyword);
        return productRepository.searchByKeyword(trimmedKeyword);
    }

    // =========================================================================
    // Price range filter
    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Fetching active products in price range [{}, {}]", minPrice, maxPrice);
        if (minPrice == null) minPrice = BigDecimal.ZERO;
        if (maxPrice == null) maxPrice = new BigDecimal("999999.99");
        return productRepository.findByIsActiveTrueAndPriceBetweenOrderByPriceAsc(minPrice, maxPrice);
    }
}
