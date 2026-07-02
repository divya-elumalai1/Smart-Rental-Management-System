package com.smartrental.repository;

import com.smartrental.model.Property;
import com.smartrental.model.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Property} entity.
 * Provides lookup queries by landlord, status, and city.
 */
@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    /**
     * Find all properties owned by a given landlord.
     */
    List<Property> findByLandlordId(UUID landlordId);

    Optional<Property> findByLandlordIdAndUnitNumber(UUID landlordId, String unitNumber);

    /**
     * Find all properties with a given status.
     */
    List<Property> findByStatus(PropertyStatus status);

    /**
     * Find all properties located in a given city.
     */
    List<Property> findByCity(String city);

    /**
     * Find property by unit number.
     */
    Optional<Property> findByUnitNumber(String unitNumber);
}
