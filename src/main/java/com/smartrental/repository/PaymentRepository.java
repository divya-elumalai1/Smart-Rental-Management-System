package com.smartrental.repository;

import com.smartrental.model.Payment;
import com.smartrental.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Payment} entity.
 * Provides lookup queries by tenant, property, status, and due-date ranges.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Find all payments for a given tenant.
     */
    List<Payment> findByTenantId(UUID tenantId);

    /**
     * Find all payments for a given property.
     */
    List<Payment> findByPropertyId(UUID propertyId);

    /**
     * Find all payments with a given status.
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find all payments whose due date falls within the given range (inclusive).
     */
    List<Payment> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find payments for a property in the current rent period (most recent first).
     */
    List<Payment> findByPropertyIdAndRentPeriodOrderByCreatedAtDesc(UUID propertyId, LocalDate rentPeriod);

    /**
     * Find all payments for a tenant ordered by payment date descending.
     */
    List<Payment> findByTenantIdOrderByPaymentDateDesc(UUID tenantId);

    /**
     * Find a payment by Razorpay order ID.
     */
    java.util.Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
