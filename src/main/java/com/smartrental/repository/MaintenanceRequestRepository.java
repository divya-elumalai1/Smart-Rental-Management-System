package com.smartrental.repository;

import com.smartrental.model.MaintenancePriority;
import com.smartrental.model.MaintenanceRequest;
import com.smartrental.model.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link MaintenanceRequest} entity.
 * Provides lookup queries by tenant, property, status, and priority.
 */
@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {

    /**
     * Find all maintenance requests raised by a given tenant.
     */
    List<MaintenanceRequest> findByTenantId(UUID tenantId);

    /**
     * Find all maintenance requests for a given property.
     */
    List<MaintenanceRequest> findByPropertyId(UUID propertyId);

    /**
     * Find all maintenance requests with a given status.
     */
    List<MaintenanceRequest> findByStatus(MaintenanceStatus status);

    /**
     * Find all maintenance requests with a given priority.
     */
    List<MaintenanceRequest> findByPriority(MaintenancePriority priority);
}
