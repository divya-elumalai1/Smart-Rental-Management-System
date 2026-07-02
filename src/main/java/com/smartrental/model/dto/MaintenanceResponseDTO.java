package com.smartrental.model.dto;

import com.smartrental.model.MaintenancePriority;
import com.smartrental.model.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for maintenance request details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceResponseDTO {

    private UUID id;
    private UUID tenantId;
    private String tenantName;
    private UUID propertyId;
    private String propertyAddress;
    private String title;
    private String description;
    private MaintenancePriority priority;
    private MaintenanceStatus status;
    private String imageUrl;
    private LocalDateTime resolvedAt;
    private UUID resolvedBy;
    private String resolutionNotes;
    private List<MaintenanceCommentDTO> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
