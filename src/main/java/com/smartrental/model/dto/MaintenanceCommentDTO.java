package com.smartrental.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a comment on a maintenance request. Used both as a request
 * (when creating a comment) and as a response (when listing comments).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceCommentDTO {

    private UUID id;

    @NotNull(message = "Maintenance request ID is required")
    private UUID maintenanceRequestId;

    private UUID userId;
    private String userName;

    @NotBlank(message = "Comment is required")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;

    private String imageUrl;
    private LocalDateTime createdAt;
}
