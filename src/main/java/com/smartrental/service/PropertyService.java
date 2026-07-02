package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.Property;
import com.smartrental.model.PropertyStatus;
import com.smartrental.model.User;
import com.smartrental.model.dto.PropertyRequestDTO;
import com.smartrental.model.dto.PropertyResponseDTO;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service handling property CRUD operations and lookups.
 * Follows the Controller -> Service -> Repository -> Model pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    /**
     * Create a new property for the given landlord.
     */
    public PropertyResponseDTO create(PropertyRequestDTO request) {
        log.info("Creating property at {} for landlord {}", request.getAddress(), request.getLandlordId());

        User landlord = userRepository.findById(request.getLandlordId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getLandlordId()));

        Property property = Property.builder()
                .landlord(landlord)
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .rentAmount(request.getRentAmount())
                .deposit(request.getDeposit())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .areaSqft(request.getAreaSqft())
                .furnishingStatus(request.getFurnishingStatus())
                .amenities(request.getAmenities())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : PropertyStatus.AVAILABLE)
                .imageUrl(request.getImageUrl())
                .build();

        Property saved = propertyRepository.save(property);
        log.info("Created property with ID: {}", saved.getId());
        return toResponseDTO(saved);
    }

    /**
     * Update an existing property.
     */
    public PropertyResponseDTO update(UUID id, PropertyRequestDTO request) {
        log.info("Updating property with ID: {}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));

        // If landlord is changing, resolve the new landlord
        if (request.getLandlordId() != null
                && !request.getLandlordId().equals(property.getLandlord().getId())) {
            User landlord = userRepository.findById(request.getLandlordId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getLandlordId()));
            property.setLandlord(landlord);
        }

        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setState(request.getState());
        property.setPostalCode(request.getPostalCode());
        property.setRentAmount(request.getRentAmount());
        property.setDeposit(request.getDeposit());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setAreaSqft(request.getAreaSqft());
        property.setFurnishingStatus(request.getFurnishingStatus());
        property.setAmenities(request.getAmenities());
        property.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            property.setStatus(request.getStatus());
        }
        property.setImageUrl(request.getImageUrl());

        Property updated = propertyRepository.save(property);
        log.info("Updated property with ID: {}", updated.getId());
        return toResponseDTO(updated);
    }

    /**
     * Soft-delete a property.
     */
    public void delete(UUID id) {
        log.info("Deleting property with ID: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
        property.markAsDeleted();
        propertyRepository.save(property);
    }

    /**
     * Get a single property by ID.
     */
    @Transactional(readOnly = true)
    public PropertyResponseDTO getById(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
        return toResponseDTO(property);
    }

    /**
     * Find all properties.
     */
    @Transactional(readOnly = true)
    public List<PropertyResponseDTO> findAll() {
        return propertyRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find all properties owned by a given landlord.
     */
    @Transactional(readOnly = true)
    public List<PropertyResponseDTO> findByLandlord(UUID landlordId) {
        return propertyRepository.findByLandlordId(landlordId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find all available (vacant) properties.
     */
    @Transactional(readOnly = true)
    public List<PropertyResponseDTO> findAvailable() {
        return propertyRepository.findByStatus(PropertyStatus.AVAILABLE).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find property by unit number.
     */
    @Transactional(readOnly = true)
    public PropertyResponseDTO findByUnitNumber(String unitNumber) {
        Property property = propertyRepository.findByUnitNumber(unitNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Property with unit number", unitNumber));
        return toResponseDTO(property);
    }

    /**
     * Find property by tenant ID (active lease).
     */
    @Transactional(readOnly = true)
    public PropertyResponseDTO findByTenantId(UUID tenantId) {
        // Find property where tenant has active lease
        // This requires joining with lease table
        // For now, return first property - in production add proper query
        List<Property> allProperties = propertyRepository.findAll();
        for (Property property : allProperties) {
            for (com.smartrental.model.Lease lease : property.getLeases()) {
                if (lease.getTenant().getId().equals(tenantId) 
                    && lease.getStatus() == com.smartrental.model.LeaseStatus.ACTIVE) {
                    return toResponseDTO(property);
                }
            }
        }
        throw new ResourceNotFoundException("Property for tenant", tenantId);
    }

    // ===========================================
    // Mapping
    // ===========================================

    private PropertyResponseDTO toResponseDTO(Property property) {
        return PropertyResponseDTO.builder()
                .id(property.getId())
                .landlordId(property.getLandlord().getId())
                .landlordName(property.getLandlord().getFullName())
                .unitNumber(property.getUnitNumber())
                .floorLabel(property.getFloorLabel())
                .address(property.getAddress())
                .city(property.getCity())
                .state(property.getState())
                .postalCode(property.getPostalCode())
                .rentAmount(property.getRentAmount())
                .deposit(property.getDeposit())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .areaSqft(property.getAreaSqft())
                .furnishingStatus(property.getFurnishingStatus())
                .amenities(property.getAmenities())
                .description(property.getDescription())
                .status(property.getStatus())
                .imageUrl(property.getImageUrl())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }
}
