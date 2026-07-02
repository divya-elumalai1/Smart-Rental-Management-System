package com.smartrental.controller;

import com.smartrental.model.dto.AuthResponseDTO;
import com.smartrental.model.dto.ChangePasswordRequestDTO;
import com.smartrental.model.dto.ForgotPasswordRequestDTO;
import com.smartrental.model.dto.LoginRequestDTO;
import com.smartrental.model.dto.RegisterRequestDTO;
import com.smartrental.model.dto.ResetPasswordRequestDTO;
import com.smartrental.model.dto.UserResponseDTO;
import com.smartrental.model.dto.VerifyEmailRequestDTO;
import com.smartrental.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Authentication Controller for register, login, token management, and password operations.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // ===========================================
    // Registration
    // ===========================================

    /**
     * Register a new user (tenant or landlord).
     * @param request registration details
     * @return AuthResponseDTO with tokens and user info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("Registration request for email: {} role: {}", request.getEmail(), request.getRole());
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ===========================================
    // Login
    // ===========================================

    /**
     * Authenticate user and return JWT tokens.
     * @param request login credentials
     * @return AuthResponseDTO with tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ===========================================
    // Token Refresh
    // ===========================================

    /**
     * Refresh access token using refresh token.
     * @param request refresh token request
     * @return new AuthResponseDTO with new tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody com.smartrental.model.dto.RefreshTokenRequestDTO request) {
        log.debug("Token refresh request");
        AuthResponseDTO response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // ===========================================
    // Logout
    // ===========================================

    /**
     * Logout user (invalidate refresh token).
     * @param authentication current authentication
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        log.info("Logout request for user: {}", authentication.getName());

        if (authentication.getPrincipal() instanceof com.smartrental.model.User user) {
            authService.logout(user.getId());
            log.info("User logged out: {}", user.getEmail());
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ===========================================
    // Email Verification
    // ===========================================

    /**
     * Verify email with token from email link.
     * @param request verification token
     * @return success message
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequestDTO request) {
        log.info("Email verification request");
        authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    /**
     * Resend verification email.
     * @param request email request
     * @return success message
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("Resend verification email for: {}", request.getEmail());
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    // ===========================================
    // Password Reset
    // ===========================================

    /**
     * Request password reset (send reset email).
     * @param request email request
     * @return success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("Forgot password request for: {}", request.getEmail());
        authService.requestPasswordReset(request.getEmail());
        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent"));
    }

    /**
     * Reset password with token from email link.
     * @param request reset token and new password
     * @return success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        log.info("Password reset request");
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // ===========================================
    // Change Password (Authenticated)
    // ===========================================

    /**
     * Change password for authenticated user.
     * @param request current and new password
     * @param authentication current authentication
     * @return success message
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            Authentication authentication) {

        log.info("Change password request for user: {}", authentication.getName());

        // Get the authenticated user entity
        if (authentication.getPrincipal() instanceof com.smartrental.model.User user) {
            authService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
            log.info("Password changed successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unable to authenticate user"));
    }

    // ===========================================
    // Current User Info
    // ===========================================

    /**
     * Get current authenticated user's profile.
     * @param userDetails authenticated user details
     * @return UserResponseDTO
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Get current user request for: {}", userDetails.getUsername());

        // The UserDetails is our User entity (since User implements UserDetails)
        if (userDetails instanceof com.smartrental.model.User user) {
            return ResponseEntity.ok(com.smartrental.util.UserMapper.INSTANCE.toResponseDTO(user));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // ===========================================
    // Health Check
    // ===========================================

    /**
     * Health check endpoint for auth service.
     * @return status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "auth"));
    }
}