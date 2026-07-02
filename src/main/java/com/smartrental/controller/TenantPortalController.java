package com.smartrental.controller;

import com.smartrental.model.dto.MaintenanceRequestDTO;
import com.smartrental.model.dto.MaintenanceResponseDTO;
import com.smartrental.model.dto.PaymentResponseDTO;
import com.smartrental.model.dto.PropertyResponseDTO;
import com.smartrental.model.dto.WaterMeterBillDTO;
import com.smartrental.model.dto.WaterMeterReadingDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.smartrental.service.MaintenanceService;
import jakarta.validation.Valid;
import com.smartrental.service.PaymentService;
import com.smartrental.service.PropertyService;
import com.smartrental.service.WaterMeterService;
import com.smartrental.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Tenant-specific portal operations.
 * Provides endpoints for tenants to view their unit, payments, bills, and raise maintenance requests.
 */
@RestController
@RequestMapping("/v1/tenant")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TENANT')")
@Tag(name = "Tenant Portal", description = "Tenant-specific portal endpoints")
public class TenantPortalController {

    private final PropertyService propertyService;
    private final PaymentService paymentService;
    private final MaintenanceService maintenanceService;
    private final WaterMeterService waterMeterService;
    private final SecurityUtil securityUtil;

    /**
     * Get tenant's own unit details.
     * GET /api/v1/tenant/my-unit
     */
    @GetMapping("/my-unit")
    public ResponseEntity<PropertyResponseDTO> getMyUnit() {
        log.debug("GET my unit for tenant: {}", securityUtil.getCurrentUserId());
        // Find the property where this tenant has an active lease
        // This requires a custom query - for now, return the first property
        // In production, you'd query by tenant's active lease
        return ResponseEntity.ok(propertyService.findByTenantId(securityUtil.getCurrentUserId()));
    }

    /**
     * Get tenant's own payment history.
     * GET /api/v1/tenant/my-payments
     */
    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentResponseDTO>> getMyPayments() {
        log.debug("GET my payments for tenant: {}", securityUtil.getCurrentUserId());
        return ResponseEntity.ok(paymentService.findByTenant(securityUtil.getCurrentUserId()));
    }

    /**
     * Get tenant's bill for a specific month.
     * GET /api/v1/tenant/my-bill/{month}
     */
    @GetMapping("/my-bill/{month}")
    public ResponseEntity<WaterMeterBillDTO> getMyBill(@PathVariable String month) {
        log.debug("GET my bill for month: {}", month);
        // Parse month (format: YYYY-MM)
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate billDate = yearMonth.atDay(1);
        
        // Get tenant's unit
        PropertyResponseDTO property = propertyService.findByTenantId(securityUtil.getCurrentUserId());
        
        // Get the latest meter reading for current reading
        java.util.List<WaterMeterReadingDTO> readings = waterMeterService.getReadingsByUnit(property.getUnitNumber());
        BigDecimal currentReading = readings.isEmpty()
            ? BigDecimal.ZERO
            : readings.get(0).getCurrentReading();
        
        // Calculate bill with actual reading
        WaterMeterBillDTO bill = waterMeterService.calculateWaterBill(
            property.getUnitNumber(), 
            currentReading
        );
        
        return ResponseEntity.ok(bill);
    }

    /**
     * Raise a maintenance request.
     * POST /api/v1/tenant/maintenance
     */
    @PostMapping("/maintenance")
    public ResponseEntity<MaintenanceResponseDTO> raiseMaintenanceRequest(
            @Valid @RequestBody MaintenanceRequestDTO request) {
        UUID userId = securityUtil.getCurrentUserId();
        log.info("POST raise maintenance request from tenant: {}", userId);
        request.setTenantId(userId);
        if (request.getPropertyId() == null) {
            PropertyResponseDTO property = propertyService.findByTenantId(userId);
            request.setPropertyId(property.getId());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(maintenanceService.create(request));
    }
}
