package com.smartrental.repository;

import com.smartrental.model.Role;
import com.smartrental.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity.
 * Provides custom queries for authentication and user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ===========================================
    // Authentication Queries
    // ===========================================

    /**
     * Find user by email (case-insensitive).
     * Used for login authentication.
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if email exists (case-insensitive).
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find user by phone number.
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Check if phone number exists.
     */
    boolean existsByPhoneNumber(String phoneNumber);

    // ===========================================
    // Token-based Queries
    // ===========================================

    /**
     * Find user by email verification token.
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Find user by password reset token.
     */
    Optional<User> findByResetToken(String token);

    /**
     * Find user by refresh token.
     */
    Optional<User> findByRefreshToken(String refreshToken);

    // ===========================================
    // Role-based Queries
    // ===========================================

    /**
     * Find all users by role.
     */
    List<User> findByRole(Role role);

    /**
     * Find paginated users by role.
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Find all active users by role.
     */
    List<User> findByRoleAndActiveTrue(Role role);

    /**
     * Find paginated active users by role.
     */
    Page<User> findByRoleAndActiveTrue(Role role, Pageable pageable);

    // ===========================================
    // Status-based Queries
    // ===========================================

    /**
     * Find all active users.
     */
    List<User> findByActiveTrue();

    /**
     * Find paginated active users.
     */
    Page<User> findByActiveTrue(Pageable pageable);

    /**
     * Find users by active status.
     */
    List<User> findByActive(boolean active);

    // ===========================================
    // Search Queries
    // ===========================================

    /**
     * Search users by name or email (case-insensitive).
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.deleted = false")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    /**
     * Search tenants by name or email.
     */
    @Query("SELECT u FROM User u WHERE u.role = com.smartrental.model.Role.TENANT " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.deleted = false")
    Page<User> searchTenants(@Param("search") String search, Pageable pageable);

    /**
     * Search owners by name or email.
     */
    @Query("SELECT u FROM User u WHERE u.role = com.smartrental.model.Role.OWNER " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.deleted = false")
    Page<User> searchOwners(@Param("search") String search, Pageable pageable);

    // ===========================================
    // Statistics Queries
    // ===========================================

    /**
     * Count total users by role.
     */
    long countByRole(Role role);

    /**
     * Count active users by role.
     */
    long countByRoleAndActiveTrue(Role role);

    /**
     * Count users created after a specific date.
     */
    long countByCreatedAtAfter(LocalDateTime date);

    /**
     * Count users by role created after a specific date.
     */
    long countByRoleAndCreatedAtAfter(Role role, LocalDateTime date);

    // ===========================================
    // Verification Queries
    // ===========================================

    /**
     * Find unverified users (email not verified).
     */
    List<User> findByEmailVerifiedFalse();

    /**
     * Find users with expired email verification tokens.
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationTokenExpiry < CURRENT_TIMESTAMP " +
           "AND u.emailVerified = false AND u.deleted = false")
    List<User> findUsersWithExpiredEmailVerification();

    /**
     * Find users with expired password reset tokens.
     */
    @Query("SELECT u FROM User u WHERE u.resetTokenExpiry < CURRENT_TIMESTAMP " +
           "AND u.resetToken IS NOT NULL AND u.deleted = false")
    List<User> findUsersWithExpiredResetTokens();

    // ===========================================
    // Cleanup Queries
    // ===========================================

    /**
     * Delete expired verification tokens (set to null).
     * Used for scheduled cleanup tasks.
     */
    @Query("UPDATE User u SET u.emailVerificationToken = null, u.emailVerificationTokenExpiry = null " +
           "WHERE u.emailVerificationTokenExpiry < CURRENT_TIMESTAMP AND u.deleted = false")
    int clearExpiredEmailVerificationTokens();

    /**
     * Delete expired reset tokens (set to null).
     */
    @Query("UPDATE User u SET u.resetToken = null, u.resetTokenExpiry = null " +
           "WHERE u.resetTokenExpiry < CURRENT_TIMESTAMP AND u.deleted = false")
    int clearExpiredResetTokens();

    /**
     * Delete expired refresh tokens (set to null).
     */
    @Query("UPDATE User u SET u.refreshToken = null, u.refreshTokenExpiry = null " +
           "WHERE u.refreshTokenExpiry < CURRENT_TIMESTAMP AND u.deleted = false")
    int clearExpiredRefreshTokens();
}