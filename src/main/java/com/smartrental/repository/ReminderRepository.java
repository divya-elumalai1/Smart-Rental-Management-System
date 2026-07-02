package com.smartrental.repository;

import com.smartrental.model.Reminder;
import com.smartrental.model.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Reminder} entity.
 * Provides lookup queries by tenant, status, and due-date ranges.
 */
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    /**
     * Find all reminders for a given tenant.
     */
    List<Reminder> findByTenantId(UUID tenantId);

    /**
     * Find all reminders with a given status.
     */
    List<Reminder> findByStatus(ReminderStatus status);

    /**
     * Find all reminders whose due date falls within the given range (inclusive).
     */
    List<Reminder> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
}
