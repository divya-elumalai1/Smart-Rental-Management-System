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
 * Entity representing a rent reminder sent to a tenant.
 *
 * <p>The scheduled reminder job (Feature 6) creates these records as it sends
 * email/SMS reminders at 7 days, 3 days, on the due date, and after overdue.
 * Each reminder is logged here for audit and deduplication.</p>
 */
@Entity
@Table(
    name = "reminders",
    indexes = {
        @Index(name = "idx_reminders_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_reminders_due_date", columnList = "due_date"),
        @Index(name = "idx_reminders_type", columnList = "type"),
        @Index(name = "idx_reminders_status", columnList = "status"),
        @Index(name = "idx_reminders_sent_at", columnList = "sent_at")
    }
)
@SQLDelete(sql = "UPDATE reminders SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"tenant"})
public class Reminder extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * The tenant this reminder was sent to.
     */
    @NotNull(message = "Tenant is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_reminders_tenant"))
    private User tenant;

    /**
     * The payment this reminder is associated with.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id",
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_reminders_payment"))
    private Payment payment;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull(message = "Reminder type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ReminderType type;

    @NotNull(message = "Reminder status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    /**
     * When the reminder was actually sent (null until sent).
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Channel used: EMAIL, SMS, or BOTH.
     */
    @Column(name = "channel", length = 20)
    @Builder.Default
    private String channel = "EMAIL";

    @Column(name = "error_message", length = 1000)
    private String errorMessage;
}
