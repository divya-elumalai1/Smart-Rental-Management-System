package com.smartrental.repository;

import com.smartrental.model.Lease;
import com.smartrental.model.LeaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Lease} entity.
 * Provides lookup queries by tenant, property, and status.
 */
@Repository
public interface LeaseRepository extends JpaRepository<Lease, UUID> {

    /**
     * Find all leases for a given tenant.
     */
    List<Lease> findByTenantId(UUID tenantId);

    /**
     * Find all leases for a given property.
     */
    List<Lease> findByPropertyId(UUID propertyId);

    /**
     * Find all leases with a given status.
     */
    List<Lease> findByStatus(LeaseStatus status);

    /**
     * Find a lease for a tenant filtered by status (e.g. the active lease).
     */
    Optional<Lease> findByTenantIdAndStatus(UUID tenantId, LeaseStatus status);

    /**
     * Find the active lease for a property.
     */
    Optional<Lease> findByPropertyIdAndStatus(UUID propertyId, LeaseStatus status);
}
