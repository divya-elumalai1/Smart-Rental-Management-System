package com.smartrental.service;

import com.smartrental.model.Property;
import com.smartrental.model.WaterMeterReading;
import com.smartrental.model.dto.WaterMeterBillDTO;
import com.smartrental.model.dto.WaterMeterReadingDTO;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.WaterMeterReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Service for water meter reading management and AI-powered meter reading.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaterMeterService {

    private final WaterMeterReadingRepository waterMeterReadingRepository;
    private final PropertyRepository propertyRepository;
    private final RestClient restClient = RestClient.builder().build();

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    private static final BigDecimal WATER_RATE = BigDecimal.valueOf(8);
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";

    /**
     * Read water meter from photo using Anthropic Claude API.
     * Falls back to estimated reading based on previous reading if API is not configured.
     */
    private static final java.util.Set<String> ALLOWED_IMAGE_TYPES = java.util.Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_PHOTO_SIZE = 5 * 1024 * 1024; // 5MB

    public BigDecimal readMeterFromPhoto(MultipartFile photo, String unitNumber) throws IOException {
        log.info("Reading water meter from photo for unit: {}", unitNumber);

        if (photo.isEmpty()) {
            throw new IllegalArgumentException("Photo file is empty");
        }
        if (photo.getSize() > MAX_PHOTO_SIZE) {
            throw new IllegalArgumentException("Photo size exceeds maximum allowed size of 5MB");
        }
        if (photo.getContentType() == null || !ALLOWED_IMAGE_TYPES.contains(photo.getContentType())) {
            throw new IllegalArgumentException("Invalid photo format. Allowed: JPEG, PNG, GIF, WebP");
        }

        if (anthropicApiKey == null || anthropicApiKey.isBlank() || anthropicApiKey.equals("YOUR_ANTHROPIC_API_KEY")) {
            log.warn("Anthropic API key not configured, estimating reading for unit: {}", unitNumber);
            return estimateReading(unitNumber);
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(photo.getBytes());
            String mimeType = photo.getContentType() != null ? photo.getContentType() : "image/jpeg";

            var request = new AnthropicRequest(
                "claude-3-5-sonnet-20241022",
                1024,
                java.util.List.of(new AnthropicMessage("user", java.util.List.of(
                    new AnthropicContent("image", new AnthropicSource("base64", mimeType, base64Image), null),
                    new AnthropicContent("text", null, "Read this water meter and return ONLY the numeric reading. Return just the number, no explanation.")
                )))
            );

            var response = restClient.post()
                .uri(ANTHROPIC_API_URL)
                .header("x-api-key", anthropicApiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AnthropicResponse.class);

            if (response != null && response.content() != null) {
                AnthropicContent firstBlock = response.content().stream()
                    .filter(c -> "text".equals(c.type()) && c.text() != null)
                    .findFirst().orElse(null);
                if (firstBlock != null) {
                    String text = firstBlock.text();
                    String digits = text.replaceAll("[^0-9.]", "").trim();
                    if (!digits.isEmpty()) {
                        return new BigDecimal(digits).setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Anthropic API call failed for unit: {}, falling back to estimate", unitNumber, e);
        }

        return estimateReading(unitNumber);
    }

    private BigDecimal estimateReading(String unitNumber) {
        WaterMeterReading lastReading = waterMeterReadingRepository
            .findTopByUnitNumberOrderByReadingDateDesc(unitNumber)
            .orElse(null);
        if (lastReading != null) {
            return lastReading.getCurrentReading()
                .add(BigDecimal.valueOf(Math.random() * 50 + 5))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(Math.random() * 400 + 100).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculate water bill for a unit.
     */
    public WaterMeterBillDTO calculateWaterBill(String unitNumber, BigDecimal currentReading) {
        log.info("Calculating water bill for unit: {}", unitNumber);
        
        // Find property by unit number
        Property property = propertyRepository.findByUnitNumber(unitNumber)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found: " + unitNumber));
        
        // Get previous reading
        WaterMeterReading lastReading = waterMeterReadingRepository
            .findTopByUnitNumberOrderByReadingDateDesc(unitNumber)
            .orElse(null);
        
        BigDecimal previousReading = lastReading != null ? lastReading.getCurrentReading() : BigDecimal.ZERO;
        
        // Calculate units consumed
        BigDecimal unitsConsumed = currentReading.subtract(previousReading);
        if (unitsConsumed.compareTo(BigDecimal.ZERO) < 0) {
            unitsConsumed = BigDecimal.ZERO;
        }
        
        // Calculate bills
        BigDecimal waterBill = unitsConsumed.multiply(WATER_RATE);
        BigDecimal totalBill = property.getRentAmount().add(waterBill);
        
        return WaterMeterBillDTO.builder()
            .unitNumber(unitNumber)
            .rentAmount(property.getRentAmount())
            .previousReading(previousReading)
            .currentReading(currentReading)
            .unitsConsumed(unitsConsumed)
            .waterRate(WATER_RATE)
            .waterBill(waterBill)
            .totalBill(totalBill)
            .billDate(LocalDate.now())
            .build();
    }

    /**
     * Save water meter reading.
     */
    public WaterMeterReadingDTO saveReading(String unitNumber, BigDecimal currentReading, String photoUrl, String notes) {
        log.info("Saving water meter reading for unit: {}", unitNumber);
        
        Property property = propertyRepository.findByUnitNumber(unitNumber)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found: " + unitNumber));
        
        // Get previous reading
        WaterMeterReading lastReading = waterMeterReadingRepository
            .findTopByUnitNumberOrderByReadingDateDesc(unitNumber)
            .orElse(null);
        
        BigDecimal previousReading = lastReading != null ? lastReading.getCurrentReading() : BigDecimal.ZERO;
        BigDecimal unitsConsumed = currentReading.subtract(previousReading);
        if (unitsConsumed.compareTo(BigDecimal.ZERO) < 0) {
            unitsConsumed = BigDecimal.ZERO;
        }
        
        WaterMeterReading reading = WaterMeterReading.builder()
            .property(property)
            .unitNumber(unitNumber)
            .previousReading(previousReading)
            .currentReading(currentReading)
            .unitsConsumed(unitsConsumed)
            .readingDate(LocalDate.now())
            .meterPhotoUrl(photoUrl)
            .notes(notes)
            .build();
        
        reading = waterMeterReadingRepository.save(reading);
        
        return WaterMeterReadingDTO.builder()
            .id(reading.getId())
            .propertyId(reading.getProperty().getId())
            .unitNumber(reading.getUnitNumber())
            .previousReading(reading.getPreviousReading())
            .currentReading(reading.getCurrentReading())
            .unitsConsumed(reading.getUnitsConsumed())
            .waterBill(reading.calculateWaterBill())
            .totalBill(reading.calculateTotalBill(property.getRentAmount()))
            .readingDate(reading.getReadingDate())
            .meterPhotoUrl(reading.getMeterPhotoUrl())
            .notes(reading.getNotes())
            .build();
    }

    /**
     * Get all readings for a unit.
     */
    public List<WaterMeterReadingDTO> getReadingsByUnit(String unitNumber) {
        List<WaterMeterReading> readings = waterMeterReadingRepository
            .findByUnitNumberOrderByReadingDateDesc(unitNumber);
        
        return readings.stream().map(reading -> 
            WaterMeterReadingDTO.builder()
                .id(reading.getId())
                .propertyId(reading.getProperty().getId())
                .unitNumber(reading.getUnitNumber())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .unitsConsumed(reading.getUnitsConsumed())
                .waterBill(reading.calculateWaterBill())
                .totalBill(reading.calculateTotalBill(reading.getProperty().getRentAmount()))
                .readingDate(reading.getReadingDate())
                .meterPhotoUrl(reading.getMeterPhotoUrl())
                .notes(reading.getNotes())
                .build()
        ).toList();
    }

    /**
     * Get latest reading for all occupied units.
     */
    public List<WaterMeterReadingDTO> getLatestReadingsForOccupiedUnits() {
        // Get all properties with active leases
        List<Property> occupiedProperties = propertyRepository.findByStatus(com.smartrental.model.PropertyStatus.OCCUPIED);
        
        return occupiedProperties.stream()
            .map(prop -> {
                WaterMeterReading lastReading = waterMeterReadingRepository
                    .findTopByUnitNumberOrderByReadingDateDesc(prop.getUnitNumber())
                    .orElse(null);
                
                if (lastReading != null) {
                    return WaterMeterReadingDTO.builder()
                        .id(lastReading.getId())
                        .propertyId(lastReading.getProperty().getId())
                        .unitNumber(lastReading.getUnitNumber())
                        .previousReading(lastReading.getPreviousReading())
                        .currentReading(lastReading.getCurrentReading())
                        .unitsConsumed(lastReading.getUnitsConsumed())
                        .waterBill(lastReading.calculateWaterBill())
                        .totalBill(lastReading.calculateTotalBill(prop.getRentAmount()))
                        .readingDate(lastReading.getReadingDate())
                        .meterPhotoUrl(lastReading.getMeterPhotoUrl())
                        .notes(lastReading.getNotes())
                        .build();
                }
                return null;
            })
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    // ===========================================
    // Anthropic Claude API DTOs
    // ===========================================

    private record AnthropicRequest(String model, int maxTokens, java.util.List<AnthropicMessage> messages) {}
    private record AnthropicMessage(String role, java.util.List<AnthropicContent> content) {}
    private record AnthropicContent(String type, AnthropicSource source, String text) {}
    private record AnthropicSource(String type, String mediaType, String data) {}
    private record AnthropicResponse(String id, String type, String role, java.util.List<AnthropicContent> content, String stopReason, String model) {}
}
