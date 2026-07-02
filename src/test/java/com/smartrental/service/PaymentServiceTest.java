package com.smartrental.service;

import com.smartrental.exception.ResourceNotFoundException;
import com.smartrental.model.Payment;
import com.smartrental.model.PaymentStatus;
import com.smartrental.model.Property;
import com.smartrental.model.Role;
import com.smartrental.model.User;
import com.smartrental.model.dto.MarkPaidRequestDTO;
import com.smartrental.model.dto.PaymentRequestDTO;
import com.smartrental.model.dto.PaymentResponseDTO;
import com.smartrental.repository.LeaseRepository;
import com.smartrental.repository.PaymentRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentService}.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private LeaseRepository leaseRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentService paymentService;

    private UUID tenantId;
    private User tenant;
    private UUID propertyId;
    private Property property;
    private UUID paymentId;
    private Payment payment;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = User.builder()
                .id(tenantId)
                .firstName("Tom")
                .lastName("Tenant")
                .email("tom@example.com")
                .role(Role.TENANT)
                .build();
        propertyId = UUID.randomUUID();
        property = Property.builder()
                .id(propertyId)
                .address("123 Main St")
                .city("Pune")
                .build();
        paymentId = UUID.randomUUID();
        payment = Payment.builder()
                .id(paymentId)
                .tenant(tenant)
                .property(property)
                .amount(new BigDecimal("15000"))
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(5))
                .build();
    }

    @Test
    void create_shouldSavePaymentWithPendingStatus() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .tenantId(tenantId)
                .propertyId(propertyId)
                .amount(new BigDecimal("15000"))
                .dueDate(LocalDate.now().plusDays(5))
                .build();
        when(userRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(paymentId);
            return p;
        });

        PaymentResponseDTO result = paymentService.create(request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getAmount()).isEqualByComparingTo("15000");
        assertThat(result.getCurrency()).isEqualTo("INR");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void create_shouldThrowWhenPropertyNotFound() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .tenantId(tenantId)
                .propertyId(propertyId)
                .amount(new BigDecimal("15000"))
                .dueDate(LocalDate.now().plusDays(5))
                .build();
        when(userRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void markAsPaid_shouldSetCompletedStatusAndPaymentDate() {
        MarkPaidRequestDTO request = MarkPaidRequestDTO.builder()
                .paymentMode("UPI")
                .reference("pay_123")
                .build();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponseDTO result = paymentService.markAsPaid(paymentId, request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getRazorpayPaymentId()).isEqualTo("pay_123");
        assertThat(result.getPaymentDate()).isNotNull();
    }

    @Test
    void calculatePendingDues_shouldSumPendingAndOverdue() {
        Payment pending1 = Payment.builder()
                .amount(new BigDecimal("15000"))
                .status(PaymentStatus.PENDING)
                .tenant(tenant)
                .property(property)
                .dueDate(LocalDate.now())
                .build();
        Payment overdue = Payment.builder()
                .amount(new BigDecimal("5000"))
                .status(PaymentStatus.OVERDUE)
                .tenant(tenant)
                .property(property)
                .dueDate(LocalDate.now().minusDays(5))
                .build();
        Payment completed = Payment.builder()
                .amount(new BigDecimal("10000"))
                .status(PaymentStatus.COMPLETED)
                .tenant(tenant)
                .property(property)
                .dueDate(LocalDate.now().minusDays(10))
                .build();
        when(paymentRepository.findByTenantId(tenantId))
                .thenReturn(List.of(pending1, overdue, completed));

        BigDecimal dues = paymentService.calculatePendingDues(tenantId);

        assertThat(dues).isEqualByComparingTo("20000");
    }

    @Test
    void calculatePendingDues_shouldReturnZeroWhenNonePending() {
        Payment completed = Payment.builder()
                .amount(new BigDecimal("10000"))
                .status(PaymentStatus.COMPLETED)
                .tenant(tenant)
                .property(property)
                .dueDate(LocalDate.now())
                .build();
        when(paymentRepository.findByTenantId(tenantId)).thenReturn(List.of(completed));

        BigDecimal dues = paymentService.calculatePendingDues(tenantId);

        assertThat(dues).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void markAsPaid_shouldThrowWhenPaymentNotFound() {
        MarkPaidRequestDTO request = MarkPaidRequestDTO.builder()
                .paymentMode("UPI")
                .reference("pay_123")
                .build();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.markAsPaid(paymentId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
