package com.smartrental.controller;

import com.smartrental.model.dto.MarkPaidRequestDTO;
import com.smartrental.model.dto.PaymentRequestDTO;
import com.smartrental.model.dto.PaymentResponseDTO;
import com.smartrental.service.PaymentService;
import com.smartrental.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for payment management.
 */
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtil securityUtil;

    /**
     * Get all payments.
     */
    @Operation(summary = "Get all payments", description = "Returns all payment records")
    @ApiResponse(responseCode = "200", description = "List of payments")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAll() {
        log.debug("GET all payments");
        return ResponseEntity.ok(paymentService.findAll());
    }

    /**
     * Get a payment by ID.
     */
    @Operation(summary = "Get payment by ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Payment found"), @ApiResponse(responseCode = "404", description = "Payment not found")})
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable @Parameter(description = "Payment ID") UUID id) {
        log.debug("GET payment {}", id);
        return ResponseEntity.ok(paymentService.getById(id));
    }

    /**
     * Get all payments for the currently authenticated tenant (most recent first).
     */
    @Operation(summary = "Get my payments", description = "Returns payments for the currently authenticated tenant")
    @GetMapping("/tenant/me")
    public ResponseEntity<List<PaymentResponseDTO>> getMyPayments() {
        log.debug("GET payments for current tenant");
        return ResponseEntity.ok(paymentService.findByTenant(securityUtil.getCurrentUserId()));
    }

    /**
     * Get all payments for a property.
     */
    @Operation(summary = "Get payments by property")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<PaymentResponseDTO>> getByProperty(@PathVariable @Parameter(description = "Property ID") UUID propertyId) {
        log.debug("GET payments for property {}", propertyId);
        return ResponseEntity.ok(paymentService.findByProperty(propertyId));
    }

    /**
     * Get the total pending dues for the currently authenticated tenant.
     */
    @Operation(summary = "Get my pending dues", description = "Returns the sum of pending + overdue amounts for the current tenant")
    @GetMapping("/tenant/me/dues")
    public ResponseEntity<BigDecimal> getMyPendingDues() {
        log.debug("GET pending dues for current tenant");
        return ResponseEntity.ok(paymentService.calculatePendingDues(securityUtil.getCurrentUserId()));
    }

    /**
     * Create a new payment.
     */
    @Operation(summary = "Create a payment", description = "Creates a new PENDING payment record")
    @ApiResponse(responseCode = "201", description = "Payment created")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> create(@Valid @RequestBody PaymentRequestDTO request) {
        log.info("POST create payment");
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request));
    }

    /**
     * Mark an existing payment as paid.
     */
    @Operation(summary = "Mark payment as paid", description = "Marks a PENDING payment as COMPLETED")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<PaymentResponseDTO> markAsPaid(@PathVariable @Parameter(description = "Payment ID") UUID id,
                                                          @Valid @RequestBody MarkPaidRequestDTO request) {
        log.info("PUT mark payment {} as paid", id);
        return ResponseEntity.ok(paymentService.markAsPaid(id, request));
    }

    /**
     * Delete (soft-delete) a payment.
     */
    @Operation(summary = "Delete payment", description = "Soft-deletes a payment record")
    @ApiResponse(responseCode = "204", description = "Payment deleted")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Parameter(description = "Payment ID") UUID id) {
        log.info("DELETE payment {}", id);
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get receipt data for a payment.
     * Returns payment details as JSON (PDF generation is not yet implemented).
     */
    @Operation(summary = "Get payment receipt", description = "Returns payment details as JSON receipt")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping("/{id}/receipt")
    public ResponseEntity<PaymentResponseDTO> getReceipt(@PathVariable @Parameter(description = "Payment ID") UUID id) {
        log.debug("GET receipt for payment {}", id);
        return ResponseEntity.ok(paymentService.getById(id));
    }

    /**
     * Export all payments as CSV.
     */
    @Operation(summary = "Export payments CSV", description = "Exports all payments as a CSV file")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCsv() {
        log.debug("GET payments CSV export");
        List<PaymentResponseDTO> payments = paymentService.findAll();
        StringBuilder csv = new StringBuilder("Tenant,Unit,Amount,Date,Mode,Status,Receipt\n");
        for (var p : payments) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                escapeCsv(p.getTenantName()),
                escapeCsv(p.getUnitNumber()),
                p.getAmount(),
                p.getPaymentDate() != null ? p.getPaymentDate().toString() : "",
                escapeCsv(p.getPaymentMode()),
                p.getStatus(),
                escapeCsv(p.getReceiptNumber())
            ));
        }
        return ResponseEntity.ok()
            .header("Content-Type", "text/csv")
            .header("Content-Disposition", "attachment; filename=payments-export.csv")
            .body(csv.toString());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
