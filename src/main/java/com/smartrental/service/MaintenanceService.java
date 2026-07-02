package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.MaintenanceComment;
import com.smartrental.model.MaintenancePriority;
import com.smartrental.model.MaintenanceRequest;
import com.smartrental.model.MaintenanceStatus;
import com.smartrental.model.Property;
import com.smartrental.model.User;
import com.smartrental.model.dto.MaintenanceCommentDTO;
import com.smartrental.model.dto.MaintenanceRequestDTO;
import com.smartrental.model.dto.MaintenanceResponseDTO;
import com.smartrental.repository.MaintenanceCommentRepository;
import com.smartrental.repository.MaintenanceRequestRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service handling maintenance request creation, status updates, comments, and lookups.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MaintenanceService {

    private final MaintenanceRequestRepository requestRepository;
    private final MaintenanceCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final EmailService emailService;

    /**
     * Create a new maintenance request.
     */
    public MaintenanceResponseDTO create(MaintenanceRequestDTO request) {
        log.info("Creating maintenance request '{}' for tenant {}", request.getTitle(), request.getTenantId());

        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getTenantId()));
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

        MaintenanceRequest maintenanceRequest = MaintenanceRequest.builder()
                .tenant(tenant)
                .property(property)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : MaintenancePriority.MEDIUM)
                .status(MaintenanceStatus.PENDING)
                .imageUrl(request.getImageUrl())
                .build();

        MaintenanceRequest saved = requestRepository.save(maintenanceRequest);
        log.info("Created maintenance request with ID: {}", saved.getId());

        try {
            emailService.sendMaintenanceRequestConfirmation(
                tenant, saved.getId().toString(), saved.getTitle(), property.getAddress()
            );
            User landlord = property.getLandlord();
            if (landlord != null) {
                emailService.sendMaintenanceNotificationToLandlord(
                    landlord, tenant.getFullName(), saved.getId().toString(),
                    saved.getTitle(), property.getAddress(),
                    saved.getPriority().name()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send maintenance notification emails: {}", e.getMessage());
        }

        return toResponseDTO(saved);
    }

    /**
     * Update the status of a maintenance request.
     */
    public MaintenanceResponseDTO updateStatus(UUID id, MaintenanceStatus status, UUID resolvedBy, String resolutionNotes) {
        log.info("Updating maintenance request {} status to {}", id, status);

        MaintenanceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRequest", id));

        request.setStatus(status);

        if (status == MaintenanceStatus.RESOLVED) {
            request.setResolvedAt(LocalDateTime.now());
            if (resolvedBy != null) {
                request.setResolvedBy(resolvedBy);
            }
            if (resolutionNotes != null) {
                request.setResolutionNotes(resolutionNotes);
            }
        }

        MaintenanceRequest updated = requestRepository.save(request);
        log.info("Maintenance request {} updated to {}", updated.getId(), status);

        try {
            emailService.sendMaintenanceStatusUpdate(
                updated.getTenant(), updated.getId().toString(), updated.getTitle(),
                status.name(), resolutionNotes != null ? resolutionNotes : ""
            );
        } catch (Exception e) {
            log.warn("Failed to send maintenance status update email: {}", e.getMessage());
        }

        return toResponseDTO(updated);
    }

    /**
     * Add a comment to a maintenance request.
     */
    public MaintenanceCommentDTO addComment(MaintenanceCommentDTO commentDTO) {
        log.info("Adding comment to maintenance request {}", commentDTO.getMaintenanceRequestId());

        MaintenanceRequest request = requestRepository.findById(commentDTO.getMaintenanceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MaintenanceRequest", commentDTO.getMaintenanceRequestId()));
        User user = userRepository.findById(commentDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", commentDTO.getUserId()));

        MaintenanceComment comment = MaintenanceComment.builder()
                .maintenanceRequest(request)
                .user(user)
                .comment(commentDTO.getComment())
                .imageUrl(commentDTO.getImageUrl())
                .build();

        MaintenanceComment saved = commentRepository.save(comment);
        log.info("Created comment with ID: {}", saved.getId());
        return toCommentDTO(saved);
    }

    /**
     * Get a single maintenance request by ID (including its comments).
     */
    @Transactional(readOnly = true)
    public MaintenanceResponseDTO getById(UUID id) {
        MaintenanceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRequest", id));
        return toResponseDTO(request);
    }

    /**
     * Find all maintenance requests.
     */
    @Transactional(readOnly = true)
    public List<MaintenanceResponseDTO> findAll() {
        return requestRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find all maintenance requests for a given tenant.
     */
    @Transactional(readOnly = true)
    public List<MaintenanceResponseDTO> findByTenant(UUID tenantId) {
        return requestRepository.findByTenantId(tenantId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find all maintenance requests for a given property.
     */
    @Transactional(readOnly = true)
    public List<MaintenanceResponseDTO> findByProperty(UUID propertyId) {
        return requestRepository.findByPropertyId(propertyId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ===========================================
    // Mapping
    // ===========================================

    private MaintenanceResponseDTO toResponseDTO(MaintenanceRequest request) {
        List<MaintenanceCommentDTO> comments = commentRepository
                .findByMaintenanceRequestId(request.getId()).stream()
                .map(this::toCommentDTO)
                .toList();

        return MaintenanceResponseDTO.builder()
                .id(request.getId())
                .tenantId(request.getTenant().getId())
                .tenantName(request.getTenant().getFullName())
                .propertyId(request.getProperty().getId())
                .propertyAddress(request.getProperty().getAddress())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .imageUrl(request.getImageUrl())
                .resolvedAt(request.getResolvedAt())
                .resolvedBy(request.getResolvedBy())
                .resolutionNotes(request.getResolutionNotes())
                .comments(comments)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    private MaintenanceCommentDTO toCommentDTO(MaintenanceComment comment) {
        return MaintenanceCommentDTO.builder()
                .id(comment.getId())
                .maintenanceRequestId(comment.getMaintenanceRequest().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFullName())
                .comment(comment.getComment())
                .imageUrl(comment.getImageUrl())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
