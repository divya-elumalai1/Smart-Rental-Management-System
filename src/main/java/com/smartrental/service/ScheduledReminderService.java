package com.smartrental.service;

import com.smartrental.model.*;
import com.smartrental.repository.LeaseRepository;
import com.smartrental.repository.PaymentRepository;
import com.smartrental.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduledReminderService {

    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;
    private final ReminderRepository reminderRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Scheduled(cron = "${app.rent-reminder.cron-expression:0 0 9 * * ?}")
    public void sendDueReminders() {
        log.info("Running scheduled rent due reminders");
        List<Lease> activeLeases = leaseRepository.findByStatus(LeaseStatus.ACTIVE);

        for (Lease lease : activeLeases) {
            LocalDate now = LocalDate.now();
            int dayOfMonth = lease.getStartDate().getDayOfMonth();
            LocalDate dueDate = LocalDate.of(now.getYear(), now.getMonth(), dayOfMonth);

            if (dueDate.isBefore(now)) {
                dueDate = dueDate.plusMonths(1);
            }

            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(now, dueDate);

            ReminderType type = null;
            String channel = "EMAIL";

            if (daysUntil == 7) {
                type = ReminderType.DUE_IN_7_DAYS;
            } else if (daysUntil == 3) {
                type = ReminderType.DUE_IN_3_DAYS;
            } else if (daysUntil == 1) {
                type = ReminderType.DUE_TODAY;
                channel = "BOTH";
            }

            if (type != null && !reminderAlreadySentToday(lease.getTenant(), type, dueDate)) {
                createAndSendReminder(lease, type, channel, dueDate);
            }
        }
    }

    @Scheduled(cron = "${app.rent-reminder.overdue-cron:0 0 10 * * ?}")
    public void sendOverdueNotices() {
        log.info("Running scheduled overdue rent notices");
        List<Lease> activeLeases = leaseRepository.findByStatus(LeaseStatus.ACTIVE);

        for (Lease lease : activeLeases) {
            LocalDate now = LocalDate.now();
            int dayOfMonth = lease.getStartDate().getDayOfMonth();
            LocalDate dueDate = LocalDate.of(now.getYear(), now.getMonth(), dayOfMonth);

            if (dueDate.isBefore(now)) {
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, now);
                if (daysOverdue >= 3 && !reminderAlreadySentToday(lease.getTenant(), ReminderType.OVERDUE, dueDate)) {
                    createAndSendReminder(lease, ReminderType.OVERDUE, "BOTH", dueDate);
                }
            }
        }
    }

    private boolean reminderAlreadySentToday(User tenant, ReminderType type, LocalDate dueDate) {
        return reminderRepository.findByDueDateBetween(dueDate, dueDate).stream()
                .anyMatch(r -> r.getTenant().getId().equals(tenant.getId())
                        && r.getType() == type
                        && r.getStatus() == ReminderStatus.SENT);
    }

    private void createAndSendReminder(Lease lease, ReminderType type, String channel, LocalDate dueDate) {
        User tenant = lease.getTenant();

        Reminder reminder = Reminder.builder()
                .tenant(tenant)
                .dueDate(dueDate)
                .type(type)
                .status(ReminderStatus.PENDING)
                .channel(channel)
                .build();
        reminder = reminderRepository.save(reminder);

        try {
            String subject = getReminderSubject(type);
            String body = getReminderBody(type, tenant.getFirstName(), dueDate);

            emailService.sendSimpleEmail(tenant.getEmail(), subject, body);

            if ("BOTH".equals(channel) && tenant.getPhoneNumber() != null) {
                smsService.sendSms(tenant.getPhoneNumber(), body);
            }

            reminder.setStatus(ReminderStatus.SENT);
            reminder.setSentAt(java.time.LocalDateTime.now());
            reminderRepository.save(reminder);
            log.info("Sent {} reminder to {} ({})", type, tenant.getEmail(), channel);
        } catch (Exception e) {
            log.error("Failed to send {} reminder to {}: {}", type, tenant.getEmail(), e.getMessage());
            reminder.setStatus(ReminderStatus.FAILED);
            reminder.setErrorMessage(e.getMessage());
            reminderRepository.save(reminder);
        }
    }

    private String getReminderSubject(ReminderType type) {
        return switch (type) {
            case DUE_IN_7_DAYS -> "Rent Due in 7 Days";
            case DUE_IN_3_DAYS -> "Rent Due in 3 Days";
            case DUE_TODAY -> "Rent Due Today";
            case OVERDUE -> "Rent Overdue — Action Required";
        };
    }

    private String getReminderBody(ReminderType type, String tenantName, LocalDate dueDate) {
        return switch (type) {
            case DUE_IN_7_DAYS ->
                    "Dear " + tenantName + ",\n\nThis is a reminder that your rent is due in 7 days (due date: " + dueDate + "). Please ensure timely payment to avoid late fees.\n\nThank you,\nSapthagiri Residency";
            case DUE_IN_3_DAYS ->
                    "Dear " + tenantName + ",\n\nYour rent is due in 3 days (due date: " + dueDate + "). Please make the payment at your earliest convenience.\n\nThank you,\nSapthagiri Residency";
            case DUE_TODAY ->
                    "Dear " + tenantName + ",\n\nYour rent is due TODAY (" + dueDate + "). Please make the payment as soon as possible.\n\nThank you,\nSapthagiri Residency";
            case OVERDUE ->
                    "Dear " + tenantName + ",\n\nYour rent is now OVERDUE (was due on: " + dueDate + "). Please make the payment immediately to avoid penalties.\n\nThank you,\nSapthagiri Residency";
        };
    }
}
