package com.clickkart.controller;

import com.clickkart.dto.request.LoginRequest;
import com.clickkart.dto.request.RegisterRequest;
import com.clickkart.dto.response.ApiResponse;
import com.clickkart.dto.response.AuthResponse;
import com.clickkart.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 *
 * Endpoints (all PUBLIC — no JWT required):
 *   POST /api/auth/register  — create a new customer account and receive a JWT
 *   POST /api/auth/login     — authenticate and receive a JWT
 *
 * Delegates all business logic to AuthService.
 * Validation errors are handled by GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // =========================================================================
    // POST /api/auth/register
    // =========================================================================

    /**
     * Registers a new customer account.
     *
     * Request body:
     * <pre>
     * { "name": "Priya Sharma", "email": "priya@example.com", "password": "Secret@123" }
     * </pre>
     *
     * Success response (201):
     * <pre>
     * { "success": true, "message": "Registration successful", "data": { "token": "...", "email": "..." } }
     * </pre>
     *
     * @param request validated registration payload
     * @return 201 Created with JWT and user details
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Welcome to ClickKart!", response));
    }

    // =========================================================================
    // POST /api/auth/login
    // =========================================================================

    /**
     * Authenticates a user and issues a JWT.
     *
     * Request body:
     * <pre>
     * { "email": "priya@example.com", "password": "Secret@123" }
     * </pre>
     *
     * Success response (200):
     * <pre>
     * { "success": true, "message": "Login successful", "data": { "token": "Bearer ...", "userId": 2, ... } }
     * </pre>
     *
     * On bad credentials: GlobalExceptionHandler maps BadCredentialsException → 401.
     *
     * @param request validated login payload
     * @return 200 OK with JWT and user details
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
    }
}
