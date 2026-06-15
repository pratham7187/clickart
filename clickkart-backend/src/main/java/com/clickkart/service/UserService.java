package com.clickkart.service;

import com.clickkart.entity.User;

/**
 * Service contract for customer account management.
 *
 * Authentication (register/login/JWT) is handled by AuthService.
 * This service manages the account profile lifecycle after authentication.
 */
public interface UserService {

    /**
     * Returns a user by their primary key.
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if not found.
     *
     * @param id the user primary key
     * @return the user entity
     */
    User getUserById(Long id);

    /**
     * Returns a user by their email address.
     * Used internally when resolving the authenticated principal.
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if not found.
     *
     * @param email the login email
     * @return the user entity
     */
    User getUserByEmail(String email);

    /**
     * Updates the display name of an existing user account.
     * Email changes are intentionally excluded — they require a verification flow.
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if user not found.
     *
     * @param userId  the authenticated user's ID
     * @param newName the new display name (2–100 characters)
     * @return the updated user entity
     */
    User updateProfile(Long userId, String newName);

    /**
     * Changes the password for an authenticated user.
     * Verifies the current password before accepting the new one.
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if user not found.
     * Throws {@link IllegalArgumentException} if the current password is incorrect.
     *
     * @param userId          the authenticated user's ID
     * @param currentPassword the password currently set (plain text — verified against BCrypt hash)
     * @param newPassword     the desired new password (plain text — will be BCrypt-encoded)
     */
    void changePassword(Long userId, String currentPassword, String newPassword);

    /**
     * Soft-deletes a user account by setting is_active = false.
     * The UserDetails.isEnabled() method returns is_active, so Spring Security
     * will reject all subsequent login attempts for this account.
     *
     * Note: Accounts with placed orders cannot be hard-deleted (ON DELETE RESTRICT).
     * Soft-delete is always the correct action.
     *
     * Throws {@link com.clickkart.exception.ResourceNotFoundException} if user not found.
     *
     * @param userId the user to deactivate
     */
    void deactivateAccount(Long userId);
}
