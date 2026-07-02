package com.smartrental.util;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.User;
import com.smartrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Helper to retrieve the currently authenticated user from the Spring Security
 * context. The JWT filter stores the authenticated principal as a Spring Security
 * {@link UserDetails} whose username is the user's email, so we resolve the
 * full {@link User} entity from the repository.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    /**
     * Get the email of the currently authenticated user.
     *
     * @return the authenticated user's email
     * @throws IllegalStateException if no authenticated user is present
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String email) {
            return email;
        }
        return authentication.getName();
    }

    /**
     * Get the currently authenticated user entity.
     *
     * @return the authenticated {@link User}
     * @throws ResourceNotFoundException if the user cannot be resolved
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    /**
     * Get the ID of the currently authenticated user.
     *
     * @return the authenticated user's UUID
     */
    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
