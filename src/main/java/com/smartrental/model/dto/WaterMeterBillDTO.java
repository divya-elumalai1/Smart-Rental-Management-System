package com.smartrental.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterMeterBillDTO {
    private String unitNumber;
    private BigDecimal rentAmount;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private BigDecimal unitsConsumed;
    private BigDecimal waterRate;
    private BigDecimal waterBill;
    private BigDecimal totalBill;
    private LocalDate billDate;
}
