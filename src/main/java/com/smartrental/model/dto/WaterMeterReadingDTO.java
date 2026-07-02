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
public class WaterMeterReadingDTO {
    private UUID id;
    private UUID propertyId;
    private String unitNumber;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private BigDecimal unitsConsumed;
    private BigDecimal waterBill;
    private BigDecimal totalBill;
    private LocalDate readingDate;
    private String meterPhotoUrl;
    private String notes;
}
