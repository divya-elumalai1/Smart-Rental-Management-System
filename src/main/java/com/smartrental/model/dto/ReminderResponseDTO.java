package com.smartrental.model.dto;

import com.smartrental.model.ReminderStatus;
import com.smartrental.model.ReminderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponseDTO {
    private UUID id;
    private UUID tenantId;
    private String tenantName;
    private String unitNumber;
    private UUID paymentId;
    private LocalDate dueDate;
    private ReminderType type;
    private ReminderStatus status;
    private String channel;
    private LocalDateTime sentAt;
    private String errorMessage;
    private LocalDateTime createdAt;
}
