package com.clickkart.service;

import com.clickkart.dto.request.LoginRequest;
import com.clickkart.dto.request.RegisterRequest;
import com.clickkart.dto.response.AuthResponse;

/**
 * Service contract for authentication operations.
 * Handles user registration and login with JWT issuance.
 */
public interface AuthService {

    /**
     * Registers a new customer account.
     * Throws DuplicateResourceException if the email is already in use.
     *
     * @param request the registration payload (name, email, password)
     * @return AuthResponse with a JWT token and user details
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user and issues a JWT.
     * Delegates to Spring Security's AuthenticationManager for credential verification.
     * Throws BadCredentialsException if email/password are wrong.
     *
     * @param request the login payload (email, password)
     * @return AuthResponse with a JWT token and user details
     */
    AuthResponse login(LoginRequest request);
}
