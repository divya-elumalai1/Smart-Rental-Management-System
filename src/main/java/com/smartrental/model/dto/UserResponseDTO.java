package com.smartrental.model.dto;

import com.smartrental.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for user response - excludes sensitive fields like password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String profileImageUrl;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean active;
    private Boolean twoFactorEnabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Get the user's full name.
     * @return concatenated first and last name
     */
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
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

    public boolean isTenant() {
        return role == Role.TENANT;
    }

    public boolean isOwner() {
        return role == Role.OWNER;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}