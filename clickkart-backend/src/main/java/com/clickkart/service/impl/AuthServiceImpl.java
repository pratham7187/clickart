package com.clickkart.service.impl;

import com.clickkart.dto.request.LoginRequest;
import com.clickkart.dto.request.RegisterRequest;
import com.clickkart.dto.response.AuthResponse;
import com.clickkart.entity.User;
import com.clickkart.exception.DuplicateResourceException;
import com.clickkart.exception.ResourceNotFoundException;
import com.clickkart.repository.UserRepository;
import com.clickkart.security.JwtUtil;
import com.clickkart.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService.
 * Handles registration, login, and JWT issuance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * {@inheritDoc}
     *
     * Registers a new USER-role account.
     * Password is BCrypt-encoded before storage.
     * A JWT is issued immediately — user is logged in on registration.
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved);

        log.info("New user registered: {} (id={})", saved.getEmail(), saved.getId());
        return AuthResponse.of(token, saved.getId(), saved.getName(), saved.getEmail(), saved.getRole().name());
    }

    /**
     * {@inheritDoc}
     *
     * Delegates credential verification to AuthenticationManager.
     * AuthenticationManager calls UserDetailsServiceImpl.loadUserByUsername()
     * and then verifies the password with BCryptPasswordEncoder.
     * Throws BadCredentialsException on wrong credentials.
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String token = jwtUtil.generateToken(user);

        log.info("User logged in: {} (id={})", user.getEmail(), user.getId());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
