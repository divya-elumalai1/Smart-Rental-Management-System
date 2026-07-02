package com.smartrental.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the status of a rent payment.
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    /** Payment has been initiated but not yet completed. */
    PENDING("Pending"),

    /** Payment was successfully completed and verified. */
    COMPLETED("Completed"),

    /** Payment failed during processing. */
    FAILED("Failed"),

    /** Payment was cancelled by the user. */
    CANCELLED("Cancelled"),

    /** Payment is past the due date and not yet paid. */
    OVERDUE("Overdue"),

    /** Payment was refunded to the tenant. */
    REFUNDED("Refunded");

    private final String displayName;
}
