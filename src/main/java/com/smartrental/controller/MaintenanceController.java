package com.smartrental.controller;

import com.smartrental.model.MaintenanceStatus;
import com.smartrental.model.dto.MaintenanceCommentDTO;
import com.smartrental.model.dto.MaintenanceRequestDTO;
import com.smartrental.model.dto.MaintenanceResponseDTO;
import com.smartrental.service.MaintenanceService;
import com.smartrental.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for maintenance request management.
 */
@RestController
@RequestMapping("/v1/maintenance")
@RequiredArgsConstructor
@Slf4j
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final SecurityUtil securityUtil;

    /**
     * Get all maintenance requests.
     */
    @GetMapping
    public ResponseEntity<List<MaintenanceResponseDTO>> getAll() {
        log.debug("GET all maintenance requests");
        return ResponseEntity.ok(maintenanceService.findAll());
    }

    /**
     * Get a maintenance request by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceResponseDTO> getById(@PathVariable UUID id) {
        log.debug("GET maintenance request {}", id);
        return ResponseEntity.ok(maintenanceService.getById(id));
    }

    /**
     * Get all maintenance requests raised by the currently authenticated tenant.
     */
    @GetMapping("/tenant/me")
    public ResponseEntity<List<MaintenanceResponseDTO>> getMyRequests() {
        log.debug("GET maintenance requests for current tenant");
        return ResponseEntity.ok(maintenanceService.findByTenant(securityUtil.getCurrentUserId()));
    }

    /**
     * Get all maintenance requests for a property.
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<MaintenanceResponseDTO>> getByProperty(@PathVariable UUID propertyId) {
        log.debug("GET maintenance requests for property {}", propertyId);
        return ResponseEntity.ok(maintenanceService.findByProperty(propertyId));
    }

    /**
     * Create a new maintenance request.
     */
    @PostMapping
    public ResponseEntity<MaintenanceResponseDTO> create(@Valid @RequestBody MaintenanceRequestDTO request) {
        log.info("POST create maintenance request");
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.create(request));
    }

    /**
     * Update the status of a maintenance request.
     * Request body: { "status": "IN_PROGRESS", "resolutionNotes": "..." }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<MaintenanceResponseDTO> updateStatus(@PathVariable UUID id,
                                                               @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null) {
            throw new IllegalArgumentException("'status' field is required");
        }
        MaintenanceStatus status = MaintenanceStatus.valueOf(statusStr.toUpperCase());
        String resolutionNotes = body.get("resolutionNotes");
        UUID resolvedBy = securityUtil.getCurrentUserId();
        log.info("PUT update maintenance request {} status to {}", id, status);
        return ResponseEntity.ok(maintenanceService.updateStatus(id, status, resolvedBy, resolutionNotes));
    }

    /**
     * Add a comment to a maintenance request.
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<MaintenanceCommentDTO> addComment(@PathVariable UUID id,
                                                            @Valid @RequestBody MaintenanceCommentDTO commentDTO) {
        log.info("POST add comment to maintenance request {}", id);
        // Ensure the comment is attached to the path's maintenance request ID
        commentDTO.setMaintenanceRequestId(id);
        commentDTO.setUserId(securityUtil.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.addComment(commentDTO));
    }
}
