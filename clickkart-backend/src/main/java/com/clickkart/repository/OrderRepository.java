package com.clickkart.repository;

import com.clickkart.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link Order} entity (table: orders).
 *
 * Orders are legal records. The DB enforces ON DELETE RESTRICT on user_id,
 * so users with orders can never be hard-deleted — all queries here operate
 * on the order record itself, never delete the parent user.
 *
 * Two audiences for order queries:
 *   1. CUSTOMER — "My Orders" page: filter by user_id, sorted newest first.
 *   2. ADMIN    — order management dashboard: filter by status, all users, paginated.
 *
 * Primary consumers:
 *   - OrderService      → checkout, getMyOrders, getOrderDetail
 *   - AdminOrderService → listAllOrders, updateOrderStatus
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // =========================================================================
    // Customer: "My Orders" queries
    // =========================================================================

    /**
     * All orders for a user, newest first.
     * Used for the "My Orders" page — no pagination (most users have < 50 orders).
     *
     * JPQL path: order.user.id
     */
    List<Order> findByUserIdOrderByOrderedAtDesc(Long userId);

    /**
     * Paginated version for high-volume accounts.
     */
    Page<Order> findByUserIdOrderByOrderedAtDesc(Long userId, Pageable pageable);

    /**
     * Load orders by user email.
     * Useful when the service has the JWT principal's email directly.
     */
    List<Order> findByUserEmailOrderByOrderedAtDesc(String email);

    /**
     * Load a specific order, verifying it belongs to the authenticated user.
     * Prevents users from fetching another user's order by guessing the order ID.
     *
     * Called by OrderService.getOrderDetail(orderId, userId):
     *   - Present  → return order
     *   - Empty    → throw OrderNotFoundException (or access denied)
     */
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    // =========================================================================
    // Customer: order detail with items pre-fetched (avoids N+1)
    // =========================================================================

    /**
     * Load an order with all its OrderItems and their Products in one query.
     * Used for the order detail / receipt page.
     *
     * Without JOIN FETCH, loading items then products would generate:
     *   1 query for order + N queries for items + N queries for products = 2N+1
     *
     * With JOIN FETCH:
     *   1 query total (Hibernate uses LEFT JOIN across three tables).
     */
    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.items oi
            JOIN FETCH oi.product
            WHERE o.id = :orderId AND o.user.id = :userId
            """)
    Optional<Order> findByIdAndUserIdWithItems(
            @Param("orderId") Long orderId,
            @Param("userId")  Long userId
    );

    // =========================================================================
    // Admin: order management dashboard
    // =========================================================================

    /**
     * All orders across all users, newest first. Paginated.
     * Used for the admin "All Orders" table.
     */
    Page<Order> findAllByOrderByOrderedAtDesc(Pageable pageable);

    /**
     * Filter orders by status (e.g., show all PLACED orders awaiting confirmation).
     * Used for the status filter tabs on the admin order management dashboard.
     */
    List<Order> findByStatusOrderByOrderedAtDesc(Order.OrderStatus status);

    /**
     * Paginated version — used when the admin filters by a status with many rows
     * (e.g., SHIPPED during a sale event).
     */
    Page<Order> findByStatusOrderByOrderedAtDesc(Order.OrderStatus status, Pageable pageable);

    // =========================================================================
    // Admin: status update
    // =========================================================================

    /**
     * Update the status of an order.
     * Called by AdminOrderService.updateStatus(orderId, newStatus).
     * @Modifying + @Transactional required on the calling service method.
     *
     * The @PreUpdate hook on the Order entity automatically updates updated_at.
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    int updateStatus(
            @Param("orderId") Long orderId,
            @Param("status")  Order.OrderStatus status
    );

    // =========================================================================
    // Dashboard statistics
    // =========================================================================

    /** Total number of orders by status (admin dashboard KPI cards). */
    long countByStatus(Order.OrderStatus status);

    /** Total number of orders placed by a specific user. */
    long countByUserId(Long userId);

    /**
     * Revenue total for all delivered orders.
     * Returns null if no delivered orders exist — caller should handle.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    java.math.BigDecimal sumRevenueDelivered();
}
