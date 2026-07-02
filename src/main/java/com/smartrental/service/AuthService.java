package com.smartrental.service;

import com.smartrental.model.Role;
import com.smartrental.model.User;
import com.smartrental.model.dto.AuthResponseDTO;
import com.smartrental.model.dto.LoginRequestDTO;
import com.smartrental.model.dto.RegisterRequestDTO;
import com.smartrental.model.dto.UserResponseDTO;
import com.smartrental.repository.UserRepository;
import com.smartrental.security.JwtUtil;
import com.smartrental.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication service handling user registration, login, token management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final EmailService emailService;

    // ===========================================
    // Registration
    // ===========================================

    /**
     * Register a new user (tenant or landlord).
     * @param request registration request DTO
     * @return AuthResponseDTO with tokens and user info
     */
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("Registering new user with email: {} and role: {}", request.getEmail(), request.getRole());

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        // Create user entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));

        // Generate email verification token
        String verificationToken = jwtUtil.generateEmailVerificationToken(user, 1440); // 24 hours
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Send verification email (async)
        emailService.sendVerificationEmail(savedUser, verificationToken);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);

        // Save refresh token
        savedUser.setRefreshToken(refreshToken);
        savedUser.setRefreshTokenExpiry(LocalDateTime.now().plusNanos(jwtUtil.getRefreshExpiration() * 1_000_000L));
        userRepository.save(savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    // ===========================================
    // Login
    // ===========================================

    @Value("${app.auth.skip-email-verification:false}")
    private boolean skipEmailVerification;

    /**
     * Authenticate user and generate tokens.
     * @param request login request DTO
     * @return AuthResponseDTO with tokens and user info
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate using Spring Security
        // Trim credentials to prevent accidental leading/trailing spaces from breaking login
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().trim(), request.getPassword().trim())
            );
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = (User) authentication.getPrincipal();

        // Check if user is active
        if (!user.getActive()) {
            throw new IllegalStateException("Account is deactivated. Contact support.");
        }

        // Check if email is verified (can be skipped in dev)
        if (!skipEmailVerification && !user.getEmailVerified()) {
            throw new IllegalStateException("Email not verified. Please verify your email first.");
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());

        // Generate new tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Save refresh token
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusNanos(jwtUtil.getRefreshExpiration() * 1_000_000L));
        userRepository.save(user);

        log.info("User logged in successfully: {}", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ===========================================
    // Token Refresh
    // ===========================================

    /**
     * Refresh access token using refresh token.
     * @param refreshToken the refresh token
     * @return new AuthResponseDTO with new tokens
     */
    public AuthResponseDTO refreshToken(String refreshToken) {
        log.debug("Refreshing access token");

        // Validate refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new IllegalArgumentException("Invalid token type");
        }

        // Find user by refresh token
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        // Check if refresh token matches and is not expired
        if (!refreshToken.equals(user.getRefreshToken()) ||
                user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        // Update refresh token
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusNanos(jwtUtil.getRefreshExpiration() * 1_000_000L));
        userRepository.save(user);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    // ===========================================
    // Logout
    // ===========================================

    /**
     * Logout user by invalidating refresh token.
     * @param userId user ID
     */
    public void logout(UUID userId) {
        log.info("Logging out user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }

    // ===========================================
    // Email Verification
    // ===========================================

    /**
     * Verify user's email with token.
     * @param token verification token
     * @return true if successful
     */
    public boolean verifyEmail(String token) {
        log.info("Verifying email with token");

        if (!jwtUtil.validateToken(token) || !"email_verification".equals(jwtUtil.getTokenType(token))) {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if token matches
        if (!token.equals(user.getEmailVerificationToken())) {
            throw new IllegalArgumentException("Invalid verification token");
        }

        // Check expiry
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token expired");
        }

        // Verify email
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", email);
        return true;
    }

    /**
     * Resend email verification token.
     * @param email user email
     */
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || user.getEmailVerified()) {
            log.warn("Resend verification skipped for email: {} (not found or already verified)", email);
            return;
        }

        // Generate new token
        String verificationToken = jwtUtil.generateEmailVerificationToken(user, 1440);
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Send email
        emailService.sendVerificationEmail(user, verificationToken);
    }

    // ===========================================
    // Password Reset
    // ===========================================

    /**
     * Request password reset (send reset email).
     * @param email user email
     */
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", email);
            return;
        }

        // Generate reset token (1 hour expiry)
        String resetToken = jwtUtil.generatePasswordResetToken(user, 60);
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Send reset email
        emailService.sendPasswordResetEmail(user, resetToken);
    }

    /**
     * Reset password with token.
     * @param token reset token
     * @param newPassword new password
     * @return true if successful
     */
    public boolean resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");

        if (!jwtUtil.validateToken(token) || !"password_reset".equals(jwtUtil.getTokenType(token))) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if token matches
        if (!token.equals(user.getResetToken())) {
            throw new IllegalArgumentException("Invalid reset token");
        }

        // Check expiry
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        // Invalidate refresh token for security
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", email);
        return true;
    }

    // ===========================================
    // Change Password (Authenticated)
    // ===========================================

    /**
     * Change password for authenticated user.
     * @param userId user ID
     * @param currentPassword current password
     * @param newPassword new password
     */
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        // Invalidate refresh token for security
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    // ===========================================
    // Helpers
    // ===========================================

    private AuthResponseDTO buildAuthResponse(User user, String accessToken, String refreshToken) {
        UserResponseDTO userDTO = userMapper.toResponseDTO(user);
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .user(userDTO)
                .build();
    }
}