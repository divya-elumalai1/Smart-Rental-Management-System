package com.smartrental.model.dto;

import com.smartrental.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for payment details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private UUID id;
    private UUID tenantId;
    private String tenantName;
    private UUID propertyId;
    private String propertyAddress;
    private UUID leaseId;
    private BigDecimal amount;
    private String currency;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private LocalDate dueDate;
    private LocalDate rentPeriod;
    private String receiptUrl;
    private String receiptNumber;
    private String paymentMode;
    private String unitNumber;
    private String notes;
    private Boolean overdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
