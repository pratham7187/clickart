/**
 * SECURITY LAYER — com.clickkart.security
 *
 * Contains the JWT infrastructure classes that plug into Spring Security.
 *
 * Files in this package:
 *
 *   JwtUtil.java
 *     - generateToken(UserDetails userDetails) → signed JWT string
 *     - validateToken(String token, UserDetails userDetails) → boolean
 *     - extractEmail(String token) → String
 *     - extractRole(String token) → String
 *     - extractExpiration(String token) → Date
 *     - isTokenExpired(String token) → boolean
 *     - Uses HS256 algorithm with a 256-bit SecretKey
 *
 *   JwtAuthFilter.java (extends OncePerRequestFilter)
 *     - Runs once per HTTP request before any controller code
 *     - Reads the "Authorization: Bearer <token>" header
 *     - Validates the token with JwtUtil
 *     - Sets Authentication in SecurityContextHolder
 *     - If token is missing or invalid: continues filter chain (request
 *       will be rejected by Spring Security's authorization checks)
 *
 *   UserDetailsServiceImpl.java (implements UserDetailsService)
 *     - loadUserByUsername(String email)
 *       → queries UserRepository.findByEmail()
 *       → wraps User entity in Spring's UserDetails
 *       → throws UsernameNotFoundException if not found or is_active=false
 */
package com.clickkart.security;
