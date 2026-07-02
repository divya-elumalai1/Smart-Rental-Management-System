package com.smartrental.config;

import com.smartrental.model.Lease;
import com.smartrental.model.LeaseStatus;
import com.smartrental.model.Payment;
import com.smartrental.model.PaymentStatus;
import com.smartrental.model.Property;
import com.smartrental.model.PropertyStatus;
import com.smartrental.model.Role;
import com.smartrental.model.User;
import com.smartrental.repository.LeaseRepository;
import com.smartrental.repository.PaymentRepository;
import com.smartrental.repository.PropertyRepository;
import com.smartrental.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds Sapthagiri Residency with owner, tenants, 10 units, leases, and current-month rent records.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        entityManager.createNativeQuery("UPDATE users SET role = 'OWNER' WHERE role = 'LANDLORD'").executeUpdate();

        if (propertyRepository.count() > 0) {
            log.info("Database already seeded — skipping");
            return;
        }

        log.info("Seeding Sapthagiri Residency data…");

        User owner = saveUser("Elumalai", "Owner", "elumalai@sapthagiri.com",
                "9876543210", Role.OWNER, "owner123");

        User isthan = saveUser("Isthan", "Tenant", "isthan@tenant.com", "9000000001", Role.TENANT, "tenant123");
        User nagaraj = saveUser("Nagaraj", "Tenant", "nagaraj@tenant.com", "9000000002", Role.TENANT, "tenant123");
        User rahul = saveUser("Rahul", "Tenant", "rahul@tenant.com", "9000000003", Role.TENANT, "tenant123");
        User harish = saveUser("Harish", "Tenant", "harish@tenant.com", "9000000004", Role.TENANT, "tenant123");

        record UnitSeed(String unit, String floor, BigDecimal rent, PropertyStatus status, User tenant) {}

        List<UnitSeed> units = List.of(
                new UnitSeed("G1", "Ground Floor", bd("9000"), PropertyStatus.OCCUPIED, isthan),
                new UnitSeed("101", "1st Floor", bd("9500"), PropertyStatus.OCCUPIED, nagaraj),
                new UnitSeed("102", "1st Floor", bd("10500"), PropertyStatus.OCCUPIED, rahul),
                new UnitSeed("103", "1st Floor", bd("9500"), PropertyStatus.UNDER_CONSTRUCTION, null),
                new UnitSeed("104", "1st Floor", bd("9500"), PropertyStatus.UNDER_CONSTRUCTION, null),
                new UnitSeed("202", "2nd Floor", bd("10000"), PropertyStatus.UNDER_CONSTRUCTION, null),
                new UnitSeed("203", "2nd Floor", bd("10000"), PropertyStatus.UNDER_CONSTRUCTION, null),
                new UnitSeed("301", "3rd Floor", bd("11500"), PropertyStatus.OCCUPIED, harish),
                new UnitSeed("302", "3rd Floor", bd("11500"), PropertyStatus.UNDER_CONSTRUCTION, null),
                new UnitSeed("303", "3rd Floor", bd("11500"), PropertyStatus.UNDER_CONSTRUCTION, null)
        );

        LocalDate leaseStart = LocalDate.of(2025, 1, 1);
        LocalDate leaseEnd = LocalDate.of(2027, 12, 31);
        LocalDate rentPeriod = LocalDate.now().withDayOfMonth(1);
        LocalDate dueDate = LocalDate.now().withDayOfMonth(1);

        for (UnitSeed seed : units) {
            Property property = Property.builder()
                    .landlord(owner)
                    .unitNumber(seed.unit())
                    .floorLabel(seed.floor())
                    .address("Sapthagiri Residency, Unit " + seed.unit())
                    .city("Bengaluru")
                    .state("Karnataka")
                    .rentAmount(seed.rent())
                    .deposit(seed.rent().multiply(BigDecimal.valueOf(2)))
                    .status(seed.status())
                    .description("Unit " + seed.unit() + " — " + seed.floor())
                    .build();
            property = propertyRepository.save(property);

            if (seed.tenant() != null) {
                Lease lease = Lease.builder()
                        .tenant(seed.tenant())
                        .property(property)
                        .startDate(leaseStart)
                        .endDate(leaseEnd)
                        .rentAmount(seed.rent())
                        .depositAmount(seed.rent().multiply(BigDecimal.valueOf(2)))
                        .status(LeaseStatus.ACTIVE)
                        .build();
                lease = leaseRepository.save(lease);

                Payment payment = Payment.builder()
                        .tenant(seed.tenant())
                        .property(property)
                        .lease(lease)
                        .amount(seed.rent())
                        .status(PaymentStatus.PENDING)
                        .dueDate(dueDate)
                        .rentPeriod(rentPeriod)
                        .notes("Rent for " + rentPeriod.getMonth() + " " + rentPeriod.getYear())
                        .build();
                paymentRepository.save(payment);
            }
        }

        log.info("Seed complete — {} owner(s) and {} tenant(s) created. Use the seeded emails to log in.", 1L, 4L);
    }

    private User saveUser(String first, String last, String email, String phone, Role role, String rawPassword) {
        User user = User.builder()
                .firstName(first)
                .lastName(last)
                .email(email)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .emailVerified(true)
                .phoneVerified(true)
                .active(true)
                .city("Bengaluru")
                .state("Karnataka")
                .country("India")
                .lastLoginAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
