package com.clickkart.controller;

import com.clickkart.dto.response.ApiResponse;
import com.clickkart.dto.response.CategoryResponse;
import com.clickkart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product categories.
 *
 * All endpoints are PUBLIC (no JWT required) — see SecurityConfig.
 *
 * Endpoints:
 *   GET /api/categories          — all active categories (for home page grid + dropdowns)
 *   GET /api/categories/{id}     — single category by integer ID
 *   GET /api/categories/slug/{name} — single category by URL slug (e.g., "men", "women")
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    // =========================================================================
    // GET /api/categories
    // =========================================================================

    /**
     * Returns all active categories ordered by sort_order ascending.
     * Used by the home page hero grid and the product-filtering dropdown.
     *
     * Response example:
     * <pre>
     * { "success": true, "data": [
     *     { "id": 1, "name": "men",   "displayName": "Mens Fashion",   "imageUrl": "assets/image/men.jpg" },
     *     { "id": 2, "name": "women", "displayName": "Womens Fashion", "imageUrl": "assets/image/women.jpg" },
     *     { "id": 3, "name": "kids",  "displayName": "Kids Fashion",   "imageUrl": "assets/image/kid.jpg" }
     * ]}
     * </pre>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        log.debug("GET /api/categories");
        List<CategoryResponse> response = categoryService.getAllCategories()
                .stream().map(CategoryResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // GET /api/categories/{id}
    // =========================================================================

    /**
     * Returns a single category by its integer primary key.
     * Throws ResourceNotFoundException (→ 404) if not found.
     *
     * @param id category primary key (INT in DB, Integer in Java)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Integer id) {
        log.debug("GET /api/categories/{}", id);
        CategoryResponse response = CategoryResponse.from(categoryService.getCategoryById(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // GET /api/categories/slug/{name}
    // =========================================================================

    /**
     * Returns a single category by its URL slug.
     * Used when the frontend passes a category name as a path segment.
     *
     * Example: GET /api/categories/slug/men
     * Throws ResourceNotFoundException (→ 404) if slug not found.
     *
     * @param name the category slug (e.g., "men", "women", "kids")
     */
    @GetMapping("/slug/{name}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(
            @PathVariable String name) {
        log.debug("GET /api/categories/slug/{}", name);
        CategoryResponse response = CategoryResponse.from(
                categoryService.getCategoryByName(name.toLowerCase().trim()));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
