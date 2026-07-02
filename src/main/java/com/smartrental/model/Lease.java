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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Entity representing a lease agreement between a tenant and a landlord for a property.
 *
 * <p>A lease captures the rental terms including start/end dates, rent amount, deposit,
 * and current status. It is linked to payments and serves as the basis for rent tracking.</p>
 */
@Entity
@Table(
    name = "leases",
    indexes = {
        @Index(name = "idx_leases_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_leases_property_id", columnList = "property_id"),
        @Index(name = "idx_leases_status", columnList = "status"),
        @Index(name = "idx_leases_end_date", columnList = "end_date"),
        @Index(name = "idx_leases_deleted", columnList = "is_deleted")
    }
)
@SQLDelete(sql = "UPDATE leases SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"tenant", "property", "payments"})
public class Lease extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * The tenant occupying the property under this lease.
     */
    @NotNull(message = "Tenant is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_leases_tenant"))
    private User tenant;

    /**
     * The property being leased.
     */
    @NotNull(message = "Property is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_leases_property"))
    private Property property;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent amount must be positive")
    @Column(name = "rent_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal rentAmount;

    @Positive(message = "Deposit amount must be positive")
    @Column(name = "deposit_amount", precision = 12, scale = 2)
    private BigDecimal depositAmount;

    @NotNull(message = "Lease status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LeaseStatus status = LeaseStatus.PENDING;

    @Column(name = "lease_document_url", length = 500)
    private String leaseDocumentUrl;

    @Column(name = "terms_and_conditions", length = 5000)
    private String termsAndConditions;

    // ===========================================
    // Bidirectional Relationships
    // ===========================================

    @OneToMany(mappedBy = "lease", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    /**
     * Check if the lease is currently active (between start and end dates).
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return status == LeaseStatus.ACTIVE
                && !today.isBefore(startDate)
                && !today.isAfter(endDate);
    }

    /**
     * Check if the lease has expired.
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }
}
