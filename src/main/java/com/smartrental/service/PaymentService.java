package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.Lease;
import com.smartrental.model.Payment;
import com.smartrental.model.PaymentStatus;
import com.smartrental.model.Property;
import com.smartrental.model.User;
import com.smartrental.model.dto.MarkPaidRequestDTO;
import com.smartrental.model.dto.PaymentRequestDTO;
import com.smartrental.model.dto.PaymentResponseDTO;
import com.smartrental.repository.LeaseRepository;
import com.smartrental.repository.PaymentRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service handling payment creation, marking payments as paid, lookups, and
 * pending-dues calculations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final LeaseRepository leaseRepository;
    private final EmailService emailService;

    /**
     * Create a new (pending) payment record.
     */
    public PaymentResponseDTO create(PaymentRequestDTO request) {
        log.info("Creating payment of {} for tenant {}", request.getAmount(), request.getTenantId());

        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getTenantId()));
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

        Lease lease = null;
        if (request.getLeaseId() != null) {
            lease = leaseRepository.findById(request.getLeaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lease", request.getLeaseId()));
        }

        Payment payment = Payment.builder()
                .tenant(tenant)
                .property(property)
                .lease(lease)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(PaymentStatus.PENDING)
                .dueDate(request.getDueDate())
                .rentPeriod(request.getRentPeriod())
                .notes(request.getNotes())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Created payment with ID: {}", saved.getId());
        return toResponseDTO(saved);
    }

    /**
     * Mark an existing payment as completed (paid).
     */
    public PaymentResponseDTO markAsPaid(UUID id, MarkPaidRequestDTO request) {
        log.info("Marking payment {} as paid", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(request.getPaymentDate() != null
                ? request.getPaymentDate().atStartOfDay()
                : LocalDateTime.now());
        payment.setPaymentMode(request.getPaymentMode());

        String reference = request.getReference() != null ? request.getReference() : request.getRazorpayPaymentId();
        if (reference != null) {
            payment.setRazorpayPaymentId(reference);
        }
        if (request.getRazorpaySignature() != null) {
            payment.setRazorpaySignature(request.getRazorpaySignature());
        }
        if (request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }
        if (payment.getReceiptNumber() == null) {
            payment.setReceiptNumber(generateReceiptNumber());
        }
        if (request.getReceiptUrl() != null) {
            payment.setReceiptUrl(request.getReceiptUrl());
        }

        Payment updated = paymentRepository.save(payment);
        log.info("Payment {} marked as completed", updated.getId());

        try {
            emailService.sendPaymentConfirmationEmail(
                updated.getTenant(),
                updated.getAmount().doubleValue(),
                updated.getProperty().getAddress(),
                updated.getReceiptNumber() != null ? updated.getReceiptNumber() : updated.getId().toString()
            );
        } catch (Exception e) {
            log.warn("Failed to send payment confirmation email: {}", e.getMessage());
        }

        return toResponseDTO(updated);
    }

    /**
     * Get a single payment by ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponseDTO getById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        return toResponseDTO(payment);
    }

    /**
     * Find all payments.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> findAll() {
        return paymentRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find all payments for a given tenant (most recent first).
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> findByTenant(UUID tenantId) {
        return paymentRepository.findByTenantIdOrderByPaymentDateDesc(tenantId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Find all payments for a given property.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> findByProperty(UUID propertyId) {
        return paymentRepository.findByPropertyId(propertyId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Calculate the total pending dues (pending + overdue) for a tenant.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePendingDues(UUID tenantId) {
        return paymentRepository.findByTenantId(tenantId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING
                        || p.getStatus() == PaymentStatus.OVERDUE)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Mark payment as paid by unit number.
     */
    public PaymentResponseDTO markPaidByUnitNumber(MarkPaidRequestDTO request) {
        Property property = propertyRepository.findByUnitNumber(request.getUnitNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Property unit " + request.getUnitNumber()));
        
        LocalDate rentPeriod = request.getMonth() != null 
            ? request.getMonth().withDayOfMonth(1)
            : LocalDate.now().withDayOfMonth(1);
        
        Payment payment = paymentRepository.findByPropertyIdAndRentPeriodOrderByCreatedAtDesc(
                property.getId(), rentPeriod)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.OVERDUE)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Pending payment for unit " + request.getUnitNumber() + " in month " + rentPeriod));
        
        return markAsPaid(payment.getId(), request);
    }

    /**
     * Find all payments with filters for landlord.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> findAllWithFilters(UUID landlordId, String month, String unitNumber) {
        List<Property> properties = propertyRepository.findByLandlordId(landlordId);
        
        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> properties.stream().anyMatch(prop -> prop.getId().equals(p.getProperty().getId())))
                .toList();
        
        if (month != null && !month.isEmpty()) {
            // Filter by month (format: YYYY-MM)
            payments = payments.stream()
                    .filter(p -> p.getRentPeriod() != null && p.getRentPeriod().toString().startsWith(month))
                    .toList();
        }
        
        if (unitNumber != null && !unitNumber.isEmpty()) {
            payments = payments.stream()
                    .filter(p -> p.getProperty().getUnitNumber().equals(unitNumber))
                    .toList();
        }
        
        return payments.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Delete (soft-delete) a payment.
     */
    public void deletePayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        paymentRepository.delete(payment);
        log.info("Payment {} deleted", id);
    }

    // ===========================================
    // Mapping
    // ===========================================

    private PaymentResponseDTO toResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .tenantId(payment.getTenant().getId())
                .tenantName(payment.getTenant().getFullName())
                .propertyId(payment.getProperty().getId())
                .propertyAddress(payment.getProperty().getAddress())
                .unitNumber(payment.getProperty().getUnitNumber())
                .leaseId(payment.getLease() != null ? payment.getLease().getId() : null)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .dueDate(payment.getDueDate())
                .rentPeriod(payment.getRentPeriod())
                .receiptUrl(payment.getReceiptUrl())
                .receiptNumber(payment.getReceiptNumber())
                .paymentMode(payment.getPaymentMode())
                .notes(payment.getNotes())
                .overdue(payment.isOverdue())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private static final AtomicInteger RECEIPT_SEQ = new AtomicInteger(100);

    private String generateReceiptNumber() {
        return "RCP-" + String.format("%05d", RECEIPT_SEQ.incrementAndGet());
    }
}
