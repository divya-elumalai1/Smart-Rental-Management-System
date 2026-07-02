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
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
 * Entity representing a rental property owned by a landlord.
 *
 * <p>A property contains details such as address, rent, number of bedrooms/bathrooms,
 * amenities, and current availability status. It serves as the central entity linked to
 * leases, payments, maintenance requests, and documents.</p>
 */
@Entity
@Table(
    name = "properties",
    indexes = {
        @Index(name = "idx_properties_landlord_id", columnList = "landlord_id"),
        @Index(name = "idx_properties_status", columnList = "status"),
        @Index(name = "idx_properties_city", columnList = "city"),
        @Index(name = "idx_properties_deleted", columnList = "is_deleted")
    }
)
@SQLDelete(sql = "UPDATE properties SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"landlord", "leases", "maintenanceRequests", "documents"})
public class Property extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * The landlord who owns this property.
     */
    @NotNull(message = "Landlord is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "landlord_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_properties_landlord"))
    private User landlord;

    @Size(max = 20, message = "Unit number must not exceed 20 characters")
    @Column(name = "unit_number", length = 20)
    private String unitNumber;

    @Size(max = 50, message = "Floor label must not exceed 50 characters")
    @Column(name = "floor_label", length = 50)
    private String floorLabel;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent amount must be positive")
    @Column(name = "rent_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal rentAmount;

    @Positive(message = "Deposit must be positive")
    @Column(name = "deposit", precision = 12, scale = 2)
    private BigDecimal deposit;

    @Column(name = "bedrooms")
    private Integer bedrooms;

    @Column(name = "bathrooms", precision = 3, scale = 1)
    private BigDecimal bathrooms;

    @Column(name = "area_sqft")
    private Integer areaSqft;

    @Column(name = "furnishing_status", length = 20)
    private String furnishingStatus;

    @Size(max = 2000, message = "Amenities must not exceed 2000 characters")
    @Column(name = "amenities", length = 2000)
    private String amenities;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(name = "description", length = 2000)
    private String description;

    @NotNull(message = "Property status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.AVAILABLE;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // ===========================================
    // Bidirectional Relationships
    // ===========================================

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Lease> leases = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    /**
     * Convenience helper to associate a lease with this property.
     */
    public void addLease(Lease lease) {
        leases.add(lease);
        lease.setProperty(this);
    }

    /**
     * Convenience helper to disassociate a lease from this property.
     */
    public void removeLease(Lease lease) {
        leases.remove(lease);
        lease.setProperty(null);
    }
}
