package com.smartrental.service;

import com.smartrental.model.Reminder;
import com.smartrental.model.ReminderStatus;
import com.smartrental.model.User;
import com.smartrental.model.dto.ReminderResponseDTO;
import com.smartrental.repository.ReminderRepository;
import com.smartrental.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public List<ReminderResponseDTO> getAllReminders() {
        User currentUser = securityUtil.getCurrentUser();
        List<Reminder> reminders;
        switch (currentUser.getRole()) {
            case ADMIN, OWNER -> reminders = reminderRepository.findAll();
            case TENANT -> reminders = reminderRepository.findByTenantId(currentUser.getId());
            default -> reminders = List.of();
        }
        return reminders.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReminderResponseDTO> getReminderLogs() {
        User currentUser = securityUtil.getCurrentUser();
        List<Reminder> reminders;
        switch (currentUser.getRole()) {
            case ADMIN, OWNER -> reminders = reminderRepository.findByStatus(ReminderStatus.SENT);
            case TENANT -> reminders = reminderRepository.findByTenantId(currentUser.getId());
            default -> reminders = List.of();
        }
        return reminders.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public void sendReminder(UUID id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found: " + id));
        reminder.setStatus(ReminderStatus.SENT);
        reminder.setSentAt(LocalDateTime.now());
        reminderRepository.save(reminder);
        log.info("Reminder {} sent manually", id);
    }

    private ReminderResponseDTO toResponseDTO(Reminder r) {
        ReminderResponseDTO.ReminderResponseDTOBuilder builder = ReminderResponseDTO.builder()
                .id(r.getId())
                .tenantId(r.getTenant().getId())
                .tenantName(r.getTenant().getFirstName() + " " + r.getTenant().getLastName())
                .dueDate(r.getDueDate())
                .type(r.getType())
                .status(r.getStatus())
                .channel(r.getChannel())
                .sentAt(r.getSentAt())
                .errorMessage(r.getErrorMessage())
                .createdAt(r.getCreatedAt());

        if (r.getPayment() != null) {
            builder.paymentId(r.getPayment().getId());
        }

        return builder.build();
    }
}
