package com.smartrental.service;

import com.smartrental.model.LeaseStatus;
import com.smartrental.model.MaintenanceStatus;
import com.smartrental.model.Payment;
import com.smartrental.model.PaymentStatus;
import com.smartrental.model.Property;
import com.smartrental.model.PropertyStatus;
import com.smartrental.model.User;
import com.smartrental.model.dto.DashboardResponseDTO;
import com.smartrental.model.dto.MaintenanceResponseDTO;
import com.smartrental.model.dto.PaymentResponseDTO;
import com.smartrental.model.dto.TenantDashboardResponseDTO;
import com.smartrental.model.dto.UnitDashboardDTO;
import com.smartrental.repository.LeaseRepository;
import com.smartrental.repository.MaintenanceRequestRepository;
import com.smartrental.repository.PaymentRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service that aggregates statistics for a landlord's dashboard.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final PropertyRepository propertyRepository;
    private final PaymentRepository paymentRepository;
    private final LeaseRepository leaseRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final UserRepository userRepository;

    /**
     * Build the dashboard summary for a given landlord.
     *
     * @param landlordId the landlord's user ID
     * @return aggregated dashboard statistics
     */
    public DashboardResponseDTO getDashboardSummary(UUID landlordId) {
        log.debug("Building dashboard summary for landlord {}", landlordId);

        List<Property> properties = propertyRepository.findByLandlordId(landlordId);
        Set<UUID> propertyIds = properties.stream()
                .map(Property::getId)
                .collect(Collectors.toSet());

        long totalUnits = properties.size();
        long occupied = properties.stream()
                .filter(p -> p.getStatus() == PropertyStatus.OCCUPIED)
                .count();
        long vacant = properties.stream()
                .filter(p -> p.getStatus() == PropertyStatus.AVAILABLE)
                .count();

        // Collected = sum of completed payments across the landlord's properties
        BigDecimal collected = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;
        BigDecimal overdue = BigDecimal.ZERO;

        for (UUID propertyId : propertyIds) {
            for (var payment : paymentRepository.findByPropertyId(propertyId)) {
                if (payment.getStatus() == PaymentStatus.COMPLETED) {
                    collected = collected.add(payment.getAmount());
                } else if (payment.getStatus() == PaymentStatus.OVERDUE) {
                    overdue = overdue.add(payment.getAmount());
                    pending = pending.add(payment.getAmount());
                } else if (payment.getStatus() == PaymentStatus.PENDING) {
                    pending = pending.add(payment.getAmount());
                }
            }
        }

        long activeLeases = properties.stream()
                .flatMap(p -> leaseRepository.findByPropertyId(p.getId()).stream())
                .filter(l -> l.getStatus() == LeaseStatus.ACTIVE)
                .count();

        long pendingMaintenance = properties.stream()
                .flatMap(p -> maintenanceRequestRepository.findByPropertyId(p.getId()).stream())
                .filter(m -> m.getStatus() == MaintenanceStatus.PENDING
                        || m.getStatus() == MaintenanceStatus.IN_PROGRESS)
                .count();

        // Tenants = unique tenant IDs across the landlord's active leases
        Set<UUID> tenantIds = new HashSet<>();
        properties.forEach(p -> leaseRepository.findByPropertyId(p.getId()).forEach(l ->
                tenantIds.add(l.getTenant().getId())));

        return DashboardResponseDTO.builder()
                .totalUnits(totalUnits)
                .occupied(occupied)
                .vacant(vacant)
                .collected(collected)
                .pending(pending)
                .overdue(overdue)
                .activeLeases(activeLeases)
                .pendingMaintenance(pendingMaintenance)
                .totalTenants(tenantIds.size())
                .build();
    }

    /**
     * Build unit cards for the owner dashboard — all properties with tenant + rent status.
     */
    public List<UnitDashboardDTO> getOwnerUnits(UUID landlordId) {
        List<Property> properties = propertyRepository.findByLandlordId(landlordId);
        LocalDate currentPeriod = LocalDate.now().withDayOfMonth(1);

        List<UnitDashboardDTO> units = new ArrayList<>();
        for (Property property : properties) {
            UnitDashboardDTO.UnitDashboardDTOBuilder builder = UnitDashboardDTO.builder()
                    .id(property.getId())
                    .unitNumber(property.getUnitNumber())
                    .floorLabel(property.getFloorLabel())
                    .type("House")
                    .rentAmount(property.getRentAmount())
                    .deposit(property.getDeposit() != null ? property.getDeposit() : BigDecimal.ZERO)
                    .propertyStatus(property.getStatus());

            if (property.getStatus() == PropertyStatus.UNDER_CONSTRUCTION) {
                builder.rentStatus("UNDER_CONSTRUCTION");
                units.add(builder.build());
                continue;
            }

            leaseRepository.findByPropertyIdAndStatus(property.getId(), LeaseStatus.ACTIVE)
                    .ifPresent(lease -> {
                        builder.tenantId(lease.getTenant().getId())
                                .tenantName(lease.getTenant().getFullName())
                                .tenantPhone(lease.getTenant().getPhoneNumber())
                                .leaseStart(lease.getStartDate());
                    });

            List<com.smartrental.model.Payment> periodPayments =
                    paymentRepository.findByPropertyIdAndRentPeriodOrderByCreatedAtDesc(
                            property.getId(), currentPeriod);

            if (!periodPayments.isEmpty()) {
                var payment = periodPayments.get(0);
                builder.currentPaymentId(payment.getId())
                        .paymentStatus(payment.getStatus())
                        .dueDate(payment.getDueDate())
                        .rentStatus(mapPaymentStatus(payment));
            } else if (property.getStatus() == PropertyStatus.OCCUPIED) {
                builder.rentStatus("PENDING");
            } else {
                builder.rentStatus("VACANT");
            }

            units.add(builder.build());
        }

        units.sort(Comparator
                .comparing((UnitDashboardDTO u) -> floorOrder(u.getFloorLabel()))
                .thenComparing(u -> unitSortKey(u.getUnitNumber())));
        return units;
    }

    private static String mapPaymentStatus(com.smartrental.model.Payment payment) {
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return "PAID";
        }
        if (payment.isOverdue()) {
            return "OVERDUE";
        }
        return "PENDING";
    }

    private static int floorOrder(String floor) {
        if (floor == null) return 99;
        return switch (floor) {
            case "Ground Floor" -> 0;
            case "1st Floor" -> 1;
            case "2nd Floor" -> 2;
            case "3rd Floor" -> 3;
            default -> 50;
        };
    }

    private static String unitSortKey(String unit) {
        if (unit == null) return "ZZZ";
        if (unit.startsWith("G")) return "0" + unit;
        return unit;
    }

    /**
     * Build the tenant dashboard — unit info, current rent status, recent payments, maintenance.
     */
    public TenantDashboardResponseDTO getTenantDashboard(UUID tenantId) {
        User tenant = userRepository.findById(tenantId).orElse(null);
        String firstName = tenant != null ? tenant.getFirstName() : "Tenant";

        // Find active lease
        var activeLeaseOpt = leaseRepository.findByTenantIdAndStatus(tenantId, LeaseStatus.ACTIVE);
        if (activeLeaseOpt.isEmpty()) {
            return TenantDashboardResponseDTO.builder()
                    .firstName(firstName)
                    .build();
        }

        var lease = activeLeaseOpt.get();
        Property property = lease.getProperty();

        // Current period payment
        LocalDate currentPeriod = LocalDate.now().withDayOfMonth(1);
        List<Payment> periodPayments = paymentRepository
                .findByPropertyIdAndRentPeriodOrderByCreatedAtDesc(property.getId(), currentPeriod);

        String rentStatus = "PENDING";
        LocalDate dueDate = null;
        if (!periodPayments.isEmpty()) {
            var payment = periodPayments.get(0);
            dueDate = payment.getDueDate();
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                rentStatus = "PAID";
            } else if (payment.isOverdue()) {
                rentStatus = "OVERDUE";
            }
        }

        // Recent payments (last 5)
        List<PaymentResponseDTO> recentPayments = paymentRepository
                .findByTenantIdOrderByPaymentDateDesc(tenantId).stream()
                .limit(5)
                .map(p -> PaymentResponseDTO.builder()
                        .id(p.getId())
                        .amount(p.getAmount())
                        .paymentDate(p.getPaymentDate())
                        .paymentMode(p.getPaymentMode())
                        .receiptNumber(p.getReceiptNumber())
                        .status(p.getStatus())
                        .rentPeriod(p.getRentPeriod())
                        .build())
                .toList();

        // Recent maintenance (last 5)
        List<MaintenanceResponseDTO> recentMaintenance = maintenanceRequestRepository
                .findByTenantId(tenantId).stream()
                .limit(5)
                .map(m -> MaintenanceResponseDTO.builder()
                        .id(m.getId())
                        .title(m.getTitle())
                        .status(m.getStatus())
                        .priority(m.getPriority())
                        .createdAt(m.getCreatedAt())
                        .build())
                .toList();

        return TenantDashboardResponseDTO.builder()
                .firstName(firstName)
                .unitNumber(property.getUnitNumber())
                .floorLabel(property.getFloorLabel())
                .rentAmount(lease.getRentAmount())
                .deposit(lease.getDepositAmount())
                .dueDate(dueDate)
                .rentStatus(rentStatus)
                .recentPayments(recentPayments)
                .recentMaintenance(recentMaintenance)
                .build();
    }
}
