package com.smartrental.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the type of rent reminder.
 */
@Getter
@RequiredArgsConstructor
public enum ReminderType {

    /** Reminder sent 7 days before the due date. */
    DUE_IN_7_DAYS("Due in 7 days"),

    /** Reminder sent 3 days before the due date. */
    DUE_IN_3_DAYS("Due in 3 days"),

    /** Reminder sent on the due date. */
    DUE_TODAY("Due today"),

    /** Alert sent after the due date has passed. */
    OVERDUE("Overdue");

    private final String displayName;
}
