package com.clickkart.controller;

import com.clickkart.dto.request.PlaceOrderRequest;
import com.clickkart.dto.response.ApiResponse;
import com.clickkart.dto.response.OrderResponse;
import com.clickkart.entity.Order;
import com.clickkart.entity.User;
import com.clickkart.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for order management.
 *
 * ALL endpoints require a valid JWT (enforced by SecurityConfig).
 * Admin-only endpoints additionally require the ADMIN role (via @PreAuthorize).
 *
 * Customer endpoints:
 *   POST   /api/orders/place        — checkout: convert cart to order
 *   GET    /api/orders              — order history (newest first)
 *   GET    /api/orders/{id}         — order detail with all line items
 *   DELETE /api/orders/{id}/cancel  — cancel a PLACED or CONFIRMED order
 *
 * Admin endpoints:
 *   GET  /api/orders/admin/all           — all orders across all users (paginated)
 *   PUT  /api/orders/admin/{id}/status   — advance order lifecycle status
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // =========================================================================
    // POST /api/orders/place  — CHECKOUT
    // =========================================================================

    /**
     * Places an order from the authenticated user's cart.
     *
     * Full atomic flow (in OrderService):
     *   1. Validate cart is not empty.
     *   2. For each cart item: atomically decrement stock.
     *   3. Snapshot prices → priceAtPurchase.
     *   4. Persist Order + OrderItems (cascade).
     *   5. Clear cart.
     *
     * On stock failure: 409 Conflict (transaction is fully rolled back).
     * On empty cart: 400 Bad Request.
     *
     * Request body:
     * <pre>
     * { "address": "123 MG Road, Bengaluru, Karnataka", "pincode": "560001" }
     * </pre>
     *
     * @return 201 Created with the full order and all line items
     */
    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PlaceOrderRequest request) {
        Long userId = currentUser.getId();
        log.info("POST /api/orders/place for userId={}", userId);

        Order order = orderService.placeOrder(userId, request.getAddress(), request.getPincode());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Order #" + order.getId() + " placed successfully!",
                        OrderResponse.from(order)
                ));
    }

    // =========================================================================
    // GET /api/orders  — order history
    // =========================================================================

    /**
     * Returns the authenticated user's order history, newest first.
     * Returns summary objects (no line items) for the order list page.
     * Use GET /api/orders/{id} to load full details with items.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrderHistory(
            @AuthenticationPrincipal User currentUser) {
        Long userId = currentUser.getId();
        log.debug("GET /api/orders for userId={}", userId);

        List<OrderResponse> response = orderService.getOrderHistory(userId)
                .stream().map(OrderResponse::summary).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // GET /api/orders/{id}  — order detail
    // =========================================================================

    /**
     * Returns a single order with all line items and product details.
     * The query is scoped to the authenticated user — a user cannot
     * retrieve another user's order by guessing the order ID.
     *
     * Uses JOIN FETCH in the repository — loaded in one SQL query.
     * Throws ResourceNotFoundException (→ 404) if not found for this user.
     *
     * @param id the order ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        Long userId = currentUser.getId();
        log.debug("GET /api/orders/{} for userId={}", id, userId);

        Order order = orderService.getOrderById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(OrderResponse.from(order)));
    }

    // =========================================================================
    // DELETE /api/orders/{id}/cancel
    // =========================================================================

    /**
     * Cancels an order. Only PLACED or CONFIRMED orders can be cancelled.
     * Cancellation restores stock for each line item.
     *
     * Throws OrderCancellationException (→ 400) if order is SHIPPED or DELIVERED.
     * Throws ResourceNotFoundException (→ 404) if order not found for this user.
     *
     * @param id the order ID to cancel
     * @return 200 OK with a confirmation message
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        Long userId = currentUser.getId();
        log.info("DELETE /api/orders/{}/cancel for userId={}", id, userId);

        orderService.cancelOrder(userId, id);
        return ResponseEntity.ok(ApiResponse.success(
                "Order #" + id + " has been cancelled. Stock has been restored.", null));
    }

    // =========================================================================
    // ADMIN: GET /api/orders/admin/all
    // =========================================================================

    /**
     * Admin-only: returns all orders across all users, newest first.
     * Requires ADMIN role — enforced by @PreAuthorize.
     *
     * Example: GET /api/orders/admin/all?page=0&size=20
     *
     * @return paginated list of all orders (summary only)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Admin GET /api/orders/admin/all page={}, size={}", page, size);
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);
        List<OrderResponse> response = orderService.getAllOrders(pageable)
                .stream().map(OrderResponse::summary).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // ADMIN: PUT /api/orders/admin/{id}/status
    // =========================================================================

    /**
     * Admin-only: advances an order through the lifecycle.
     * Allowed transitions: PLACED → CONFIRMED → SHIPPED → DELIVERED
     *
     * Request body: plain string (e.g., "CONFIRMED")
     * Example: PUT /api/orders/admin/12/status
     * Body: "SHIPPED"
     *
     * @param id        the order to update
     * @param newStatus the desired status string (must match Order.OrderStatus enum)
     * @return 200 OK with the updated order summary
     */
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody String newStatus) {
        log.info("Admin PUT /api/orders/admin/{}/status → {}", id, newStatus);
        Order.OrderStatus status;
        try {
            status = Order.OrderStatus.valueOf(newStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status: '" + newStatus + "'. Valid values: PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED");
        }
        Order updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(
                "Order #" + id + " status updated to " + status.name() + ".",
                OrderResponse.summary(updated)
        ));
    }
}
