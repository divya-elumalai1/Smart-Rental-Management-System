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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private LeaseRepository leaseRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TenantService tenantService;

    private UUID landlordId;
    private UUID tenantId;
    private UUID propertyId;
    private UUID leaseId;
    private User landlord;
    private User tenant;
    private Property property;
    private Lease lease;

    @BeforeEach
    void setUp() {
        landlordId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        leaseId = UUID.randomUUID();

        landlord = User.builder()
                .id(landlordId)
                .firstName("Owner")
                .lastName("Landlord")
                .email("owner@example.com")
                .role(Role.OWNER)
                .build();

        tenant = User.builder()
                .id(tenantId)
                .firstName("Tom")
                .lastName("Tenant")
                .email("tom@example.com")
                .phoneNumber("9000000001")
                .role(Role.TENANT)
                .build();

        property = Property.builder()
                .id(propertyId)
                .landlord(landlord)
                .unitNumber("G1")
                .floorLabel("Ground Floor")
                .address("Sapthagiri Residency, Unit G1")
                .city("Bengaluru")
                .state("Karnataka")
                .rentAmount(new BigDecimal("10000"))
                .deposit(new BigDecimal("20000"))
                .status(PropertyStatus.AVAILABLE)
                .build();

        lease = Lease.builder()
                .id(leaseId)
                .tenant(tenant)
                .property(property)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .rentAmount(new BigDecimal("10000"))
                .depositAmount(new BigDecimal("20000"))
                .status(LeaseStatus.ACTIVE)
                .build();
    }

    @Test
    void listTenants_shouldReturnActiveTenants() {
        property.setStatus(PropertyStatus.OCCUPIED);
        when(propertyRepository.findByLandlordId(landlordId)).thenReturn(List.of(property));
        when(leaseRepository.findByPropertyIdAndStatus(propertyId, LeaseStatus.ACTIVE))
                .thenReturn(Optional.of(lease));

        List<TenantSummaryDTO> result = tenantService.listTenants(landlordId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTenantName()).isEqualTo("Tom Tenant");
        assertThat(result.get(0).getUnitNumber()).isEqualTo("G1");
    }

    @Test
    void listTenants_shouldSkipUnitsWithoutActiveLease() {
        property.setStatus(PropertyStatus.AVAILABLE);
        when(propertyRepository.findByLandlordId(landlordId)).thenReturn(List.of(property));
        when(leaseRepository.findByPropertyIdAndStatus(propertyId, LeaseStatus.ACTIVE))
                .thenReturn(Optional.empty());

        List<TenantSummaryDTO> result = tenantService.listTenants(landlordId);

        assertThat(result).isEmpty();
    }

    @Test
    void assignTenant_shouldCreateUserLeaseAndPayment() {
        TenantAssignRequestDTO request = TenantAssignRequestDTO.builder()
                .unitNumber("G1")
                .firstName("New")
                .lastName("Tenant")
                .email("new@example.com")
                .phoneNumber("9000000099")
                .rentAmount(new BigDecimal("10000"))
                .deposit(new BigDecimal("20000"))
                .leaseStart(LocalDate.of(2025, 6, 1))
                .password("password123")
                .build();

        when(propertyRepository.findByLandlordIdAndUnitNumber(landlordId, "G1"))
                .thenReturn(Optional.of(property));
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("9000000099")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(tenantId);
            return u;
        });
        when(leaseRepository.save(any(Lease.class))).thenAnswer(inv -> {
            Lease l = inv.getArgument(0);
            l.setId(leaseId);
            return l;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(mock(Payment.class));

        TenantSummaryDTO result = tenantService.assignTenant(landlordId, request);

        assertThat(result.getTenantName()).isEqualTo("New Tenant");
        assertThat(result.getUnitNumber()).isEqualTo("G1");
        assertThat(result.getRentAmount()).isEqualByComparingTo("10000");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-pass");

        verify(leaseRepository).save(any(Lease.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void assignTenant_shouldThrowWhenUnitNotAvailable() {
        property.setStatus(PropertyStatus.OCCUPIED);
        TenantAssignRequestDTO request = TenantAssignRequestDTO.builder()
                .unitNumber("G1")
                .firstName("New")
                .lastName("Tenant")
                .email("new@example.com")
                .phoneNumber("9000000099")
                .rentAmount(new BigDecimal("10000"))
                .leaseStart(LocalDate.now())
                .password("password123")
                .build();

        when(propertyRepository.findByLandlordIdAndUnitNumber(landlordId, "G1"))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() -> tenantService.assignTenant(landlordId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");

        verify(userRepository, never()).save(any(User.class));
        verify(leaseRepository, never()).save(any(Lease.class));
    }

    @Test
    void assignTenant_shouldThrowWhenUnitUnderConstruction() {
        property.setStatus(PropertyStatus.UNDER_CONSTRUCTION);
        TenantAssignRequestDTO request = TenantAssignRequestDTO.builder()
                .unitNumber("G1")
                .firstName("New")
                .lastName("Tenant")
                .email("new@example.com")
                .phoneNumber("9000000099")
                .rentAmount(new BigDecimal("10000"))
                .leaseStart(LocalDate.now())
                .password("password123")
                .build();

        when(propertyRepository.findByLandlordIdAndUnitNumber(landlordId, "G1"))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() -> tenantService.assignTenant(landlordId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void assignTenant_shouldThrowWhenUnitUnderMaintenance() {
        property.setStatus(PropertyStatus.MAINTENANCE);
        TenantAssignRequestDTO request = TenantAssignRequestDTO.builder()
                .unitNumber("G1")
                .firstName("New")
                .lastName("Tenant")
                .email("new@example.com")
                .phoneNumber("9000000099")
                .rentAmount(new BigDecimal("10000"))
                .leaseStart(LocalDate.now())
                .password("password123")
                .build();

        when(propertyRepository.findByLandlordIdAndUnitNumber(landlordId, "G1"))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() -> tenantService.assignTenant(landlordId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void assignTenant_shouldThrowWhenEmailExists() {
        TenantAssignRequestDTO request = TenantAssignRequestDTO.builder()
                .unitNumber("G1")
                .firstName("New")
                .lastName("Tenant")
                .email("existing@example.com")
                .phoneNumber("9000000099")
                .rentAmount(new BigDecimal("10000"))
                .leaseStart(LocalDate.now())
                .password("password123")
                .build();

        when(propertyRepository.findByLandlordIdAndUnitNumber(landlordId, "G1"))
                .thenReturn(Optional.of(property));
        when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> tenantService.assignTenant(landlordId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void assignTenant_shouldThrowWhenPhoneExists() {
        TenantAssignRequestDTO request = TenantAssignRequestDTO.builder()
                .unitNumber("G1")
                .firstName("New")
                .lastName("Tenant")
                .email("new@example.com")
                .phoneNumber("9000000099")
                .rentAmount(new BigDecimal("10000"))
                .leaseStart(LocalDate.now())
                .password("password123")
                .build();

        when(propertyRepository.findByLandlordIdAndUnitNumber(landlordId, "G1"))
                .thenReturn(Optional.of(property));
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("9000000099")).thenReturn(true);

        assertThatThrownBy(() -> tenantService.assignTenant(landlordId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number already registered");
    }

    @Test
    void updateTenant_shouldModifyTenantAndLease() {
        property.setStatus(PropertyStatus.OCCUPIED);
        when(leaseRepository.findById(leaseId)).thenReturn(Optional.of(lease));

        TenantUpdateRequestDTO request = TenantUpdateRequestDTO.builder()
                .firstName("Tommy")
                .lastName("Tenant")
                .email("tommy@example.com")
                .phoneNumber("9000000002")
                .rentAmount(new BigDecimal("12000"))
                .deposit(new BigDecimal("24000"))
                .leaseStart(LocalDate.of(2025, 2, 1))
                .leaseEnd(LocalDate.of(2027, 1, 31))
                .build();

        TenantSummaryDTO result = tenantService.updateTenant(landlordId, leaseId, request);

        assertThat(result.getTenantName()).isEqualTo("Tommy Tenant");
        assertThat(result.getEmail()).isEqualTo("tommy@example.com");
        assertThat(result.getRentAmount()).isEqualByComparingTo("12000");
        verify(userRepository).save(any(User.class));
        verify(leaseRepository).save(any(Lease.class));
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void updateTenant_shouldThrowWhenLeaseNotOwned() {
        User otherLandlord = User.builder()
                .id(UUID.randomUUID())
                .firstName("Other")
                .role(Role.OWNER)
                .build();
        Property otherProperty = Property.builder()
                .id(UUID.randomUUID())
                .landlord(otherLandlord)
                .build();
        lease.setProperty(otherProperty);

        when(leaseRepository.findById(leaseId)).thenReturn(Optional.of(lease));

        TenantUpdateRequestDTO request = TenantUpdateRequestDTO.builder()
                .firstName("Tommy")
                .lastName("Tenant")
                .email("tommy@example.com")
                .phoneNumber("9000000002")
                .build();

        assertThatThrownBy(() -> tenantService.updateTenant(landlordId, leaseId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void removeTenant_shouldTerminateLeaseAndFreeProperty() {
        property.setStatus(PropertyStatus.OCCUPIED);
        when(leaseRepository.findById(leaseId)).thenReturn(Optional.of(lease));

        tenantService.removeTenant(landlordId, leaseId);

        assertThat(lease.getStatus()).isEqualTo(LeaseStatus.TERMINATED);
        assertThat(property.getStatus()).isEqualTo(PropertyStatus.AVAILABLE);
        verify(leaseRepository).save(lease);
        verify(propertyRepository).save(property);
    }

    @Test
    void resetTenantPassword_shouldEncodeAndSave() {
        property.setStatus(PropertyStatus.OCCUPIED);
        when(propertyRepository.findByLandlordIdAndUnitNumber(landlordId, "G1"))
                .thenReturn(Optional.of(property));
        when(leaseRepository.findByPropertyIdAndStatus(propertyId, LeaseStatus.ACTIVE))
                .thenReturn(Optional.of(lease));
        when(passwordEncoder.encode("newSecret123")).thenReturn("encoded-new-pass");

        tenantService.resetTenantPassword(landlordId, "G1", "newSecret123");

        assertThat(tenant.getPassword()).isEqualTo("encoded-new-pass");
        verify(userRepository).save(tenant);
    }

    @Test
    void resetTenantPassword_shouldThrowWhenUnitNotFound() {
        when(propertyRepository.findByLandlordIdAndUnitNumber(landlordId, "G1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.resetTenantPassword(landlordId, "G1", "newPass"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("G1");
    }

    @Test
    void resetTenantPassword_shouldThrowWhenNoActiveLease() {
        when(propertyRepository.findByLandlordIdAndUnitNumber(landlordId, "G1"))
                .thenReturn(Optional.of(property));
        when(leaseRepository.findByPropertyIdAndStatus(propertyId, LeaseStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.resetTenantPassword(landlordId, "G1", "newPass"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Active lease");
    }
}
