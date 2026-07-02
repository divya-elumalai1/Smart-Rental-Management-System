package com.smartrental.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the status of a lease agreement.
 */
@Getter
@RequiredArgsConstructor
public enum LeaseStatus {

    /** Lease has been created but not yet active. */
    PENDING("Pending"),

    /** Lease is currently active. */
    ACTIVE("Active"),

    /** Lease has expired. */
    EXPIRED("Expired"),

    /** Lease was terminated before the end date. */
    TERMINATED("Terminated");

    private final String displayName;
}
