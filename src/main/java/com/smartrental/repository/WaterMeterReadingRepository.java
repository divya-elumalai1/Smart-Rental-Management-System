package com.smartrental.repository;

import com.smartrental.model.WaterMeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaterMeterReadingRepository extends JpaRepository<WaterMeterReading, UUID> {

    Optional<WaterMeterReading> findByPropertyIdAndReadingDate(UUID propertyId, LocalDate readingDate);

    List<WaterMeterReading> findByUnitNumberOrderByReadingDateDesc(String unitNumber);

    @Query("SELECT w FROM WaterMeterReading w WHERE w.property.id = :propertyId ORDER BY w.readingDate DESC")
    List<WaterMeterReading> findByPropertyIdOrderByReadingDateDesc(@Param("propertyId") UUID propertyId);

    Optional<WaterMeterReading> findTopByUnitNumberOrderByReadingDateDesc(String unitNumber);

    @Query("SELECT w FROM WaterMeterReading w WHERE w.readingDate BETWEEN :startDate AND :endDate ORDER BY w.readingDate DESC")
    List<WaterMeterReading> findByReadingDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
