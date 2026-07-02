package com.smartrental.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the delivery status of a reminder.
 */
@Getter
@RequiredArgsConstructor
public enum ReminderStatus {

    /** Reminder has been queued but not yet sent. */
    PENDING("Pending"),

    /** Reminder was successfully delivered (email and/or SMS). */
    SENT("Sent"),

    /** Reminder delivery failed. */
    FAILED("Failed");

    private final String displayName;
}
