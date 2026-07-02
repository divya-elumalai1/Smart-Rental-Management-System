package com.smartrental.controller;

import com.smartrental.model.dto.LeaseRequestDTO;
import com.smartrental.model.dto.LeaseResponseDTO;
import com.smartrental.service.LeaseService;
import com.smartrental.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for lease management.
 */
@RestController
@RequestMapping("/v1/leases")
@RequiredArgsConstructor
@Slf4j
public class LeaseController {

    private final LeaseService leaseService;
    private final SecurityUtil securityUtil;

    /**
     * Get all leases.
     */
    @GetMapping
    public ResponseEntity<List<LeaseResponseDTO>> getAll() {
        log.debug("GET all leases");
        return ResponseEntity.ok(leaseService.findAll());
    }

    /**
     * Get a lease by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeaseResponseDTO> getById(@PathVariable UUID id) {
        log.debug("GET lease {}", id);
        return ResponseEntity.ok(leaseService.getById(id));
    }

    /**
     * Get all leases for the currently authenticated tenant.
     */
    @GetMapping("/tenant/me")
    public ResponseEntity<List<LeaseResponseDTO>> getMyLeases() {
        log.debug("GET leases for current tenant");
        return ResponseEntity.ok(leaseService.findByTenant(securityUtil.getCurrentUserId()));
    }

    /**
     * Get all leases for a property.
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<LeaseResponseDTO>> getByProperty(@PathVariable UUID propertyId) {
        log.debug("GET leases for property {}", propertyId);
        return ResponseEntity.ok(leaseService.findByProperty(propertyId));
    }

    /**
     * Get all currently active leases.
     */
    @GetMapping("/active")
    public ResponseEntity<List<LeaseResponseDTO>> getActive() {
        log.debug("GET active leases");
        return ResponseEntity.ok(leaseService.findActiveLeases());
    }

    /**
     * Create a new lease.
     */
    @PostMapping
    public ResponseEntity<LeaseResponseDTO> create(@Valid @RequestBody LeaseRequestDTO request) {
        log.info("POST create lease");
        return ResponseEntity.status(HttpStatus.CREATED).body(leaseService.create(request));
    }

    /**
     * Update an existing lease.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeaseResponseDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody LeaseRequestDTO request) {
        log.info("PUT update lease {}", id);
        return ResponseEntity.ok(leaseService.update(id, request));
    }

    /**
     * Delete (soft-delete) a lease.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        log.info("DELETE lease {}", id);
        leaseService.delete(id);
    }
}
