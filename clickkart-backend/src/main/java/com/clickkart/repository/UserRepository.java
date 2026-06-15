package com.clickkart.repository;

import com.clickkart.entity.User;
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
 * Repository for the {@link User} entity (table: users).
 *
 * Spring Data derives SQL from method names automatically.
 * Custom @Query methods use JPQL (entity field names, not column names).
 *
 * Primary consumers:
 *   - UserDetailsServiceImpl  → loadUserByUsername(email)
 *   - AuthService             → register, login
 *   - AdminUserService        → list users, soft-delete
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================================================================
    // Lookup by email
    // =========================================================================

    /**
     * Used by UserDetailsServiceImpl to load a user at authentication time.
     * Returns Optional — caller decides how to handle missing users.
     * Spring Security calls this on every protected request.
     */
    Optional<User> findByEmail(String email);

    /**
     * Used by AuthService.register() to validate email uniqueness before
     * attempting an INSERT, giving a cleaner error message than a DB exception.
     */
    boolean existsByEmail(String email);

    // =========================================================================
    // Role-based queries (Admin use)
    // =========================================================================

    /**
     * Admin dashboard: list all customers (excludes admin accounts).
     * JPQL references the entity field 'role' and enum constant User.Role.USER.
     */
    List<User> findByRole(User.Role role);

    /**
     * Admin: paginated user list filtered by role.
     * Used when the user list is large enough to need pagination.
     */
    Page<User> findByRole(User.Role role, Pageable pageable);

    // =========================================================================
    // Active / soft-delete queries
    // =========================================================================

    /**
     * Derived query for: WHERE is_active = true
     * Spring Data resolves the boolean field 'isActive' to a TINYINT(1) filter.
     */
    List<User> findByIsActiveTrue();

    /**
     * Admin: paginated list of all active users, sorted by registration date.
     */
    Page<User> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    // =========================================================================
    // Soft-delete (Admin only — deactivate without hard delete)
    // =========================================================================

    /**
     * Soft-deletes a user by setting is_active = false.
     * This is the correct delete strategy when the user has placed orders
     * (ON DELETE RESTRICT on orders.user_id would reject a hard delete).
     *
     * @Modifying requires @Transactional on the calling service method.
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :id")
    void softDeleteById(@Param("id") Long id);

    /**
     * Reactivate a previously deactivated account.
     * Used by admin to restore accounts after review.
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = true WHERE u.id = :id")
    void reactivateById(@Param("id") Long id);

    // =========================================================================
    // Count queries
    // =========================================================================

    /** Total registered customers (for admin dashboard stats). */
    long countByRole(User.Role role);

    /** Total active users (for admin dashboard active user count). */
    long countByIsActiveTrue();
}
