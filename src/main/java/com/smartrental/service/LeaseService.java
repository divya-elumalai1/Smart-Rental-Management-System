package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.Lease;
import com.smartrental.model.LeaseStatus;
import com.smartrental.model.Property;
import com.smartrental.model.User;
import com.smartrental.model.dto.LeaseRequestDTO;
import com.smartrental.model.dto.LeaseResponseDTO;
import com.smartrental.repository.LeaseRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service handling lease CRUD operations, tenant assignment, and active-lease lookups.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    /**
     * Create a new lease (optionally assigning a tenant to a property).
     */
    public LeaseResponseDTO create(LeaseRequestDTO request) {
        log.info("Creating lease for tenant {} on property {}", request.getTenantId(), request.getPropertyId());

        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getTenantId()));
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

        Lease lease = Lease.builder()
                .tenant(tenant)
                .property(property)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rentAmount(request.getRentAmount())
                .depositAmount(request.getDepositAmount())
                .status(request.getStatus() != null ? request.getStatus() : LeaseStatus.PENDING)
                .leaseDocumentUrl(request.getLeaseDocumentUrl())
                .termsAndConditions(request.getTermsAndConditions())
                .build();

        Lease saved = leaseRepository.save(lease);
        log.info("Created lease with ID: {}", saved.getId());
        return toResponseDTO(saved);
    }

    /**
     * Update an existing lease.
     */
    public LeaseResponseDTO update(UUID id, LeaseRequestDTO request) {
        log.info("Updating lease with ID: {}", id);

        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lease", id));

        if (request.getTenantId() != null
                && !request.getTenantId().equals(lease.getTenant().getId())) {
            User tenant = userRepository.findById(request.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getTenantId()));
            lease.setTenant(tenant);
        }

        if (request.getPropertyId() != null
                && !request.getPropertyId().equals(lease.getProperty().getId())) {
            Property property = propertyRepository.findById(request.getPropertyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));
            lease.setProperty(property);
        }

        lease.setStartDate(request.getStartDate());
        lease.setEndDate(request.getEndDate());
        lease.setRentAmount(request.getRentAmount());
        lease.setDepositAmount(request.getDepositAmount());
        if (request.getStatus() != null) {
            lease.setStatus(request.getStatus());
        }
        lease.setLeaseDocumentUrl(request.getLeaseDocumentUrl());
        lease.setTermsAndConditions(request.getTermsAndConditions());

        Lease updated = leaseRepository.save(lease);
        log.info("Updated lease with ID: {}", updated.getId());
        return toResponseDTO(updated);
    }

    /**
     * Soft-delete a lease.
     */
    public void delete(UUID id) {
        log.info("Deleting lease with ID: {}", id);
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lease", id));
        lease.markAsDeleted();
        leaseRepository.save(lease);
    }

    /**
     * Get a single lease by ID.
     */
    @Transactional(readOnly = true)
    public LeaseResponseDTO getById(UUID id) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lease", id));
        return toResponseDTO(lease);
    }

    /**
     * Find all leases.
     */
    @Transactional(readOnly = true)
    public List<LeaseResponseDTO> findAll() {
        return leaseRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find all leases for a given tenant.
     */
    @Transactional(readOnly = true)
    public List<LeaseResponseDTO> findByTenant(UUID tenantId) {
        return leaseRepository.findByTenantId(tenantId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find all leases for a given property.
     */
    @Transactional(readOnly = true)
    public List<LeaseResponseDTO> findByProperty(UUID propertyId) {
        return leaseRepository.findByPropertyId(propertyId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find all currently active leases (status ACTIVE and within date range).
     */
    @Transactional(readOnly = true)
    public List<LeaseResponseDTO> findActiveLeases() {
        return leaseRepository.findByStatus(LeaseStatus.ACTIVE).stream()
                .filter(lease -> !LocalDate.now().isBefore(lease.getStartDate())
                        && !LocalDate.now().isAfter(lease.getEndDate()))
                .map(this::toResponseDTO)
                .toList();
    }

    // ===========================================
    // Mapping
    // ===========================================

    private LeaseResponseDTO toResponseDTO(Lease lease) {
        return LeaseResponseDTO.builder()
                .id(lease.getId())
                .tenantId(lease.getTenant().getId())
                .tenantName(lease.getTenant().getFullName())
                .propertyId(lease.getProperty().getId())
                .propertyAddress(lease.getProperty().getAddress())
                .startDate(lease.getStartDate())
                .endDate(lease.getEndDate())
                .rentAmount(lease.getRentAmount())
                .depositAmount(lease.getDepositAmount())
                .status(lease.getStatus())
                .leaseDocumentUrl(lease.getLeaseDocumentUrl())
                .termsAndConditions(lease.getTermsAndConditions())
                .currentlyActive(lease.isCurrentlyActive())
                .createdAt(lease.getCreatedAt())
                .updatedAt(lease.getUpdatedAt())
                .build();
    }
}
