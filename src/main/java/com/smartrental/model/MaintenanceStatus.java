package com.smartrental.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the workflow status of a maintenance request.
 */
@Getter
@RequiredArgsConstructor
public enum MaintenanceStatus {

    /** Request submitted, awaiting landlord action. */
    PENDING("Pending"),

    /** Landlord accepted and work is in progress. */
    IN_PROGRESS("In Progress"),

    /** Request has been resolved. */
    RESOLVED("Resolved"),

    /** Request was cancelled by the tenant or landlord. */
    CANCELLED("Cancelled");

    private final String displayName;
}
