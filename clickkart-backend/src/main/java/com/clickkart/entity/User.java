package com.clickkart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Mapped to DB table: users
 *
 * Implements UserDetails so Spring Security can load this entity directly
 * from UserDetailsServiceImpl without a wrapper object.
 *
 * Relationships:
 *   users -> cart         (OneToOne,  mappedBy = "user",  CASCADE = ALL)
 *   users -> orders       (OneToMany, mappedBy = "user",  CASCADE = ALL)
 *   users -> wishlist_items (OneToMany, mappedBy = "user", CASCADE = ALL)
 *
 * is_active = false means soft-deleted. The UserDetails method isEnabled()
 * returns this flag, so Spring Security rejects login for deactivated accounts.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uq_users_email", columnNames = "email")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    // -------------------------------------------------------------------------
    // Primary Key
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // -------------------------------------------------------------------------
    // Columns
    // -------------------------------------------------------------------------
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Column(name = "email", nullable = false, length = 150, unique = true)
    private String email;

    /**
     * BCrypt hash stored here — NEVER plain text.
     * Set by AuthService using BCryptPasswordEncoder.encode().
     */
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Mapped as a String column matching the MySQL ENUM('USER','ADMIN').
     * Hibernate stores the enum name() — "USER" or "ADMIN" — which matches
     * the ENUM values in MySQL exactly.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10,
            columnDefinition = "ENUM('USER','ADMIN') DEFAULT 'USER'")
    @Builder.Default
    private Role role = Role.USER;

    /**
     * Soft-delete flag. FALSE = account deactivated.
     * The UserDetails.isEnabled() method returns this value.
     * ON DELETE RESTRICT on orders prevents hard deletes for users with orders.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Lifecycle Hooks
    // -------------------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * Bidirectional OneToOne with Cart.
     * LAZY fetch — we don't need the cart on every user load.
     * cascade = ALL: persisting/deleting a user manages its cart.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    /**
     * Bidirectional OneToMany with Order.
     * LAZY — order list is loaded only when explicitly requested.
     * ON DELETE RESTRICT in DB: never hard-delete a user with orders.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    /**
     * Bidirectional OneToMany with WishlistItem.
     * LAZY — wishlist loaded only on the wishlist page.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WishlistItem> wishlistItems;

    // -------------------------------------------------------------------------
    // UserDetails Implementation
    // Spring Security uses these methods to authenticate and authorize.
    // -------------------------------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security requires role name prefixed with "ROLE_"
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // Spring Security uses email as the username
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Soft-delete: is_active = false disables login via Spring Security
        return isActive;
    }

    // -------------------------------------------------------------------------
    // Role Enum
    // -------------------------------------------------------------------------
    public enum Role {
        USER,   // Standard customer: browse, cart, orders, wishlist
        ADMIN   // Platform manager: product CRUD, order status management
    }
}
