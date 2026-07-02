package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.MaintenanceComment;
import com.smartrental.model.MaintenancePriority;
import com.smartrental.model.MaintenanceRequest;
import com.smartrental.model.MaintenanceStatus;
import com.smartrental.model.Property;
import com.smartrental.model.Role;
import com.smartrental.model.User;
import com.smartrental.model.dto.MaintenanceCommentDTO;
import com.smartrental.model.dto.MaintenanceRequestDTO;
import com.smartrental.model.dto.MaintenanceResponseDTO;
import com.smartrental.repository.MaintenanceCommentRepository;
import com.smartrental.repository.MaintenanceRequestRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MaintenanceService}.
 */
@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceRequestRepository requestRepository;
    @Mock
    private MaintenanceCommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    private UUID tenantId;
    private User tenant;
    private UUID propertyId;
    private Property property;
    private UUID requestId;
    private MaintenanceRequest request;

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
                .build();
        requestId = UUID.randomUUID();
        request = MaintenanceRequest.builder()
                .id(requestId)
                .tenant(tenant)
                .property(property)
                .title("Leaky tap")
                .description("Kitchen tap is leaking")
                .priority(MaintenancePriority.MEDIUM)
                .status(MaintenanceStatus.PENDING)
                .build();
    }

    @Test
    void create_shouldSaveRequestWithPendingStatus() {
        MaintenanceRequestDTO dto = MaintenanceRequestDTO.builder()
                .tenantId(tenantId)
                .propertyId(propertyId)
                .title("Leaky tap")
                .description("Kitchen tap is leaking")
                .priority(MaintenancePriority.HIGH)
                .build();
        when(userRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(requestRepository.save(any(MaintenanceRequest.class))).thenAnswer(inv -> {
            MaintenanceRequest r = inv.getArgument(0);
            r.setId(requestId);
            return r;
        });
        when(commentRepository.findByMaintenanceRequestId(requestId)).thenReturn(List.of());

        MaintenanceResponseDTO result = maintenanceService.create(dto);

        assertThat(result.getStatus()).isEqualTo(MaintenanceStatus.PENDING);
        assertThat(result.getPriority()).isEqualTo(MaintenancePriority.HIGH);
        verify(requestRepository).save(any(MaintenanceRequest.class));
    }

    @Test
    void updateStatus_shouldResolveAndSetResolvedAt() {
        UUID resolverId = UUID.randomUUID();
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(MaintenanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(commentRepository.findByMaintenanceRequestId(requestId)).thenReturn(List.of());

        MaintenanceResponseDTO result = maintenanceService.updateStatus(
                requestId, MaintenanceStatus.RESOLVED, resolverId, "Fixed the tap");

        assertThat(result.getStatus()).isEqualTo(MaintenanceStatus.RESOLVED);
        assertThat(result.getResolvedAt()).isNotNull();
        assertThat(result.getResolvedBy()).isEqualTo(resolverId);
        assertThat(result.getResolutionNotes()).isEqualTo("Fixed the tap");
    }

    @Test
    void addComment_shouldPersistComment() {
        MaintenanceCommentDTO commentDTO = MaintenanceCommentDTO.builder()
                .maintenanceRequestId(requestId)
                .userId(tenantId)
                .comment("Still leaking")
                .build();
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(commentRepository.save(any(MaintenanceComment.class))).thenAnswer(inv -> {
            MaintenanceComment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        MaintenanceCommentDTO result = maintenanceService.addComment(commentDTO);

        assertThat(result.getComment()).isEqualTo("Still leaking");
        assertThat(result.getUserName()).isEqualTo("Tom Tenant");
        verify(commentRepository).save(any(MaintenanceComment.class));
    }

    @Test
    void addComment_shouldThrowWhenRequestNotFound() {
        MaintenanceCommentDTO commentDTO = MaintenanceCommentDTO.builder()
                .maintenanceRequestId(requestId)
                .userId(tenantId)
                .comment("Still leaking")
                .build();
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.addComment(commentDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void getByTenant_shouldReturnRequestsForTenant() {
        when(requestRepository.findByTenantId(tenantId)).thenReturn(List.of(request));
        when(commentRepository.findByMaintenanceRequestId(requestId)).thenReturn(List.of());

        List<MaintenanceResponseDTO> results = maintenanceService.findByTenant(tenantId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTenantName()).isEqualTo("Tom Tenant");
    }
}
