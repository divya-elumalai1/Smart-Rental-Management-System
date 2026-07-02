package com.smartrental.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDashboardResponseDTO {
    private String firstName;
    private String unitNumber;
    private String floorLabel;
    private BigDecimal rentAmount;
    private BigDecimal deposit;
    private LocalDate dueDate;
    private String rentStatus;
    private List<PaymentResponseDTO> recentPayments;
    private List<MaintenanceResponseDTO> recentMaintenance;
}
