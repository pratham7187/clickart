package com.clickkart.service.impl;

import com.clickkart.entity.User;
import com.clickkart.exception.ResourceNotFoundException;
import com.clickkart.repository.UserRepository;
import com.clickkart.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Implementation of {@link UserService}.
 *
 * Authentication (register/login/JWT issuance) lives in AuthService.
 * This service manages the account profile after authentication.
 *
 * Password handling: PasswordEncoder (BCryptPasswordEncoder) is injected
 * as an interface. The concrete bean is declared in SecurityConfig.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository   userRepository;
    private final PasswordEncoder  passwordEncoder;

    // =========================================================================
    // Lookup
    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * {@inheritDoc}
     *
     * Also used by UserDetailsServiceImpl to load the principal on every request.
     */
    @Override
    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    // =========================================================================
    // Profile update
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Only the display name is mutable here. Email changes require a dedicated
     * verification flow (send confirmation email → verify → update) which is
     * outside the scope of this method.
     *
     * Validation: name must not be blank and must be 2–100 characters.
     */
    @Override
    @Transactional
    public User updateProfile(Long userId, String newName) {
        if (!StringUtils.hasText(newName) || newName.trim().length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters.");
        }
        if (newName.trim().length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 characters.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setName(newName.trim());
        User saved = userRepository.save(user);

        log.info("Profile updated for userId: {}", userId);
        return saved;
    }

    // =========================================================================
    // Password change
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Security flow:
     *   1. Load user.
     *   2. Verify currentPassword against the stored BCrypt hash.
     *   3. Encode newPassword with BCrypt.
     *   4. Persist the new hash.
     *
     * Does not invalidate existing JWT tokens — that requires a token blacklist
     * or a short expiry policy, which is a security config concern.
     */
    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify the supplied current password against the stored hash
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        // Enforce minimum new password length
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for userId: {}", userId);
    }

    // =========================================================================
    // Account deactivation (soft-delete)
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Sets is_active = false using a targeted JPQL UPDATE query.
     * Spring Security's isEnabled() returns is_active, so the user is immediately
     * locked out without invalidating any specific token.
     *
     * ON DELETE RESTRICT on orders.user_id means this account can never be
     * hard-deleted if they have placed orders — soft-delete is always correct.
     */
    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        // Validate the user exists before issuing the UPDATE
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        userRepository.softDeleteById(userId);
        log.info("Account deactivated (soft-deleted) for userId: {}", userId);
    }
}
