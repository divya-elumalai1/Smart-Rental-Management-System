package com.smartrental.service;

import com.smartrental.model.Lease;
import com.smartrental.model.LeaseStatus;
import com.smartrental.model.MaintenanceRequest;
import com.smartrental.model.MaintenanceStatus;
import com.smartrental.model.Payment;
import com.smartrental.model.PaymentStatus;
import com.smartrental.model.Property;
import com.smartrental.model.PropertyStatus;
import com.smartrental.model.Role;
import com.smartrental.model.User;
import com.smartrental.model.dto.DashboardResponseDTO;
import com.smartrental.model.dto.TenantDashboardResponseDTO;
import com.smartrental.repository.LeaseRepository;
import com.smartrental.repository.MaintenanceRequestRepository;
import com.smartrental.repository.PaymentRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardService}.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private LeaseRepository leaseRepository;
    @Mock
    private MaintenanceRequestRepository maintenanceRequestRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private UUID landlordId;
    private UUID propertyId;
    private Property property;
    private UUID tenantId;
    private User tenant;

    @BeforeEach
    void setUp() {
        landlordId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        property = Property.builder()
                .id(propertyId)
                .unitNumber("101")
                .floorLabel("1st Floor")
                .status(PropertyStatus.OCCUPIED)
                .build();
        tenant = User.builder()
                .id(tenantId)
                .firstName("Tom")
                .lastName("Tenant")
                .email("tom@example.com")
                .role(Role.TENANT)
                .build();
    }

    @Test
    void getDashboardSummary_shouldAggregateStats() {
        Property vacant = Property.builder()
                .id(UUID.randomUUID())
                .status(PropertyStatus.AVAILABLE)
                .build();
        when(propertyRepository.findByLandlordId(landlordId)).thenReturn(List.of(property, vacant));

        Payment completed = Payment.builder()
                .amount(new BigDecimal("15000"))
                .status(PaymentStatus.COMPLETED)
                .build();
        Payment pending = Payment.builder()
                .amount(new BigDecimal("10000"))
                .status(PaymentStatus.PENDING)
                .build();
        Payment overdue = Payment.builder()
                .amount(new BigDecimal("5000"))
                .status(PaymentStatus.OVERDUE)
                .build();
        when(paymentRepository.findByPropertyId(propertyId))
                .thenReturn(List.of(completed, pending, overdue));
        when(paymentRepository.findByPropertyId(vacant.getId())).thenReturn(List.of());

        Lease activeLease = Lease.builder()
                .id(UUID.randomUUID())
                .tenant(tenant)
                .property(property)
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(11))
                .rentAmount(new BigDecimal("15000"))
                .status(LeaseStatus.ACTIVE)
                .build();
        when(leaseRepository.findByPropertyId(propertyId)).thenReturn(List.of(activeLease));
        when(leaseRepository.findByPropertyId(vacant.getId())).thenReturn(List.of());

        MaintenanceRequest open = MaintenanceRequest.builder()
                .id(UUID.randomUUID())
                .status(MaintenanceStatus.PENDING)
                .build();
        when(maintenanceRequestRepository.findByPropertyId(propertyId)).thenReturn(List.of(open));
        when(maintenanceRequestRepository.findByPropertyId(vacant.getId())).thenReturn(List.of());

        DashboardResponseDTO result = dashboardService.getDashboardSummary(landlordId);

        assertThat(result.getTotalUnits()).isEqualTo(2);
        assertThat(result.getOccupied()).isEqualTo(1);
        assertThat(result.getVacant()).isEqualTo(1);
        assertThat(result.getCollected()).isEqualByComparingTo("15000");
        assertThat(result.getPending()).isEqualByComparingTo("15000");
        assertThat(result.getOverdue()).isEqualByComparingTo("5000");
        assertThat(result.getActiveLeases()).isEqualTo(1);
        assertThat(result.getPendingMaintenance()).isEqualTo(1);
        assertThat(result.getTotalTenants()).isEqualTo(1);
    }

    @Test
    void getDashboardSummary_shouldReturnZerosWhenNoProperties() {
        when(propertyRepository.findByLandlordId(landlordId)).thenReturn(List.of());

        DashboardResponseDTO result = dashboardService.getDashboardSummary(landlordId);

        assertThat(result.getTotalUnits()).isZero();
        assertThat(result.getCollected()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getPendingMaintenance()).isZero();
    }

    @Test
    void getTenantDashboard_shouldReturnDashboard() {
        when(userRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        Lease activeLease = Lease.builder()
                .id(UUID.randomUUID())
                .tenant(tenant)
                .property(property)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(6))
                .rentAmount(new BigDecimal("15000"))
                .status(LeaseStatus.ACTIVE)
                .build();
        when(leaseRepository.findByTenantIdAndStatus(tenantId, LeaseStatus.ACTIVE))
                .thenReturn(Optional.of(activeLease));

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("15000"))
                .status(PaymentStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(5))
                .build();
        when(paymentRepository.findByPropertyIdAndRentPeriodOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(propertyId),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(payment));
        when(paymentRepository.findByTenantIdOrderByPaymentDateDesc(tenantId))
                .thenReturn(List.of(payment));
        when(maintenanceRequestRepository.findByTenantId(tenantId)).thenReturn(List.of());

        TenantDashboardResponseDTO result = dashboardService.getTenantDashboard(tenantId);

        assertThat(result.getUnitNumber()).isNotNull();
        assertThat(result.getRentStatus()).isEqualTo("PENDING");
        assertThat(result.getRecentPayments()).hasSize(1);
        assertThat(result.getRecentMaintenance()).isEmpty();
    }

    @Test
    void getTenantDashboard_shouldReturnMinimalWhenNoLease() {
        when(userRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(leaseRepository.findByTenantIdAndStatus(tenantId, LeaseStatus.ACTIVE))
                .thenReturn(Optional.empty());

        TenantDashboardResponseDTO result = dashboardService.getTenantDashboard(tenantId);

        assertThat(result.getFirstName()).isEqualTo("Tom");
        assertThat(result.getUnitNumber()).isNull();
    }
}
