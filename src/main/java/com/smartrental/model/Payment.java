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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Entity representing a rent payment made by a tenant.
 *
 * <p>Payments are initiated via Razorpay and tracked through their lifecycle:
 * pending → completed / failed / cancelled. The due date drives the automated
 * reminder scheduler, and overdue payments are flagged for follow-up.</p>
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_payments_property_id", columnList = "property_id"),
        @Index(name = "idx_payments_status", columnList = "status"),
        @Index(name = "idx_payments_due_date", columnList = "due_date"),
        @Index(name = "idx_payments_payment_date", columnList = "payment_date"),
        @Index(name = "idx_payments_razorpay_order", columnList = "razorpay_order_id")
    }
)
@SQLDelete(sql = "UPDATE payments SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"tenant", "property", "lease"})
public class Payment extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * The tenant who made (or is responsible for) this payment.
     */
    @NotNull(message = "Tenant is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_payments_tenant"))
    private User tenant;

    /**
     * The property this payment is associated with.
     */
    @NotNull(message = "Property is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_payments_property"))
    private Property property;

    /**
     * The lease this payment belongs to (optional — some payments may be ad-hoc).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lease_id",
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_payments_lease"))
    private Lease lease;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    /**
     * Razorpay order ID created when the payment is initiated.
     */
    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    /**
     * Razorpay payment ID returned after successful payment.
     */
    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    /**
     * Razorpay signature for verification.
     */
    @Column(name = "razorpay_signature", length = 500)
    private String razorpaySignature;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * The date the payment was actually completed (null until paid).
     */
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    /**
     * The due date for this payment — drives the reminder scheduler.
     */
    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Rent month this payment covers (e.g. 2024-06-01 for June 2024).
     */
    @Column(name = "rent_period")
    private LocalDate rentPeriod;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    /** Manual payment channel: UPI, Cash, Bank Transfer, Cheque, etc. */
    @Column(name = "payment_mode", length = 30)
    private String paymentMode;

    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Check if this payment is overdue (past due date and not completed).
     */
    public boolean isOverdue() {
        return status != PaymentStatus.COMPLETED
                && status != PaymentStatus.CANCELLED
                && status != PaymentStatus.REFUNDED
                && LocalDate.now().isAfter(dueDate);
    }
}
