package com.smartrental.security;

import com.smartrental.model.User;
import com.smartrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService implementation.
 * Loads user by email for authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // Check if user is active
        if (!user.getActive()) {
            log.warn("User account is disabled: {}", email);
            throw new UsernameNotFoundException("User account is disabled");
        }

        // Check if email is verified
        if (!user.getEmailVerified()) {
            log.warn("User email not verified: {}", email);
            throw new UsernameNotFoundException("Email not verified");
        }

        log.debug("Successfully loaded user: {} with role: {}", email, user.getRole());
        return user;
    }
}