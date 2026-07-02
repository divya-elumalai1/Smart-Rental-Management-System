package com.smartrental.controller;

import com.smartrental.model.dto.TenantAssignRequestDTO;
import com.smartrental.model.dto.TenantSummaryDTO;
import com.smartrental.model.dto.TenantUpdateRequestDTO;
import com.smartrental.service.TenantService;
import com.smartrental.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {

    private final TenantService tenantService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<List<TenantSummaryDTO>> listTenants() {
        return ResponseEntity.ok(tenantService.listTenants(securityUtil.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<TenantSummaryDTO> assignTenant(@Valid @RequestBody TenantAssignRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.assignTenant(securityUtil.getCurrentUserId(), request));
    }

    @PutMapping("/{leaseId}")
    public ResponseEntity<TenantSummaryDTO> updateTenant(
            @PathVariable UUID leaseId,
            @Valid @RequestBody TenantUpdateRequestDTO request) {
        return ResponseEntity.ok(tenantService.updateTenant(securityUtil.getCurrentUserId(), leaseId, request));
    }

    @DeleteMapping("/{leaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTenant(@PathVariable UUID leaseId) {
        tenantService.removeTenant(securityUtil.getCurrentUserId(), leaseId);
    }
}
