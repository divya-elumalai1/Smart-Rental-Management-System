package com.smartrental.model.dto;

import com.smartrental.model.LeaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for lease details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseResponseDTO {

    private UUID id;
    private UUID tenantId;
    private String tenantName;
    private UUID propertyId;
    private String propertyAddress;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private LeaseStatus status;
    private String leaseDocumentUrl;
    private String termsAndConditions;
    private Boolean currentlyActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
