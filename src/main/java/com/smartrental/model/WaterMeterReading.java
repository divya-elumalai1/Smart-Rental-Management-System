package com.smartrental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing water meter readings for rental units.
 */
@Entity
@Table(
    name = "water_meter_readings",
    indexes = {
        @Index(name = "idx_water_meter_property_id", columnList = "property_id"),
        @Index(name = "idx_water_meter_unit_number", columnList = "unit_number"),
        @Index(name = "idx_water_meter_reading_date", columnList = "reading_date")
    }
)
@SQLDelete(sql = "UPDATE water_meter_readings SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"property"})
public class WaterMeterReading extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NotNull(message = "Property is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_water_meter_property"))
    private Property property;

    @NotNull(message = "Unit number is required")
    @Column(name = "unit_number", nullable = false, length = 20)
    private String unitNumber;

    @NotNull(message = "Previous reading is required")
    @Column(name = "previous_reading", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal previousReading = BigDecimal.ZERO;

    @NotNull(message = "Current reading is required")
    @Column(name = "current_reading", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentReading;

    @NotNull(message = "Units consumed is required")
    @Column(name = "units_consumed", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitsConsumed;

    @NotNull(message = "Reading date is required")
    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @Column(name = "meter_photo_url", length = 500)
    private String meterPhotoUrl;

    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Calculate water bill based on units consumed.
     * Rate: ₹8 per unit
     */
    public BigDecimal calculateWaterBill() {
        return unitsConsumed.multiply(BigDecimal.valueOf(8));
    }

    /**
     * Calculate total bill (rent + water).
     */
    public BigDecimal calculateTotalBill(BigDecimal rentAmount) {
        return rentAmount.add(calculateWaterBill());
    }
}
