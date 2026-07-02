package com.smartrental.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
 * Entity representing a maintenance request raised by a tenant.
 *
 * <p>Tenants raise requests for repairs or issues in their rented property.
 * Landlords track and update the status, and both parties can add comments.
 * The tenant receives notifications when the status changes.</p>
 */
@Entity
@Table(
    name = "maintenance_requests",
    indexes = {
        @Index(name = "idx_maintreq_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_maintreq_property_id", columnList = "property_id"),
        @Index(name = "idx_maintreq_status", columnList = "status"),
        @Index(name = "idx_maintreq_priority", columnList = "priority"),
        @Index(name = "idx_maintreq_created_at", columnList = "created_at")
    }
)
@SQLDelete(sql = "UPDATE maintenance_requests SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"tenant", "property", "comments"})
public class MaintenanceRequest extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * The tenant who raised this request.
     */
    @NotNull(message = "Tenant is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_maintreq_tenant"))
    private User tenant;

    /**
     * The property the request pertains to.
     */
    @NotNull(message = "Property is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_maintreq_property"))
    private Property property;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Column(name = "description", nullable = false, length = 5000)
    private String description;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private MaintenancePriority priority = MaintenancePriority.MEDIUM;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.PENDING;

    /**
     * URL of any image attached to the request (e.g. photo of the issue).
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * When the request was resolved (null until resolved).
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * User who resolved the request (landlord or admin).
     */
    @Column(name = "resolved_by", columnDefinition = "uuid")
    private UUID resolvedBy;

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;

    // ===========================================
    // Bidirectional Relationships
    // ===========================================

    @OneToMany(mappedBy = "maintenanceRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MaintenanceComment> comments = new ArrayList<>();

    /**
     * Convenience helper to add a comment to this request.
     */
    public void addComment(MaintenanceComment comment) {
        comments.add(comment);
        comment.setMaintenanceRequest(this);
    }

    /**
     * Convenience helper to remove a comment from this request.
     */
    public void removeComment(MaintenanceComment comment) {
        comments.remove(comment);
        comment.setMaintenanceRequest(null);
    }
}
