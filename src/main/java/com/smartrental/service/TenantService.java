package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.*;
import com.smartrental.model.dto.TenantAssignRequestDTO;
import com.smartrental.model.dto.TenantSummaryDTO;
import com.smartrental.model.dto.TenantUpdateRequestDTO;
import com.smartrental.repository.LeaseRepository;
import com.smartrental.repository.PaymentRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<TenantSummaryDTO> listTenants(UUID landlordId) {
        List<TenantSummaryDTO> result = new ArrayList<>();
        for (Property property : propertyRepository.findByLandlordId(landlordId)) {
            leaseRepository.findByPropertyIdAndStatus(property.getId(), LeaseStatus.ACTIVE)
                    .ifPresent(lease -> result.add(toSummary(lease, property)));
        }
        return result;
    }

    public TenantSummaryDTO assignTenant(UUID landlordId, TenantAssignRequestDTO request) {
        Property property = propertyRepository.findByLandlordIdAndUnitNumber(landlordId, request.getUnitNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Property unit " + request.getUnitNumber()));

        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new IllegalStateException("Unit " + request.getUnitNumber() + " is not available (current status: " + property.getStatus().getDisplayName() + ")");
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        User tenant = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword().trim()))
                .role(Role.TENANT)
                .emailVerified(true)
                .phoneVerified(true)
                .active(true)
                .city("Bengaluru")
                .state("Karnataka")
                .country("India")
                .build();
        tenant = userRepository.save(tenant);

        LocalDate endDate = request.getLeaseEnd() != null
                ? request.getLeaseEnd()
                : request.getLeaseStart().plusYears(2);

        Lease lease = Lease.builder()
                .tenant(tenant)
                .property(property)
                .startDate(request.getLeaseStart())
                .endDate(endDate)
                .rentAmount(request.getRentAmount())
                .depositAmount(request.getDeposit() != null ? request.getDeposit() : BigDecimal.ZERO)
                .status(LeaseStatus.ACTIVE)
                .build();
        lease = leaseRepository.save(lease);

        property.setRentAmount(request.getRentAmount());
        if (request.getDeposit() != null) {
            property.setDeposit(request.getDeposit());
        }
        property.setStatus(PropertyStatus.OCCUPIED);
        propertyRepository.save(property);

        LocalDate rentPeriod = LocalDate.now().withDayOfMonth(1);
        paymentRepository.save(Payment.builder()
                .tenant(tenant)
                .property(property)
                .lease(lease)
                .amount(request.getRentAmount())
                .status(PaymentStatus.PENDING)
                .dueDate(rentPeriod)
                .rentPeriod(rentPeriod)
                .build());

        log.info("Assigned tenant {} to unit {}", tenant.getEmail(), property.getUnitNumber());
        return toSummary(lease, property);
    }

    public TenantSummaryDTO updateTenant(UUID landlordId, UUID leaseId, TenantUpdateRequestDTO request) {
        Lease lease = getOwnedLease(landlordId, leaseId);
        User tenant = lease.getTenant();
        Property property = lease.getProperty();

        tenant.setFirstName(request.getFirstName());
        tenant.setLastName(request.getLastName());
        tenant.setEmail(request.getEmail());
        tenant.setPhoneNumber(request.getPhoneNumber());
        userRepository.save(tenant);

        if (request.getRentAmount() != null) {
            lease.setRentAmount(request.getRentAmount());
            property.setRentAmount(request.getRentAmount());
        }
        if (request.getDeposit() != null) {
            lease.setDepositAmount(request.getDeposit());
            property.setDeposit(request.getDeposit());
        }
        if (request.getLeaseStart() != null) {
            lease.setStartDate(request.getLeaseStart());
        }
        if (request.getLeaseEnd() != null) {
            lease.setEndDate(request.getLeaseEnd());
        }
        leaseRepository.save(lease);
        propertyRepository.save(property);

        return toSummary(lease, property);
    }

    public void removeTenant(UUID landlordId, UUID leaseId) {
        Lease lease = getOwnedLease(landlordId, leaseId);
        Property property = lease.getProperty();

        lease.setStatus(LeaseStatus.TERMINATED);
        leaseRepository.save(lease);

        property.setStatus(PropertyStatus.AVAILABLE);
        propertyRepository.save(property);

        paymentRepository.findByPropertyId(property.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.OVERDUE)
                .forEach(p -> {
                    p.setStatus(PaymentStatus.CANCELLED);
                    paymentRepository.save(p);
                });

        log.info("Removed tenant from unit {}", property.getUnitNumber());
    }

    /**
     * Reset a tenant's password.
     */
    public void resetTenantPassword(UUID landlordId, String unitNumber, String newPassword) {
        Property property = propertyRepository.findByLandlordIdAndUnitNumber(landlordId, unitNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Property unit " + unitNumber));

        Lease lease = leaseRepository.findByPropertyIdAndStatus(property.getId(), LeaseStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active lease for unit " + unitNumber));

        User tenant = lease.getTenant();
        tenant.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(tenant);

        log.info("Password reset for tenant {} in unit {}", tenant.getEmail(), unitNumber);
    }

    private Lease getOwnedLease(UUID landlordId, UUID leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lease", leaseId));
        if (!lease.getProperty().getLandlord().getId().equals(landlordId)) {
            throw new IllegalArgumentException("Lease does not belong to this landlord");
        }
        return lease;
    }

    private TenantSummaryDTO toSummary(Lease lease, Property property) {
        LocalDate period = LocalDate.now().withDayOfMonth(1);
        String rentStatus = "PENDING";
        var payments = paymentRepository.findByPropertyIdAndRentPeriodOrderByCreatedAtDesc(
                property.getId(), period);
        if (!payments.isEmpty()) {
            Payment p = payments.get(0);
            if (p.getStatus() == PaymentStatus.COMPLETED) rentStatus = "PAID";
            else if (p.isOverdue()) rentStatus = "OVERDUE";
        }

        User tenant = lease.getTenant();
        return TenantSummaryDTO.builder()
                .leaseId(lease.getId())
                .tenantId(tenant.getId())
                .propertyId(property.getId())
                .unitNumber(property.getUnitNumber())
                .floorLabel(property.getFloorLabel())
                .tenantName(tenant.getFullName())
                .email(tenant.getEmail())
                .phoneNumber(tenant.getPhoneNumber())
                .rentAmount(lease.getRentAmount())
                .deposit(lease.getDepositAmount())
                .leaseStart(lease.getStartDate())
                .leaseEnd(lease.getEndDate())
                .rentStatus(rentStatus)
                .build();
    }

    /**
     * Update tenant by unit number.
     */
    public TenantSummaryDTO updateTenantByUnitNumber(UUID landlordId, String unitNumber, TenantUpdateRequestDTO request) {
        Property property = propertyRepository.findByLandlordIdAndUnitNumber(landlordId, unitNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Property unit " + unitNumber));
        
        Lease lease = leaseRepository.findByPropertyIdAndStatus(property.getId(), LeaseStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active lease for unit " + unitNumber));
        
        return updateTenant(landlordId, lease.getId(), request);
    }

    /**
     * Remove tenant by unit number.
     */
    public void removeTenantByUnitNumber(UUID landlordId, String unitNumber) {
        Property property = propertyRepository.findByLandlordIdAndUnitNumber(landlordId, unitNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Property unit " + unitNumber));
        
        Lease lease = leaseRepository.findByPropertyIdAndStatus(property.getId(), LeaseStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active lease for unit " + unitNumber));
        
        removeTenant(landlordId, lease.getId());
    }
}
