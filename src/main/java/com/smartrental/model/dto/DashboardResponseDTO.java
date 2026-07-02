package com.smartrental.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO containing aggregate dashboard statistics for a landlord.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {

    /** Total number of properties managed by the landlord. */
    private long totalUnits;

    /** Properties currently rented out. */
    private long occupied;

    /** Properties available for rent. */
    private long vacant;

    /** Total rent collected (completed payments). */
    private BigDecimal collected;

    /** Total rent pending (pending + overdue payments). */
    private BigDecimal pending;

    /** Total rent overdue (overdue payments). */
    private BigDecimal overdue;

    /** Number of active leases. */
    private long activeLeases;

    /** Number of maintenance requests still open (pending or in progress). */
    private long pendingMaintenance;

    /** Total number of tenants associated with the landlord's properties. */
    private long totalTenants;
}
