package com.smartrental.controller;

import com.smartrental.model.dto.ReminderResponseDTO;
import com.smartrental.service.ReminderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reminders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reminders", description = "Rent reminder management")
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReminderResponseDTO>> getAllReminders() {
        log.debug("GET all reminders");
        return ResponseEntity.ok(reminderService.getAllReminders());
    }

    @GetMapping("/logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReminderResponseDTO>> getReminderLogs() {
        log.debug("GET reminder logs");
        return ResponseEntity.ok(reminderService.getReminderLogs());
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> sendReminder(@PathVariable UUID id) {
        log.info("POST send reminder {}", id);
        reminderService.sendReminder(id);
        return ResponseEntity.ok(Map.of("message", "Reminder sent"));
    }
}
