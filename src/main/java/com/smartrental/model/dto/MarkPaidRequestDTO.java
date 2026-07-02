package com.smartrental.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for marking a payment as paid (manual or Razorpay).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkPaidRequestDTO {

    /** Unit number for owner API */
    private String unitNumber;

    /** Month for filtering (format: YYYY-MM) */
    private LocalDate month;

    /** UPI, Cash, Bank Transfer, Cheque, Razorpay */
    @NotBlank(message = "Payment mode is required")
    private String paymentMode;

    /** Transaction reference or Razorpay payment ID */
    private String reference;

    private LocalDate paymentDate;
    private String notes;

    /** Razorpay fields (optional for manual payments) */
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String receiptUrl;
}
