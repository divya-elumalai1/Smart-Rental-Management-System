package com.smartrental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Entity representing a document uploaded to Cloudinary.
 *
 * <p>Both landlords and tenants can upload documents such as rental agreements,
 * ID proofs, NOCs, and receipts. Files are stored in Cloudinary and referenced by URL.</p>
 */
@Entity
@Table(
    name = "documents",
    indexes = {
        @Index(name = "idx_documents_user_id", columnList = "user_id"),
        @Index(name = "idx_documents_property_id", columnList = "property_id"),
        @Index(name = "idx_documents_category", columnList = "category"),
        @Index(name = "idx_documents_created_at", columnList = "created_at")
    }
)
@SQLDelete(sql = "UPDATE documents SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "property"})
public class Document extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * The user who uploaded this document.
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_documents_user"))
    private User user;

    /**
     * The property this document is associated with (optional).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id",
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_documents_property"))
    private Property property;

    @NotBlank(message = "File URL is required")
    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "public_id", length = 500)
    private String publicId;

    @NotNull(message = "Document category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private DocumentCategory category;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
}
