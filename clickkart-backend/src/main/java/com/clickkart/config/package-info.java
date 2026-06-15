/**
 * CONFIGURATION LAYER — com.clickkart.config
 *
 * Contains all Spring configuration classes. These are @Configuration beans
 * that wire up the security filter chain, CORS policy, and JWT constants.
 *
 * Files in this package:
 *
 *   SecurityConfig.java
 *     - Defines the Spring Security filter chain (@Bean SecurityFilterChain)
 *     - Sets which endpoints are public vs. authenticated vs. ADMIN-only
 *     - Registers JwtAuthFilter before UsernamePasswordAuthenticationFilter
 *     - Configures stateless session (SessionCreationPolicy.STATELESS)
 *     - Disables CSRF (not needed for stateless JWT APIs)
 *     - Declares BCryptPasswordEncoder @Bean (strength = 12)
 *     - Declares AuthenticationManager @Bean
 *
 *   CorsConfig.java
 *     - Implements WebMvcConfigurer
 *     - Reads app.cors.allowed-origins from application.properties
 *     - Allows GET, POST, PUT, PATCH, DELETE methods
 *     - Allows Authorization, Content-Type headers
 *     - Exposes Authorization header in responses
 *
 *   JwtConfig.java (or constants in JwtUtil)
 *     - Reads app.jwt.secret and app.jwt.expiration-ms via @Value
 *     - Provides a SecretKey @Bean used by JwtUtil
 */
package com.clickkart.config;
