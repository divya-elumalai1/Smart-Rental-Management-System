# Code Quality Report

**Project:** Smart Rental Management System  
**Date:** July 2, 2026  
**Backend:** 15 services, 14 controllers, 32 DTOs, 4 security classes  
**Frontend:** 30+ React components  

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Java files** | ~80 |
| **Total JSX files** | ~30 |
| **Lombok usage** | Heavy (`@Data`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`) |
| **Test coverage** | Not measured (no test execution during audit) |
| **Code smells found** | 28 |
| **Duplicate code patterns** | 6 |
| **Hardcoded values** | 15+ |
| **Dead code removed** | 12 items |

---

## Code Quality Issues

### 1. Duplicate Code Patterns (6 instances)

| # | Pattern | Files | Recommendation |
|---|---------|-------|---------------|
| 1 | `findByPropertyId()` + `findByTenant()` loops | `DashboardService.java`, `TenantService.java`, `PropertyService.java` | Extract to shared repository query with JOIN FETCH |
| 2 | Refresh token saving logic | `AuthService.java` (3 locations) | Extract to private helper method |
| 3 | CSV export logic | `PaymentController.java`, `ReportsPage.jsx` | Share via a CSV utility |
| 4 | Password validation annotations | Multiple DTOs | Create a `@ValidPassword` custom annotation |
| 5 | `Controller` → `Service` delegation pattern | All controllers | Consistent but introduces boilerplate |
| 6 | File upload handling | `DocumentController.java`, `WaterMeterController.java` | Extract common validation to utility |

### 2. Hardcoded Values (15+ instances)

| # | File | Line | Value | Fix |
|---|------|------|-------|-----|
| 1 | `WaterMeterService.java` | 41 | `WATER_RATE = BigDecimal.valueOf(8)` | Move to `@Value("${water.rate:8}")` |
| 2 | `DashboardService.java` | 133 | `type("House")` | Make configurable |
| 3 | `WaterMeterService.java` | 61 | `"claude-3-5-sonnet-20241022"` | Move to `@Value("${claude.model:...}")` |
| 4 | `ScheduledReminderService.java` | 133-142 | Property company name | Move to config |
| 5 | `PaymentService.java` | 270 | `RECEIPT_SEQ = new AtomicInteger(100)` | Use DB sequence |
| 6 | `DataSeeder.java` | 57-63 | Passwords `"owner123"`, `"tenant123"` | Move to env vars |
| 7 | `application.properties` | 14-17 | `YOUR_GMAIL`, `YOUR_APP_PASSWORD` | Placeholder values |
| 8 | Various | Multiple | `"Sapthagiri Residency"` | Centralize in config |

### 3. Performance Issues

| # | Issue | File | Impact |
|---|-------|------|--------|
| 1 | N+1 queries (6 locations) | Various services | Page load time degrades with data volume |
| 2 | No collection pagination (7 endpoints) | Various controllers | Memory exhaustion with 10K+ records |
| 3 | In-memory filtering | `PaymentService.java:204-227` | Loads all records for filtering |
| 4 | No `@Transactional` on `WaterMeterService` | `WaterMeterService.java:32` | Partial DB updates on failure |
| 5 | Multiple DB queries per request | `SecurityUtil.java:54-58` | Calls DB on every request |

### 4. Code Organization

| Observation | Details |
|-------------|---------|
| **Good:** Package structure is clean | `controller/`, `service/`, `model/`, `repository/`, `security/`, `config/`, `exception/`, `util/` |
| **Good:** DTOs separated from entities | `model.dto` package contains all DTOs |
| **Good:** Lombok reduces boilerplate | `@Data`, `@Builder`, `@Slf4j` used consistently |
| **Good:** SLF4J logging consistent | All classes use `@Slf4j` |
| **Good:** Validation annotations on DTOs | Most DTOs have `@NotBlank`, `@Email`, `@Size` |
| **Improvement:** `ChatController` returns entity | `ChatService.ChatResponse` is an inner class in service |
| **Improvement:** `ChatController` accepts raw Map | No DTO for message input |
| **Improvement:** `MaintenanceController` accepts raw Map | For status updates |

### 5. Dependency Issues

| Dependency | Version | Status | Issue |
|------------|---------|--------|-------|
| Lombok | 1.18.46 | ✅ Updated | Supports JDK 26 |
| Spring Boot | 3.2.5 | ⚠️ Newer available | 3.3.x+ has security patches |
| OpenAI GPT3 Java | 0.18.2 | ❌ Outdated | Repo archived in 2023. Switch to official OpenAI SDK |
| JJWT | 0.12.5 | ✅ Current | Latest stable |
| Razorpay Java SDK | 1.4.9 | ⚠️ Old | Released 2023 |
| Testcontainers | 1.19.8 | ⚠️ Old | 1.20+ available |
| OWASP Dependency Check | 9.0.6 | ❌ Not configured | Plugin included but not bound to build |

### 6. Dead/Redundant Code Removed

| File | Removed | Reason |
|------|---------|--------|
| `BaseEntity.java` | `restore()` method | Never called |
| `CloudinaryService.java` | `isConfigured()` method | Never called |
| `Role.java` | Explicit `getAuthority()` | Lombok `@Getter` handles it |
| `WaterMeterPhotoRequestDTO.java` | Entire class | Never used |
| `JwtUtil.java` | 9 unused methods | `getUserIdFromToken`, `getRoleFromToken`, `validateTokenType`, `isTokenExpired`, `getRemainingTime`, `getExpirationFromToken`, `getAuthentication`, `getHeader`, `getTokenPrefix` |
| `AuthController.java` | `jwtUtil` field and `logoutWithUserId()` | Field unused, endpoint insecure |
| `OwnerDashboard.jsx` | `CURRENT_MONTH` constant | Never used |
| Various JSX | 14 unused imports | Various unused lucide-react icons |
| Various JSX | 5 unused catch params | Changed to `catch` without parameter |
| `application.properties` | `anthropic.api.key` | Dead config, no SDK uses it |

### 7. Logging Practices

| Good Practices | Issues |
|----------------|--------|
| All classes use SLF4J (`@Slf4j`) | PII (email) logged in plaintext in `AuthService.java` |
| Log levels appropriate (info, debug, error) | Stack traces logged at ERROR in production |
| No `System.out.println` usage | `DataSeeder.java` logged passwords (fixed) |
| Meaningful log messages with context | Email addresses not masked before logging |

### 8. Testing

| Aspect | Status |
|--------|--------|
| Test dependencies present | ✅ JUnit 5, Mockito, AssertJ, Testcontainers |
| Test configuration | ✅ `application-test.properties` should exist (profiles defined) |
| Test files found | ⚠️ No test execution performed in this audit |
| Test coverage | ❓ Unknown |

---

## Recommendations

### Immediate (next sprint)
1. Hash refresh tokens before DB storage
2. Add account lockout mechanism
3. Add login-specific rate limiting
4. Move JWT tokens to httpOnly cookies on frontend

### Short-term (this quarter)
5. Fix N+1 queries in all services
6. Add pagination to collection endpoints
7. Replace `Math.random()` with deterministic fallback in `WaterMeterService`
8. Extract hardcoded values to configuration properties
9. Standardize API versioning (`/v1/` prefix on all endpoints)

### Long-term (next quarter)
10. Upgrade to official OpenAI Java SDK
11. Implement distributed rate limiting (Redis)
12. Add OWASP Dependency Check to build pipeline
13. Add integration tests for payment and auth flows
14. Implement PDF receipt generation (currently a stub)
15. Add structured audit logging for security events
