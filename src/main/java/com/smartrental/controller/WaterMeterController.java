package com.smartrental.controller;

import com.smartrental.model.dto.WaterMeterBillDTO;
import com.smartrental.model.dto.WaterMeterReadingDTO;
import com.smartrental.service.WaterMeterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for water meter reading management.
 */
@RestController
@RequestMapping("/v1/water-meter")
@RequiredArgsConstructor
@Slf4j
public class WaterMeterController {

    private final WaterMeterService waterMeterService;

    /**
     * Read water meter from photo using AI.
     * POST /api/v1/water-meter/read-photo
     */
    @PostMapping("/read-photo")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<BigDecimal> readMeterFromPhoto(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("unitNumber") String unitNumber) throws IOException {
        log.info("POST read meter from photo for unit: {}", unitNumber);
        BigDecimal reading = waterMeterService.readMeterFromPhoto(photo, unitNumber);
        return ResponseEntity.ok(reading);
    }

    /**
     * Calculate water bill for a unit.
     * POST /api/v1/water-meter/calculate
     */
    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<WaterMeterBillDTO> calculateWaterBill(
            @RequestParam("unitNumber") String unitNumber,
            @RequestParam("currentReading") BigDecimal currentReading) {
        log.info("POST calculate water bill for unit: {}", unitNumber);
        WaterMeterBillDTO bill = waterMeterService.calculateWaterBill(unitNumber, currentReading);
        return ResponseEntity.ok(bill);
    }

    /**
     * Save water meter reading.
     * POST /api/v1/water-meter/save
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<WaterMeterReadingDTO> saveReading(
            @RequestParam("unitNumber") String unitNumber,
            @RequestParam("currentReading") BigDecimal currentReading,
            @RequestParam(value = "photoUrl", required = false) String photoUrl,
            @RequestParam(value = "notes", required = false) String notes) {
        log.info("POST save water meter reading for unit: {}", unitNumber);
        WaterMeterReadingDTO reading = waterMeterService.saveReading(unitNumber, currentReading, photoUrl, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(reading);
    }

    /**
     * Get all readings for a unit.
     * GET /api/v1/water-meter/unit/{unitNumber}
     */
    @GetMapping("/unit/{unitNumber}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'TENANT')")
    public ResponseEntity<List<WaterMeterReadingDTO>> getReadingsByUnit(@PathVariable String unitNumber) {
        log.debug("GET water meter readings for unit: {}", unitNumber);
        return ResponseEntity.ok(waterMeterService.getReadingsByUnit(unitNumber));
    }

    /**
     * Get latest readings for all occupied units.
     * GET /api/v1/water-meter/occupied
     */
    @GetMapping("/occupied")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<WaterMeterReadingDTO>> getLatestReadingsForOccupiedUnits() {
        log.debug("GET latest water meter readings for occupied units");
        return ResponseEntity.ok(waterMeterService.getLatestReadingsForOccupiedUnits());
    }
}
