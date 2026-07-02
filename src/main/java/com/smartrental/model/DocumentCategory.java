package com.smartrental.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the category of an uploaded document.
 */
@Getter
@RequiredArgsConstructor
public enum DocumentCategory {

    /** Rental agreement between landlord and tenant. */
    AGREEMENT("Rental Agreement"),

    /** Identity proof document (Aadhaar, passport, etc.). */
    ID_PROOF("ID Proof"),

    /** No Objection Certificate. */
    NOC("NOC"),

    /** Payment receipt. */
    RECEIPT("Receipt"),

    /** Any other document type. */
    OTHER("Other");

    private final String displayName;
}
