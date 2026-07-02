package com.smartrental.controller;

import com.smartrental.model.dto.PropertyRequestDTO;
import com.smartrental.model.dto.PropertyResponseDTO;
import com.smartrental.service.PropertyService;
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
 * REST controller for property management.
 */
@RestController
@RequestMapping("/v1/properties")
@RequiredArgsConstructor
@Slf4j
public class PropertyController {

    private final PropertyService propertyService;
    private final SecurityUtil securityUtil;

    /**
     * Get all properties.
     */
    @GetMapping
    public ResponseEntity<List<PropertyResponseDTO>> getAll() {
        log.debug("GET all properties");
        return ResponseEntity.ok(propertyService.findAll());
    }

    /**
     * Get a property by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> getById(@PathVariable UUID id) {
        log.debug("GET property {}", id);
        return ResponseEntity.ok(propertyService.getById(id));
    }

    /**
     * Get all properties owned by the currently authenticated landlord.
     */
    @GetMapping("/landlord/me")
    public ResponseEntity<List<PropertyResponseDTO>> getMyProperties() {
        log.debug("GET properties for current landlord");
        return ResponseEntity.ok(propertyService.findByLandlord(securityUtil.getCurrentUserId()));
    }

    /**
     * Get all available (vacant) properties.
     */
    @GetMapping("/available")
    public ResponseEntity<List<PropertyResponseDTO>> getAvailable() {
        log.debug("GET available properties");
        return ResponseEntity.ok(propertyService.findAvailable());
    }

    /**
     * Create a new property.
     */
    @PostMapping
    public ResponseEntity<PropertyResponseDTO> create(@Valid @RequestBody PropertyRequestDTO request) {
        log.info("POST create property at {}", request.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.create(request));
    }

    /**
     * Update an existing property.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> update(@PathVariable UUID id,
                                                      @Valid @RequestBody PropertyRequestDTO request) {
        log.info("PUT update property {}", id);
        return ResponseEntity.ok(propertyService.update(id, request));
    }

    /**
     * Delete (soft-delete) a property.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        log.info("DELETE property {}", id);
        propertyService.delete(id);
    }

    /**
     * Get property by unit number.
     */
    @GetMapping("/unit/{unitNumber}")
    public ResponseEntity<PropertyResponseDTO> getByUnitNumber(@PathVariable String unitNumber) {
        log.debug("GET property by unit number: {}", unitNumber);
        return ResponseEntity.ok(propertyService.findByUnitNumber(unitNumber));
    }
}
