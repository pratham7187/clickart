package com.clickkart.controller;

import com.clickkart.dto.request.AddToCartRequest;
import com.clickkart.dto.request.UpdateCartItemRequest;
import com.clickkart.dto.response.ApiResponse;
import com.clickkart.dto.response.CartResponse;
import com.clickkart.entity.Cart;
import com.clickkart.entity.CartItem;
import com.clickkart.entity.User;
import com.clickkart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for shopping cart operations.
 *
 * ALL endpoints require a valid JWT (enforced by SecurityConfig).
 * The authenticated user is injected via @AuthenticationPrincipal — Spring
 * resolves it from the SecurityContext populated by JwtAuthFilter.
 *
 * Endpoints:
 *   GET    /api/cart                        — get the full cart with items
 *   POST   /api/cart/add                    — add a product (or increment qty)
 *   PUT    /api/cart/update                 — set absolute quantity for an item
 *   DELETE /api/cart/remove/{productId}     — remove a product line
 *   DELETE /api/cart/clear                  — empty the entire cart
 *   GET    /api/cart/count                  — item count badge (for nav header)
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    // =========================================================================
    // GET /api/cart  — full cart with item lines
    // =========================================================================

    /**
     * Returns the authenticated user's cart with all product lines.
     * If no cart exists yet, returns an empty cart (lazy creation pattern).
     *
     * Response example:
     * <pre>
     * { "success": true, "data": {
     *     "cartId": 5,
     *     "items": [ { "product": {...}, "quantity": 2, "subtotal": 998.00 } ],
     *     "itemCount": 1,
     *     "totalAmount": 998.00
     * }}
     * </pre>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal User currentUser) {
        Long userId = currentUser.getId();
        log.debug("GET /api/cart for userId={}", userId);

        Cart cart = cartService.getCart(userId);
        List<CartItem> items = cartService.getCartItems(userId);
        CartResponse response = CartResponse.from(cart.getId(), items);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // POST /api/cart/add
    // =========================================================================

    /**
     * Adds a product to the cart, or increments its quantity if already present.
     *
     * Business rules (enforced by CartService):
     *   - Product must be active.
     *   - New total quantity must not exceed available stock.
     *   - Same product appears only once per cart (merged, not duplicated).
     *
     * Request body:
     * <pre>
     * { "productId": 7, "quantity": 2 }
     * </pre>
     *
     * @param request  validated add-to-cart payload
     * @return 200 OK with the refreshed cart state
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AddToCartRequest request) {
        Long userId = currentUser.getId();
        log.debug("POST /api/cart/add userId={}, productId={}, qty={}",
                userId, request.getProductId(), request.getQuantity());

        cartService.addToCart(userId, request.getProductId(), request.getQuantity());

        // Return the full refreshed cart state
        Cart cart = cartService.getCart(userId);
        List<CartItem> items = cartService.getCartItems(userId);
        CartResponse response = CartResponse.from(cart.getId(), items);
        return ResponseEntity.ok(ApiResponse.success("Product added to cart.", response));
    }

    // =========================================================================
    // PUT /api/cart/update
    // =========================================================================

    /**
     * Sets the quantity of a product line to an absolute value.
     * Sending quantity = 0 removes the item.
     *
     * Request body:
     * <pre>
     * { "productId": 7, "quantity": 3 }
     * </pre>
     *
     * @return 200 OK with the refreshed cart state
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = currentUser.getId();
        log.debug("PUT /api/cart/update userId={}, productId={}, newQty={}",
                userId, request.getProductId(), request.getQuantity());

        cartService.updateCartItemQuantity(userId, request.getProductId(), request.getQuantity());

        Cart cart = cartService.getCart(userId);
        List<CartItem> items = cartService.getCartItems(userId);
        CartResponse response = CartResponse.from(cart.getId(), items);
        return ResponseEntity.ok(ApiResponse.success("Cart updated.", response));
    }

    // =========================================================================
    // DELETE /api/cart/remove/{productId}
    // =========================================================================

    /**
     * Removes a specific product line from the cart.
     * Idempotent — no error if the product is not in the cart.
     *
     * @param productId the product to remove
     * @return 200 OK with the refreshed cart state
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId) {
        Long userId = currentUser.getId();
        log.debug("DELETE /api/cart/remove/{} for userId={}", productId, userId);

        cartService.removeFromCart(userId, productId);

        Cart cart = cartService.getCart(userId);
        List<CartItem> items = cartService.getCartItems(userId);
        CartResponse response = CartResponse.from(cart.getId(), items);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart.", response));
    }

    // =========================================================================
    // DELETE /api/cart/clear
    // =========================================================================

    /**
     * Empties all items from the cart (cart header row is preserved).
     *
     * @return 200 OK with an empty cart response
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            @AuthenticationPrincipal User currentUser) {
        Long userId = currentUser.getId();
        log.debug("DELETE /api/cart/clear for userId={}", userId);

        cartService.clearCart(userId);

        Cart cart = cartService.getCart(userId);
        CartResponse response = CartResponse.empty(cart.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared.", response));
    }

    // =========================================================================
    // GET /api/cart/count
    // =========================================================================

    /**
     * Returns only the count of distinct product lines in the cart.
     * Designed for the cart badge in the navigation header — minimal payload.
     *
     * @return 200 OK with a single integer count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(
            @AuthenticationPrincipal User currentUser) {
        int count = cartService.getCartItemCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
