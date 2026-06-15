package com.clickkart.controller;

import com.clickkart.dto.response.ApiResponse;
import com.clickkart.dto.response.ProductResponse;
import com.clickkart.entity.Product;
import com.clickkart.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for the product catalog.
 *
 * All endpoints are PUBLIC (no JWT required) — see SecurityConfig.
 *
 * Endpoints:
 *   GET /api/products                          — paginated storefront listing
 *   GET /api/products/{id}                     — single product detail
 *   GET /api/products/category/{categoryId}    — filter by top-level category
 *   GET /api/products/subcategory/{sub}        — filter by subcategory tab
 *   GET /api/products/search?keyword=          — full-text keyword search
 *   GET /api/products/filter?min=&max=&cat=    — price range + optional category
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    // =========================================================================
    // GET /api/products   (paginated, newest first)
    // =========================================================================

    /**
     * Returns a paginated page of active products, newest first.
     *
     * Query parameters (all optional):
     *   page  (default 0)
     *   size  (default 12)
     *   sort  (default createdAt,desc)
     *
     * Example: GET /api/products?page=0&size=12
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.debug("GET /api/products page={}", pageable.getPageNumber());
        Page<Product> productPage = productService.getAllProducts(pageable);
        Page<ProductResponse> responsePage = productPage.map(ProductResponse::from);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    // =========================================================================
    // GET /api/products/{id}
    // =========================================================================

    /**
     * Returns a single product by ID.
     * Throws ResourceNotFoundException (→ 404) if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        log.debug("GET /api/products/{}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(ProductResponse.from(product)));
    }

    // =========================================================================
    // GET /api/products/category/{categoryId}
    // =========================================================================

    /**
     * Returns all active products for a top-level category.
     * Also supports an optional subcategory refinement query param.
     *
     * Example: GET /api/products/category/1
     * Example: GET /api/products/category/1?subcategory=tshirt
     *
     * @param categoryId  category primary key (1=men, 2=women, 3=kids)
     * @param subcategory optional subcategory slug for tab-level filtering
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(required = false) String subcategory) {
        log.debug("GET /api/products/category/{}, subcategory={}", categoryId, subcategory);

        List<Product> products = (subcategory != null && !subcategory.isBlank())
                ? productService.getProductsByCategoryAndSubcategory(categoryId, subcategory.trim())
                : productService.getProductsByCategory(categoryId);

        List<ProductResponse> response = products.stream().map(ProductResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // GET /api/products/subcategory/{subcategory}
    // =========================================================================

    /**
     * Returns all active products for a subcategory across all parent categories.
     * Example: GET /api/products/subcategory/sarees
     */
    @GetMapping("/subcategory/{subcategory}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsBySubcategory(
            @PathVariable String subcategory) {
        log.debug("GET /api/products/subcategory/{}", subcategory);
        List<ProductResponse> response = productService.getProductsBySubcategory(subcategory)
                .stream().map(ProductResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // GET /api/products/search?keyword=
    // =========================================================================

    /**
     * Full-text keyword search across product name and subcategory.
     * Uses MySQL MATCH ... AGAINST (BOOLEAN MODE) for performance.
     *
     * Example: GET /api/products/search?keyword=kurti
     *
     * @param keyword the search term (empty keyword returns all active products)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        log.debug("GET /api/products/search?keyword='{}'", keyword);
        List<ProductResponse> response = productService.searchProducts(keyword)
                .stream().map(ProductResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(
                response.isEmpty() ? "No products found for: " + keyword : "Search results for: " + keyword,
                response
        ));
    }

    // =========================================================================
    // GET /api/products/filter?min=&max=&categoryId=
    // =========================================================================

    /**
     * Price range filter with optional category scope.
     *
     * Example: GET /api/products/filter?min=500&max=2000
     * Example: GET /api/products/filter?min=500&max=2000&categoryId=2
     *
     * @param min        minimum price (inclusive, default 0)
     * @param max        maximum price (inclusive, default ₹99,999)
     * @param categoryId optional category scope
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> filterByPrice(
            @RequestParam(defaultValue = "0") BigDecimal min,
            @RequestParam(defaultValue = "99999") BigDecimal max,
            @RequestParam(required = false) Integer categoryId) {
        log.debug("GET /api/products/filter min={}, max={}, categoryId={}", min, max, categoryId);
        List<Product> products = productService.getProductsByPriceRange(min, max);

        // Apply optional category filter in-memory (the DB already filters by price)
        if (categoryId != null) {
            final Integer cid = categoryId;
            products = products.stream()
                    .filter(p -> p.getCategory().getId().equals(cid))
                    .toList();
        }

        List<ProductResponse> response = products.stream().map(ProductResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
