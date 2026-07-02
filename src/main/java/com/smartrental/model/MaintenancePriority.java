package com.smartrental.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the priority level of a maintenance request.
 */
@Getter
@RequiredArgsConstructor
public enum MaintenancePriority {

    /** Low priority — cosmetic or non-urgent issues. */
    LOW("Low"),

    /** Medium priority — needs attention but not urgent. */
    MEDIUM("Medium"),

    /** High priority — significant inconvenience to tenant. */
    HIGH("High"),

    /** Urgent — safety hazard or major dysfunction, act immediately. */
    URGENT("Urgent");

    private final String displayName;
}
