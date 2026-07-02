package com.smartrental.model.dto;

import com.smartrental.model.PropertyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating or updating a property.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequestDTO {

    @NotNull(message = "Landlord ID is required")
    private UUID landlordId;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent amount must be positive")
    private BigDecimal rentAmount;

    @Positive(message = "Deposit must be positive")
    private BigDecimal deposit;

    private Integer bedrooms;
    private BigDecimal bathrooms;
    private Integer areaSqft;
    private String furnishingStatus;

    @Size(max = 2000, message = "Amenities must not exceed 2000 characters")
    private String amenities;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private PropertyStatus status;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
}
