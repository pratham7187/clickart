package com.clickkart.security;

import com.clickkart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService implementation.
 * Loads a User entity from the database by email.
 * Since User implements UserDetails, no adapter/wrapper is needed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Called by Spring Security during authentication.
     * Username = email in ClickKart.
     *
     * @param email the login email
     * @return the User entity (which implements UserDetails)
     * @throws UsernameNotFoundException if no user with that email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });
    }
}
