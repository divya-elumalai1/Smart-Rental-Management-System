package com.smartrental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entity representing a comment on a maintenance request.
 *
 * <p>Both tenants and landlords can add comments to a maintenance request
 * to communicate about the issue, updates, or resolution.</p>
 */
@Entity
@Table(
    name = "maintenance_comments",
    indexes = {
        @Index(name = "idx_maintcomment_request_id", columnList = "request_id"),
        @Index(name = "idx_maintcomment_user_id", columnList = "user_id"),
        @Index(name = "idx_maintcomment_created_at", columnList = "created_at")
    }
)
@SQLDelete(sql = "UPDATE maintenance_comments SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"maintenanceRequest", "user"})
public class MaintenanceComment extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * The maintenance request this comment belongs to.
     */
    @NotNull(message = "Maintenance request is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_maintcomment_request"))
    private MaintenanceRequest maintenanceRequest;

    /**
     * The user who posted this comment (tenant or landlord).
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_maintcomment_user"))
    private User user;

    @NotBlank(message = "Comment is required")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    @Column(name = "comment", nullable = false, length = 2000)
    private String comment;

    /**
     * Optional URL of an image attached to the comment.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
