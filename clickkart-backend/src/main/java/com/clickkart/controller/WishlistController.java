package com.clickkart.controller;

import com.clickkart.dto.response.ApiResponse;
import com.clickkart.dto.response.CartResponse;
import com.clickkart.dto.response.WishlistItemResponse;
import com.clickkart.entity.Cart;
import com.clickkart.entity.CartItem;
import com.clickkart.entity.User;
import com.clickkart.entity.WishlistItem;
import com.clickkart.service.CartService;
import com.clickkart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for wishlist (save for later) operations.
 *
 * ALL endpoints require a valid JWT (enforced by SecurityConfig).
 *
 * Endpoints:
 *   GET    /api/wishlist                       — get full wishlist
 *   POST   /api/wishlist/add/{productId}       — add product to wishlist
 *   DELETE /api/wishlist/remove/{productId}    — remove product from wishlist
 *   POST   /api/wishlist/move-to-cart/{productId} — move to cart and remove from wishlist
 *   GET    /api/wishlist/check/{productId}     — check if a product is wishlisted (for heart icon)
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;
    private final CartService     cartService;

    // =========================================================================
    // GET /api/wishlist
    // =========================================================================

    /**
     * Returns all products in the authenticated user's wishlist.
     * Uses JOIN FETCH in the repository — no N+1 query problem.
     *
     * Response example:
     * <pre>
     * { "success": true, "data": [
     *     { "id": 3, "product": { "id": 7, "name": "Silk Saree", "price": 1499.00, ... }, "addedAt": "..." }
     * ]}
     * </pre>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getWishlist(
            @AuthenticationPrincipal User currentUser) {
        log.debug("GET /api/wishlist for userId={}", currentUser.getId());
        List<WishlistItemResponse> response = wishlistService.getWishlist(currentUser.getId())
                .stream().map(WishlistItemResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // POST /api/wishlist/add/{productId}
    // =========================================================================

    /**
     * Adds a product to the wishlist.
     * Returns 201 Created with the new wishlist item.
     * Returns 409 Conflict if the product is already in the wishlist.
     *
     * @param productId the product to bookmark
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addToWishlist(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId) {
        log.debug("POST /api/wishlist/add/{} for userId={}", productId, currentUser.getId());
        WishlistItem item = wishlistService.addToWishlist(currentUser.getId(), productId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added to wishlist.", WishlistItemResponse.from(item)));
    }

    // =========================================================================
    // DELETE /api/wishlist/remove/{productId}
    // =========================================================================

    /**
     * Removes a product from the wishlist.
     * Idempotent — no error if the product is not in the wishlist.
     *
     * @param productId the product to remove
     * @return 200 OK with a success message
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId) {
        log.debug("DELETE /api/wishlist/remove/{} for userId={}", productId, currentUser.getId());
        wishlistService.removeFromWishlist(currentUser.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist.", null));
    }

    // =========================================================================
    // POST /api/wishlist/move-to-cart/{productId}
    // =========================================================================

    /**
     * Moves a product from the wishlist into the cart in one atomic operation.
     *
     * Business logic (in WishlistService):
     *   1. Verify product is in the wishlist.
     *   2. Call CartService.addToCart() — throws on stock failure.
     *   3. Only if step 2 succeeds, remove from wishlist.
     *
     * The @Transactional annotation on WishlistServiceImpl.moveToCart() ensures
     * the wishlist entry is NOT removed if the cart add fails.
     *
     * Query param:
     *   quantity — number of units to add to cart (default: 1)
     *
     * @return 200 OK with the refreshed cart state
     */
    @PostMapping("/move-to-cart/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> moveToCart(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        Long userId = currentUser.getId();
        log.debug("POST /api/wishlist/move-to-cart/{} for userId={}, qty={}", productId, userId, quantity);

        wishlistService.moveToCart(userId, productId, quantity);

        // Return the refreshed cart state so the frontend can update the cart badge
        Cart cart = cartService.getCart(userId);
        List<CartItem> items = cartService.getCartItems(userId);
        CartResponse response = CartResponse.from(cart.getId(), items);
        return ResponseEntity.ok(ApiResponse.success("Product moved to cart.", response));
    }

    // =========================================================================
    // GET /api/wishlist/check/{productId}
    // =========================================================================

    /**
     * Returns whether a specific product is in the authenticated user's wishlist.
     * Used to render the filled ♥ / unfilled ♡ heart icon on product cards.
     *
     * @param productId the product to check
     * @return 200 OK with boolean result
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> isProductWishlisted(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId) {
        boolean wishlisted = wishlistService.isProductWishlisted(currentUser.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(wishlisted));
    }
}
