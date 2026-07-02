package com.smartrental.model.dto;

import com.smartrental.model.PropertyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for property details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponseDTO {

    private UUID id;
    private UUID landlordId;
    private String landlordName;
    private String address;
    private String unitNumber;
    private String floorLabel;
    private String city;
    private String state;
    private String postalCode;
    private BigDecimal rentAmount;
    private BigDecimal deposit;
    private Integer bedrooms;
    private BigDecimal bathrooms;
    private Integer areaSqft;
    private String furnishingStatus;
    private String amenities;
    private String description;
    private PropertyStatus status;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
