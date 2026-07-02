package com.smartrental.model.dto;

import com.smartrental.model.PaymentStatus;
import com.smartrental.model.PropertyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dashboard unit card — property + active tenant + current rent payment status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitDashboardDTO {

    private UUID id;
    private String unitNumber;
    private String floorLabel;
    private String type;
    private BigDecimal rentAmount;
    private BigDecimal deposit;
    private PropertyStatus propertyStatus;

    /** PAID | PENDING | OVERDUE | VACANT | UNDER_CONSTRUCTION */
    private String rentStatus;

    private UUID tenantId;
    private String tenantName;
    private String tenantPhone;
    private LocalDate leaseStart;

    private UUID currentPaymentId;
    private PaymentStatus paymentStatus;
    private LocalDate dueDate;
}
