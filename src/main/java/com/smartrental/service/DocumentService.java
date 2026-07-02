package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.Document;
import com.smartrental.model.DocumentCategory;
import com.smartrental.model.Property;
import com.smartrental.model.User;
import com.smartrental.model.dto.DocumentResponseDTO;
import com.smartrental.repository.DocumentRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import com.smartrental.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing document uploads and retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final SecurityUtil securityUtil;
    private final CloudinaryService cloudinaryService;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "application/pdf",
        "image/jpeg", "image/png", "image/gif",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final String LOCAL_UPLOAD_DIR = "uploads/documents";

    /**
     * Upload a new document.
     */
    public DocumentResponseDTO uploadDocument(MultipartFile file, DocumentCategory category,
                                               UUID propertyId, String description) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed: " + file.getContentType());
        }

        User currentUser = securityUtil.getCurrentUser();

        Document.DocumentBuilder builder = Document.builder()
                .user(currentUser)
                .fileUrl("")
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .category(category)
                .description(description)
                .publicId("");

        if (propertyId != null) {
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));
            builder.property(property);
        }

        Document document = documentRepository.save(builder.build());

        String fileUrl = "";
        String publicId = "";

        try {
            Map result = cloudinaryService.upload(file, "documents");
            fileUrl = (String) result.get("url");
            publicId = (String) result.get("public_id");
        } catch (Exception e) {
            log.warn("Cloudinary upload failed, falling back to local storage: {}", e.getMessage());
        }

        // Fallback to local storage if Cloudinary returned empty or failed
        if (fileUrl == null || fileUrl.isEmpty()) {
            String uniqueName = document.getId() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(LOCAL_UPLOAD_DIR);
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(uniqueName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            fileUrl = "/api/v1/documents/" + document.getId() + "/file";
            publicId = "local:" + uniqueName;
            log.info("Saved file locally at: {}", targetPath);
        }

        if (fileUrl != null && !fileUrl.isEmpty()) {
            document.setFileUrl(fileUrl);
            document.setPublicId(publicId);
            document = documentRepository.save(document);
        }

        log.info("Document uploaded: {} by user: {}", document.getFileName(), currentUser.getEmail());
        return toResponseDTO(document);
    }

    /**
     * Get all documents accessible to the current user.
     * Admin/Landlord sees all; Tenant sees only their own.
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getAllDocuments() {
        User currentUser = securityUtil.getCurrentUser();
        List<Document> documents;

        switch (currentUser.getRole()) {
            case ADMIN, OWNER -> documents = documentRepository.findAll();
            case TENANT -> documents = documentRepository.findByUserId(currentUser.getId());
            default -> documents = List.of();
        }

        return documents.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get documents for a specific property.
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getDocumentsByProperty(UUID propertyId) {
        return documentRepository.findByPropertyId(propertyId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete a document by ID.
     */
    public void deleteDocument(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        if (document.getPublicId() != null && document.getPublicId().startsWith("local:")) {
            String uniqueName = document.getPublicId().substring(6);
            Path targetPath = Paths.get(LOCAL_UPLOAD_DIR).resolve(uniqueName);
            try {
                Files.deleteIfExists(targetPath);
                log.info("Deleted local file: {}", targetPath);
            } catch (IOException e) {
                log.warn("Failed to delete local file: {}", e.getMessage());
            }
        } else {
            try {
                cloudinaryService.delete(document.getPublicId());
            } catch (Exception e) {
                log.warn("Cloudinary delete failed, removing DB record anyway: {}", e.getMessage());
            }
        }

        documentRepository.delete(document);
        log.info("Document deleted: {} (id: {})", document.getFileName(), id);
    }

    /**
     * Map Document entity to DTO.
     */
    private DocumentResponseDTO toResponseDTO(Document document) {
        DocumentResponseDTO dto = DocumentResponseDTO.builder()
                .id(document.getId())
                .userId(document.getUser().getId())
                .userName(document.getUser().getFirstName() + " " + document.getUser().getLastName())
                .fileUrl(document.getFileUrl())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .publicId(document.getPublicId())
                .category(document.getCategory())
                .description(document.getDescription())
                .createdAt(document.getCreatedAt())
                .build();

        if (document.getProperty() != null) {
            dto.setPropertyId(document.getProperty().getId());
        }

        return dto;
    }
}
