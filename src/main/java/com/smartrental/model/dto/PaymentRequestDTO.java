package com.smartrental.model.dto;

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
 * Request DTO for creating a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    private UUID leaseId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Builder.Default
    private String currency = "INR";

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private LocalDate rentPeriod;
    private String notes;
}
