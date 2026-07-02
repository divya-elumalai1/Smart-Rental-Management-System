package com.smartrental.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSummaryDTO {

    private UUID leaseId;
    private UUID tenantId;
    private UUID propertyId;
    private String unitNumber;
    private String floorLabel;
    private String tenantName;
    private String email;
    private String phoneNumber;
    private BigDecimal rentAmount;
    private BigDecimal deposit;
    private LocalDate leaseStart;
    private LocalDate leaseEnd;
    private String rentStatus;
}
