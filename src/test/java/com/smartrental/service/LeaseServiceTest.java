package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.Lease;
import com.smartrental.model.LeaseStatus;
import com.smartrental.model.Property;
import com.smartrental.model.PropertyStatus;
import com.smartrental.model.Role;
import com.smartrental.model.User;
import com.smartrental.model.dto.LeaseRequestDTO;
import com.smartrental.model.dto.LeaseResponseDTO;
import com.smartrental.repository.LeaseRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaseService}.
 */
@ExtendWith(MockitoExtension.class)
class LeaseServiceTest {

    @Mock
    private LeaseRepository leaseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private LeaseService leaseService;

    private UUID tenantId;
    private User tenant;
    private UUID propertyId;
    private Property property;
    private UUID leaseId;
    private Lease lease;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = User.builder()
                .id(tenantId)
                .firstName("Tom")
                .lastName("Tenant")
                .email("tom@example.com")
                .role(Role.TENANT)
                .build();
        propertyId = UUID.randomUUID();
        property = Property.builder()
                .id(propertyId)
                .address("123 Main St")
                .city("Pune")
                .rentAmount(new BigDecimal("15000"))
                .status(PropertyStatus.AVAILABLE)
                .build();
        leaseId = UUID.randomUUID();
        lease = Lease.builder()
                .id(leaseId)
                .tenant(tenant)
                .property(property)
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(11))
                .rentAmount(new BigDecimal("15000"))
                .depositAmount(new BigDecimal("30000"))
                .status(LeaseStatus.ACTIVE)
                .build();
    }

    @Test
    void create_shouldSaveLeaseAndReturnDTO() {
        LeaseRequestDTO request = LeaseRequestDTO.builder()
                .tenantId(tenantId)
                .propertyId(propertyId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .rentAmount(new BigDecimal("15000"))
                .build();
        when(userRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(leaseRepository.save(any(Lease.class))).thenAnswer(inv -> {
            Lease l = inv.getArgument(0);
            l.setId(leaseId);
            return l;
        });

        LeaseResponseDTO result = leaseService.create(request);

        assertThat(result.getId()).isEqualTo(leaseId);
        assertThat(result.getTenantName()).isEqualTo("Tom Tenant");
        assertThat(result.getStatus()).isEqualTo(LeaseStatus.PENDING);
        verify(leaseRepository).save(any(Lease.class));
    }

    @Test
    void create_shouldThrowWhenTenantNotFound() {
        LeaseRequestDTO request = LeaseRequestDTO.builder()
                .tenantId(tenantId)
                .propertyId(propertyId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .rentAmount(new BigDecimal("15000"))
                .build();
        when(userRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaseService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(leaseRepository, never()).save(any());
    }

    @Test
    void findActiveLeases_shouldReturnOnlyCurrentlyActive() {
        Lease expiredActive = Lease.builder()
                .id(UUID.randomUUID())
                .tenant(tenant)
                .property(property)
                .startDate(LocalDate.now().minusMonths(20))
                .endDate(LocalDate.now().minusMonths(8)) // past end date
                .rentAmount(new BigDecimal("15000"))
                .status(LeaseStatus.ACTIVE)
                .build();
        when(leaseRepository.findByStatus(LeaseStatus.ACTIVE))
                .thenReturn(List.of(lease, expiredActive));

        List<LeaseResponseDTO> results = leaseService.findActiveLeases();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(leaseId);
        assertThat(results.get(0).getCurrentlyActive()).isTrue();
    }

    @Test
    void getById_shouldReturnLease() {
        when(leaseRepository.findById(leaseId)).thenReturn(Optional.of(lease));

        LeaseResponseDTO result = leaseService.getById(leaseId);

        assertThat(result.getId()).isEqualTo(leaseId);
        assertThat(result.getRentAmount()).isEqualByComparingTo("15000");
    }

    @Test
    void delete_shouldSoftDelete() {
        when(leaseRepository.findById(leaseId)).thenReturn(Optional.of(lease));

        leaseService.delete(leaseId);

        assertThat(lease.getDeleted()).isTrue();
        verify(leaseRepository).save(lease);
    }
}
