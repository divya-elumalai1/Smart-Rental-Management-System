package com.smartrental.model.dto;

import com.smartrental.model.MaintenancePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a maintenance request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRequestDTO {

    private UUID tenantId;

    private UUID propertyId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private MaintenancePriority priority;
    private String imageUrl;
}
