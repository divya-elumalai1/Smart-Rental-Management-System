package com.smartrental.model.dto;

import com.smartrental.model.DocumentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for document details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDTO {

    private UUID id;
    private UUID userId;
    private String userName;
    private UUID propertyId;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String publicId;
    private DocumentCategory category;
    private String description;
    private LocalDateTime createdAt;
}
