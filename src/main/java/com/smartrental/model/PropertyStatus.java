package com.smartrental.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the availability status of a property.
 */
@Getter
@RequiredArgsConstructor
public enum PropertyStatus {

    /** Property is vacant and available for rent. */
    AVAILABLE("Available"),

    /** Property is currently rented and occupied. */
    OCCUPIED("Occupied"),

    /** Property is under maintenance and not available. */
    MAINTENANCE("Maintenance"),

    /** Unit is under construction and not yet rentable. */
    UNDER_CONSTRUCTION("Under Construction");

    private final String displayName;
}
