package com.smartrental.service;

import com.smartrental.model.Lease;
import com.smartrental.model.LeaseStatus;
import com.smartrental.model.Payment;
import com.smartrental.model.PaymentStatus;
import com.smartrental.model.Property;
import com.smartrental.model.PropertyStatus;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class PaymentServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private LeaseRepository leaseRepository;

    @MockBean
    private EmailService emailService;

    private User tenant;
    private User landlord;
    private Property property;
    private Lease lease;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        leaseRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();

        landlord = userRepository.save(User.builder()
                .firstName("John")
                .lastName("Landlord")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+911234567890")
                .role(Role.OWNER)
                .build());

        tenant = userRepository.save(User.builder()
                .firstName("Jane")
                .lastName("Tenant")
                .email("jane@example.com")
                .password("password123")
                .phoneNumber("+919876543210")
                .role(Role.TENANT)
                .build());

        property = propertyRepository.save(Property.builder()
                .landlord(landlord)
                .unitNumber("101")
                .address("123 Main St")
                .city("Mumbai")
                .state("MH")
                .status(PropertyStatus.OCCUPIED)
                .rentAmount(new BigDecimal("15000"))
                .build());

        lease = leaseRepository.save(Lease.builder()
                .tenant(tenant)
                .property(property)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(6))
                .rentAmount(new BigDecimal("15000"))
                .depositAmount(new BigDecimal("30000"))
                .status(LeaseStatus.ACTIVE)
                .build());
    }

    @Test
    void shouldCreateAndRetrievePayment() {
        PaymentResponseDTO created = paymentService.create(PaymentRequestDTO.builder()
                .tenantId(tenant.getId())
                .propertyId(property.getId())
                .leaseId(lease.getId())
                .amount(new BigDecimal("15000"))
                .dueDate(LocalDate.now().plusDays(5))
                .rentPeriod(LocalDate.now().withDayOfMonth(1))
                .build());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getAmount()).isEqualByComparingTo("15000");
        assertThat(created.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(created.getTenantName()).isEqualTo("Jane Tenant");

        PaymentResponseDTO fetched = paymentService.getById(created.getId());
        assertThat(fetched.getId()).isEqualTo(created.getId());
    }

    @Test
    void shouldMarkPaymentAsPaid() {
        PaymentResponseDTO created = paymentService.create(PaymentRequestDTO.builder()
                .tenantId(tenant.getId())
                .propertyId(property.getId())
                .leaseId(lease.getId())
                .amount(new BigDecimal("15000"))
                .dueDate(LocalDate.now().plusDays(5))
                .rentPeriod(LocalDate.now().withDayOfMonth(1))
                .build());

        PaymentResponseDTO paid = paymentService.markAsPaid(created.getId(), MarkPaidRequestDTO.builder()
                .paymentMode("UPI")
                .reference("TEST-REF-123")
                .build());

        assertThat(paid.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(paid.getPaymentMode()).isEqualTo("UPI");
        assertThat(paid.getReceiptNumber()).isNotNull();
    }

    @Test
    void shouldSoftDeletePayment() {
        PaymentResponseDTO created = paymentService.create(PaymentRequestDTO.builder()
                .tenantId(tenant.getId())
                .propertyId(property.getId())
                .leaseId(lease.getId())
                .amount(new BigDecimal("15000"))
                .dueDate(LocalDate.now().plusDays(5))
                .rentPeriod(LocalDate.now().withDayOfMonth(1))
                .build());

        paymentService.deletePayment(created.getId());

        assertThatThrownBy(() -> paymentService.getById(created.getId()))
                .hasMessageContaining("Payment");
    }

    @Test
    void shouldListAllPayments() {
        paymentService.create(PaymentRequestDTO.builder()
                .tenantId(tenant.getId())
                .propertyId(property.getId())
                .leaseId(lease.getId())
                .amount(new BigDecimal("15000"))
                .dueDate(LocalDate.now().plusDays(5))
                .rentPeriod(LocalDate.now().withDayOfMonth(1))
                .build());

        List<PaymentResponseDTO> all = paymentService.findAll();
        assertThat(all).hasSize(1);
    }

    @Test
    void shouldCalculatePendingDues() {
        paymentService.create(PaymentRequestDTO.builder()
                .tenantId(tenant.getId())
                .propertyId(property.getId())
                .leaseId(lease.getId())
                .amount(new BigDecimal("15000"))
                .dueDate(LocalDate.now().plusDays(5))
                .rentPeriod(LocalDate.now().withDayOfMonth(1))
                .build());

        BigDecimal dues = paymentService.calculatePendingDues(tenant.getId());
        assertThat(dues).isEqualByComparingTo("15000");
    }
}
