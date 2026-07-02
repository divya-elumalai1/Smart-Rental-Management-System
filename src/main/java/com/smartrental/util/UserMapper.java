package com.smartrental.util;

import com.smartrental.model.Role;
import com.smartrental.model.User;
import com.smartrental.model.dto.RegisterRequestDTO;
import com.smartrental.model.dto.UpdateProfileRequestDTO;
import com.smartrental.model.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for converting between User entity and DTOs.
 * Generates implementation at compile time for optimal performance.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {}
)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // ===========================================
    // Entity to Response DTO
    // ===========================================

    UserResponseDTO toResponseDTO(User user);

    // ===========================================
    // Request DTO to Entity (Registration)
    // ===========================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Set separately after encoding
    @Mapping(target = "role", source = "role")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth", qualifiedByName = "parseDate")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "phoneVerified", constant = "false")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "emailVerificationTokenExpiry", ignore = true)
    @Mapping(target = "resetToken", ignore = true)
    @Mapping(target = "resetTokenExpiry", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "refreshTokenExpiry", ignore = true)
    @Mapping(target = "twoFactorEnabled", constant = "false")
    @Mapping(target = "twoFactorSecret", ignore = true)
    @Mapping(target = "backupCodes", ignore = true)
    @Mapping(target = "profileImageUrl", ignore = true)
    User toEntity(RegisterRequestDTO dto);

    // ===========================================
    // Update Entity from Request DTO
    // ===========================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "phoneVerified", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "emailVerificationTokenExpiry", ignore = true)
    @Mapping(target = "resetToken", ignore = true)
    @Mapping(target = "resetTokenExpiry", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "refreshTokenExpiry", ignore = true)
    @Mapping(target = "twoFactorEnabled", ignore = true)
    @Mapping(target = "twoFactorSecret", ignore = true)
    @Mapping(target = "backupCodes", ignore = true)
    @Mapping(target = "dateOfBirth", source = "dateOfBirth", qualifiedByName = "parseDate")
    void updateEntityFromDTO(UpdateProfileRequestDTO dto, @MappingTarget User user);

    // ===========================================
    // Helper Methods
    // ===========================================

    @Named("parseDate")
    default java.time.LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }
}