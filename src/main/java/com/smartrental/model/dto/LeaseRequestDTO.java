package com.smartrental.model.dto;

import com.smartrental.model.LeaseStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating or updating a lease.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseRequestDTO {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent amount must be positive")
    private BigDecimal rentAmount;

    @Positive(message = "Deposit amount must be positive")
    private BigDecimal depositAmount;

    private LeaseStatus status;
    private String leaseDocumentUrl;
    private String termsAndConditions;
}
