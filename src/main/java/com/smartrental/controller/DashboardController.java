package com.smartrental.controller;

import com.smartrental.model.dto.DashboardResponseDTO;
import com.smartrental.model.dto.TenantDashboardResponseDTO;
import com.smartrental.model.dto.UnitDashboardDTO;
import com.smartrental.service.DashboardService;
import com.smartrental.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for dashboard summary statistics.
 */
@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard summary and unit cards")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityUtil securityUtil;

    /**
     * Get the dashboard summary for the currently authenticated landlord.
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardResponseDTO> getSummary() {
        log.debug("GET dashboard summary");
        return ResponseEntity.ok(dashboardService.getDashboardSummary(securityUtil.getCurrentUserId()));
    }

    /**
     * Get all unit cards for the owner dashboard (tenant + rent status per unit).
     */
    @GetMapping("/units")
    public ResponseEntity<List<UnitDashboardDTO>> getOwnerUnits() {
        log.debug("GET owner dashboard units");
        return ResponseEntity.ok(dashboardService.getOwnerUnits(securityUtil.getCurrentUserId()));
    }

    /**
     * Get the tenant dashboard — unit info, rent status, recent payments, maintenance.
     */
    @GetMapping("/tenant")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<TenantDashboardResponseDTO> getTenantDashboard() {
        log.debug("GET tenant dashboard");
        return ResponseEntity.ok(dashboardService.getTenantDashboard(securityUtil.getCurrentUserId()));
    }
}
