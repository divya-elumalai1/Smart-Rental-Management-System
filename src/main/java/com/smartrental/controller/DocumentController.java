package com.smartrental.controller;

import com.smartrental.model.Document;
import com.smartrental.model.DocumentCategory;
import com.smartrental.model.dto.DocumentResponseDTO;
import com.smartrental.repository.DocumentRepository;
import com.smartrental.service.DocumentService;
import com.smartrental.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for document management.
 */
@RestController
@RequestMapping("/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    /**
     * Get all documents accessible to the current user.
     * GET /api/v1/documents
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentResponseDTO>> getAllDocuments() {
        log.debug("GET all documents");
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    /**
     * Get documents for a specific property.
     * GET /api/v1/documents/property/{propertyId}
     */
    @GetMapping("/property/{propertyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentResponseDTO>> getDocumentsByProperty(@PathVariable UUID propertyId) {
        log.debug("GET documents for property: {}", propertyId);
        return ResponseEntity.ok(documentService.getDocumentsByProperty(propertyId));
    }

    /**
     * Upload a new document.
     * POST /api/v1/documents
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponseDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") DocumentCategory category,
            @RequestParam(value = "propertyId", required = false) UUID propertyId,
            @RequestParam(value = "description", required = false) String description) throws IOException {
        log.info("POST upload document: {}, category: {}", file.getOriginalFilename(), category);
        DocumentResponseDTO response = documentService.uploadDocument(file, category, propertyId, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Serve a locally-stored document file.
     * GET /api/v1/documents/{id}/file
     */
    @GetMapping("/{id}/file")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getDocumentFile(@PathVariable UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        if (document.getPublicId() == null || !document.getPublicId().startsWith("local:")) {
            // Cloudinary file — redirect to the stored URL
            if (document.getFileUrl() != null && !document.getFileUrl().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, document.getFileUrl())
                        .build();
            }
            return ResponseEntity.notFound().build();
        }

        String uniqueName = document.getPublicId().substring(6);
        Path filePath = Paths.get("uploads/documents").resolve(uniqueName).normalize();

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);
        String contentType = document.getFileType() != null ? document.getFileType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Delete a document by ID.
     * DELETE /api/v1/documents/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        log.info("DELETE document: {}", id);
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
