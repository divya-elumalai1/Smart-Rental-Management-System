package com.smartrental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * User entity representing both Tenants and Landlords in the system.
 * Implements UserDetails for Spring Security integration.
 * Supports soft delete via @SQLDelete and @Where annotations.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_phone", columnNames = "phone_number")
    },
    indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_deleted", columnList = "is_deleted"),
        @Index(name = "idx_users_created_at", columnList = "created_at")
    }
)
@SQLDelete(sql = "UPDATE users SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"password", "resetToken", "resetTokenExpiry"})
public class User extends BaseEntity<UUID> implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid (E.164 format)")
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "email_verification_token", length = 500)
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;

    @Column(name = "reset_token", length = 500)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;

    @Column(name = "refresh_token_expiry")
    private LocalDateTime refreshTokenExpiry;

    // Two-factor authentication fields
    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret", length = 100)
    private String twoFactorSecret;

    @Column(name = "backup_codes", length = 1000)
    private String backupCodes; // JSON array of backup codes

    // ===========================================
    // UserDetails Implementation
    // ===========================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active != null && active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active != null && active && emailVerified != null && emailVerified;
    }

    // ===========================================
    // Helper Methods
    // ===========================================

    /**
     * Get the user's full name.
     * @return concatenated first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if the user is a tenant.
     * @return true if role is TENANT
     */
    public boolean isTenant() {
        return role == Role.TENANT;
    }

    /**
     * Check if the user is an owner.
     * @return true if role is OWNER
     */
    public boolean isOwner() {
        return role == Role.OWNER;
    }

    /**
     * Check if the user is an admin.
     * @return true if role is ADMIN
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Get the formatted address string.
     * @return formatted address
     */
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (city != null) sb.append(sb.length() > 0 ? ", " : "").append(city);
        if (state != null) sb.append(sb.length() > 0 ? ", " : "").append(state);
        if (postalCode != null) sb.append(sb.length() > 0 ? " " : "").append(postalCode);
        if (country != null) sb.append(sb.length() > 0 ? ", " : "").append(country);
        return sb.toString();
    }

    /**
     * Clear sensitive authentication tokens.
     */
    public void clearAuthTokens() {
        this.resetToken = null;
        this.resetTokenExpiry = null;
        this.emailVerificationToken = null;
        this.emailVerificationTokenExpiry = null;
        this.refreshToken = null;
        this.refreshTokenExpiry = null;
    }
}