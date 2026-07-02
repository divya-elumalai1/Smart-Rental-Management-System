package com.smartrental.security;

import com.smartrental.model.User;
import com.smartrental.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT Authentication Filter that intercepts requests and validates JWT tokens.
 * Extends OncePerRequestFilter to ensure single execution per request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // Paths that should be excluded from JWT filtering
    // Note: /auth/me and /auth/logout are NOT excluded - they need JWT auth
    private static final String[] EXCLUDED_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/auth/health",
            "/api/public/",
            "/api/health",
            "/actuator/",
            "/swagger-ui",
            "/api-docs",
            "/v3/api-docs",
            "/error",
            "/favicon.ico"
    };

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Skip filter for excluded paths
            if (shouldNotFilter(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = jwtUtil.extractToken(request);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // Check if it's an access token
                if ("access".equals(jwtUtil.getTokenType(token))) {
                    String email = jwtUtil.getEmailFromToken(token);

                    // Fetch user from database to ensure they still exist and are active
                    Optional<User> userOptional = userRepository.findByEmailIgnoreCase(email);

                    if (userOptional.isPresent()) {
                        User user = userOptional.get();

                        if (user.getActive() && user.getEmailVerified()) {
                            // Use the actual User entity as the principal
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user, token, user.getAuthorities());

                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request)
                            );

                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.debug("Authenticated user: {} with role: {}", email, user.getRole());
                        } else {
                            log.debug("User not active or email not verified: {}", email);
                        }
                    } else {
                        log.debug("User not found for email: {}", email);
                    }
                } else {
                    log.debug("Invalid token type for authentication: {}", jwtUtil.getTokenType(token));
                }
            } else if (StringUtils.hasText(token)) {
                log.debug("Invalid or expired JWT token");
            }
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage(), e);
            // Don't block the request, let Spring Security handle unauthorized access
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return java.util.Arrays.stream(EXCLUDED_PATHS)
                .anyMatch(path::startsWith);
    }
}