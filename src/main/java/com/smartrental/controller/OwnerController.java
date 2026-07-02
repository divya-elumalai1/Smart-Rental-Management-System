package com.smartrental.controller;

import com.smartrental.model.dto.DashboardResponseDTO;
import com.smartrental.model.dto.MarkPaidRequestDTO;
import com.smartrental.model.dto.PaymentResponseDTO;
import com.smartrental.model.dto.ResetTenantPasswordRequestDTO;
import com.smartrental.model.dto.TenantAssignRequestDTO;
import com.smartrental.model.dto.TenantSummaryDTO;
import com.smartrental.model.dto.UnitDashboardDTO;
import com.smartrental.service.DashboardService;
import com.smartrental.service.PaymentService;
import com.smartrental.service.TenantService;
import com.smartrental.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Owner-specific operations.
 * Provides endpoints for managing tenants, payments, and dashboard stats.
 */
@RestController
@RequestMapping("/v1/owner")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
@Tag(name = "Owner", description = "Owner-specific operations: tenants, payments, dashboard")
public class OwnerController {

    private final TenantService tenantService;
    private final PaymentService paymentService;
    private final DashboardService dashboardService;
    private final SecurityUtil securityUtil;

    /**
     * Get all units with tenant details and payment status.
     * GET /api/v1/owner/units
     */
    @GetMapping("/units")
    public ResponseEntity<List<UnitDashboardDTO>> getUnits() {
        log.debug("GET owner units");
        return ResponseEntity.ok(dashboardService.getOwnerUnits(securityUtil.getCurrentUserId()));
    }

    /**
     * Add new tenant to a vacant unit.
     * POST /api/v1/owner/tenants
     */
    @PostMapping("/tenants")
    public ResponseEntity<TenantSummaryDTO> addTenant(@Valid @RequestBody TenantAssignRequestDTO request) {
        log.info("POST add tenant to unit: {}", request.getUnitNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.assignTenant(securityUtil.getCurrentUserId(), request));
    }

    /**
     * Edit existing tenant details by unit number.
     * PUT /api/v1/owner/tenants/{unitNumber}
     */
    @PutMapping("/tenants/{unitNumber}")
    public ResponseEntity<TenantSummaryDTO> editTenant(
            @PathVariable String unitNumber,
            @Valid @RequestBody com.smartrental.model.dto.TenantUpdateRequestDTO request) {
        log.info("PUT edit tenant for unit: {}", unitNumber);
        // Find lease by unit number and update
        // This requires finding the lease first
        return ResponseEntity.ok(tenantService.updateTenantByUnitNumber(securityUtil.getCurrentUserId(), unitNumber, request));
    }

    /**
     * Remove tenant from unit.
     * DELETE /api/v1/owner/tenants/{unitNumber}
     */
    @DeleteMapping("/tenants/{unitNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTenant(@PathVariable String unitNumber) {
        log.info("DELETE tenant from unit: {}", unitNumber);
        tenantService.removeTenantByUnitNumber(securityUtil.getCurrentUserId(), unitNumber);
    }

    /**
     * Reset tenant's password.
     * POST /api/v1/owner/tenants/reset-password
     */
    @PostMapping("/tenants/reset-password")
    public ResponseEntity<Map<String, String>> resetTenantPassword(
            @Valid @RequestBody ResetTenantPasswordRequestDTO request) {
        log.info("POST reset password for tenant in unit: {}", request.getUnitNumber());
        tenantService.resetTenantPassword(securityUtil.getCurrentUserId(), request.getUnitNumber(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    /**
     * Mark rent as paid for a unit.
     * POST /api/v1/payments/mark-paid
     */
    @PostMapping("/payments/mark-paid")
    public ResponseEntity<PaymentResponseDTO> markPaid(@Valid @RequestBody MarkPaidRequestDTO request) {
        log.info("POST mark rent as paid for unit: {}", request.getUnitNumber());
        return ResponseEntity.ok(paymentService.markPaidByUnitNumber(request));
    }

    /**
     * Delete a payment by ID.
     * DELETE /api/v1/owner/payments/{id}
     */
    @DeleteMapping("/payments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayment(@PathVariable UUID id) {
        log.info("DELETE payment {}", id);
        paymentService.deletePayment(id);
    }

    /**
     * Get all payment history with filters.
     * GET /api/v1/payments/owner/all
     */
    @GetMapping("/payments/all")
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String unitNumber) {
        log.debug("GET all payments with filters - month: {}, unit: {}", month, unitNumber);
        return ResponseEntity.ok(paymentService.findAllWithFilters(securityUtil.getCurrentUserId(), month, unitNumber));
    }

    /**
     * Get dashboard statistics.
     * GET /api/v1/owner/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardResponseDTO> getDashboardStats() {
        log.debug("GET dashboard stats");
        return ResponseEntity.ok(dashboardService.getDashboardSummary(securityUtil.getCurrentUserId()));
    }
}
