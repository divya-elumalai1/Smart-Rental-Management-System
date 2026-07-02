# Project Audit: Smart Rental Management System

**Project:** Sapthagiri Residency — Smart Rental Management  
**Stack:** Spring Boot 3.2.5 (Java 21) + React 19 (Vite 8)  
**Database:** PostgreSQL 15 (Flyway migrations)  
**Date:** 2026-07-01

---

## 1. Existing REST APIs

All endpoints are under the context path `/api` (e.g., `/api/v1/properties`).

| # | Controller | Method | Path | Auth | Description |
|---|-----------|--------|------|------|-------------|
| 1 | AuthController | POST | `/auth/register` | Public | Register new user |
| 2 | AuthController | POST | `/auth/login` | Public | Login and get JWT tokens |
| 3 | AuthController | POST | `/auth/refresh` | Public | Refresh access token |
| 4 | AuthController | POST | `/auth/logout` | Public | Logout (stub) |
| 5 | AuthController | POST | `/auth/logout/{userId}` | Public | Logout by user ID |
| 6 | AuthController | POST | `/auth/verify-email` | Public | Verify email with token |
| 7 | AuthController | POST | `/auth/resend-verification` | Public | Resend verification email |
| 8 | AuthController | POST | `/auth/forgot-password` | Public | Request password reset |
| 9 | AuthController | POST | `/auth/reset-password` | Public | Reset password |
| 10 | AuthController | POST | `/auth/change-password` | Public | Change password (stub) |
| 11 | AuthController | GET | `/auth/me` | Public | Get current user profile |
| 12 | AuthController | GET | `/auth/health` | Public | Auth health check |
| 13 | DashboardController | GET | `/v1/dashboard/summary` | LANDLORD/ADMIN | Dashboard statistics |
| 14 | DashboardController | GET | `/v1/dashboard/units` | LANDLORD/ADMIN | Unit cards with rent status |
| 15 | LeaseController | GET | `/v1/leases` | LANDLORD/ADMIN | List all leases |
| 16 | LeaseController | GET | `/v1/leases/{id}` | LANDLORD/ADMIN | Get lease by ID |
| 17 | LeaseController | GET | `/v1/leases/tenant/me` | LANDLORD/ADMIN | Get current tenant leases |
| 18 | LeaseController | GET | `/v1/leases/property/{id}` | LANDLORD/ADMIN | Leases for a property |
| 19 | LeaseController | GET | `/v1/leases/active` | LANDLORD/ADMIN | Get active leases |
| 20 | LeaseController | POST | `/v1/leases` | LANDLORD/ADMIN | Create lease |
| 21 | LeaseController | PUT | `/v1/leases/{id}` | LANDLORD/ADMIN | Update lease |
| 22 | LeaseController | DELETE | `/v1/leases/{id}` | LANDLORD/ADMIN | Soft-delete lease |
| 23 | MaintenanceController | GET | `/v1/maintenance` | Authenticated | List all maintenance requests |
| 24 | MaintenanceController | GET | `/v1/maintenance/{id}` | Authenticated | Get maintenance request |
| 25 | MaintenanceController | GET | `/v1/maintenance/tenant/me` | Authenticated | Current tenant's requests |
| 26 | MaintenanceController | GET | `/v1/maintenance/property/{id}` | Authenticated | Requests for a property |
| 27 | MaintenanceController | POST | `/v1/maintenance` | Authenticated | Create maintenance request |
| 28 | MaintenanceController | PUT | `/v1/maintenance/{id}/status` | Authenticated | Update request status |
| 29 | MaintenanceController | POST | `/v1/maintenance/{id}/comments` | Authenticated | Add comment |
| 30 | OwnerController | GET | `/v1/owner/units` | LANDLORD/ADMIN | Get units (same as #14) |
| 31 | OwnerController | POST | `/v1/owner/tenants` | LANDLORD/ADMIN | Assign tenant to unit |
| 32 | OwnerController | PUT | `/v1/owner/tenants/{unitNumber}` | LANDLORD/ADMIN | Edit tenant by unit |
| 33 | OwnerController | DELETE | `/v1/owner/tenants/{unitNumber}` | LANDLORD/ADMIN | Remove tenant by unit |
| 34 | OwnerController | POST | `/v1/owner/payments/mark-paid` | LANDLORD/ADMIN | Mark rent paid by unit |
| 35 | OwnerController | GET | `/v1/owner/payments/all` | LANDLORD/ADMIN | All payments with filters |
| 36 | OwnerController | GET | `/v1/owner/dashboard/stats` | LANDLORD/ADMIN | Dashboard stats (same as #13) |
| 37 | PaymentController | GET | `/v1/payments` | Authenticated | List all payments |
| 38 | PaymentController | GET | `/v1/payments/{id}` | Authenticated | Get payment by ID |
| 39 | PaymentController | GET | `/v1/payments/tenant/me` | Authenticated | Current tenant's payments |
| 40 | PaymentController | GET | `/v1/payments/property/{id}` | Authenticated | Payments for a property |
| 41 | PaymentController | GET | `/v1/payments/tenant/me/dues` | Authenticated | Get pending dues |
| 42 | PaymentController | POST | `/v1/payments` | Authenticated | Create payment |
| 43 | PaymentController | PUT | `/v1/payments/{id}/mark-paid` | Authenticated | Mark payment as paid |
| 44 | PropertyController | GET | `/v1/properties` | LANDLORD/ADMIN | List all properties |
| 45 | PropertyController | GET | `/v1/properties/{id}` | LANDLORD/ADMIN | Get property by ID |
| 46 | PropertyController | GET | `/v1/properties/landlord/me` | LANDLORD/ADMIN | Current landlord's properties |
| 47 | PropertyController | GET | `/v1/properties/available` | LANDLORD/ADMIN | Get vacant properties |
| 48 | PropertyController | POST | `/v1/properties` | LANDLORD/ADMIN | Create property |
| 49 | PropertyController | PUT | `/v1/properties/{id}` | LANDLORD/ADMIN | Update property |
| 50 | PropertyController | DELETE | `/v1/properties/{id}` | LANDLORD/ADMIN | Soft-delete property |
| 51 | PropertyController | GET | `/v1/properties/unit/{unitNumber}` | LANDLORD/ADMIN | Get property by unit number |
| 52 | TenantController | GET | `/v1/tenants` | LANDLORD/ADMIN | List tenants |
| 53 | TenantController | POST | `/v1/tenants` | LANDLORD/ADMIN | Assign tenant (duplicate of #31) |
| 54 | TenantController | PUT | `/v1/tenants/{leaseId}` | LANDLORD/ADMIN | Update tenant by lease |
| 55 | TenantController | DELETE | `/v1/tenants/{leaseId}` | LANDLORD/ADMIN | Remove tenant by lease |
| 56 | TenantPortalController | GET | `/v1/tenant/my-unit` | TENANT | Get tenant's unit |
| 57 | TenantPortalController | GET | `/v1/tenant/my-payments` | TENANT | Tenant payment history |
| 58 | TenantPortalController | GET | `/v1/tenant/my-bill/{month}` | TENANT | Get monthly bill |
| 59 | TenantPortalController | POST | `/v1/tenant/maintenance` | TENANT | Raise maintenance request |
| 60 | WaterMeterController | POST | `/v1/water-meter/read-photo` | LANDLORD/ADMIN | AI meter reading from photo |
| 61 | WaterMeterController | POST | `/v1/water-meter/calculate` | LANDLORD/ADMIN | Calculate water bill |
| 62 | WaterMeterController | POST | `/v1/water-meter/save` | LANDLORD/ADMIN | Save meter reading |
| 63 | WaterMeterController | GET | `/v1/water-meter/unit/{unitNumber}` | All roles | Get readings for a unit |
| 64 | WaterMeterController | GET | `/v1/water-meter/occupied` | LANDLORD/ADMIN | Latest readings (occupied) |

**Public endpoints:** `/auth/**`, `/public/**`, `/health`, `/actuator/health`, `/actuator/info`, `/swagger-ui/**`, `/v3/api-docs/**`

---

## 2. Existing Entities (JPA)

| # | Entity | Table | Key Relationships | Notes |
|---|--------|-------|-------------------|-------|
| 1 | `User` | `users` | OneToMany: properties, leases, payments, maintenance_requests, documents, chat_history, reminders | Implements `UserDetails`; has soft-delete, 2FA fields, email/phone verification, token management |
| 2 | `Property` | `properties` | ManyToOne: landlord (User); OneToMany: leases, maintenance_requests, documents | Has unit_number, floor_label (added via V2 migration); supports status: AVAILABLE, OCCUPIED, MAINTENANCE, UNDER_CONSTRUCTION |
| 3 | `Lease` | `leases` | ManyToOne: tenant (User), property (Property); OneToMany: payments | Tracks start/end dates, rent, deposit, status; has `isCurrentlyActive()` helper |
| 4 | `Payment` | `payments` | ManyToOne: tenant (User), property (Property), lease (Lease) | Razorpay-integrated (order_id, payment_id, signature); Has `isOverdue()` helper; supports payment_mode, receipt_number |
| 5 | `MaintenanceRequest` | `maintenance_requests` | ManyToOne: tenant (User), property (Property); OneToMany: comments | Has priority, status, resolution tracking; supports comments |
| 6 | `MaintenanceComment` | `maintenance_comments` | ManyToOne: maintenance_request, user | Belongs to a maintenance request with cascade delete |
| 7 | `Document` | `documents` | ManyToOne: user (User), property (Property) | Cloudinary file refs; has file metadata and category enum |
| 8 | `ChatHistory` | `chat_history` | ManyToOne: user (User) | AI chatbot conversation history per user per session |
| 9 | `Reminder` | `reminders` | ManyToOne: tenant (User), payment (Payment) | Rent reminder tracking with status, channel, error_message |
| 10 | `WaterMeterReading` | `water_meter_readings` | ManyToOne: property (Property) | Tracks previous/current reading, units consumed; calculates bills |

**Base entity fields** (in `BaseEntity`): `createdAt`, `updatedAt`, `deleted`, `deletedAt`

**Enums:** `Role` (TENANT, LANDLORD, ADMIN), `PropertyStatus`, `LeaseStatus`, `PaymentStatus`, `MaintenanceStatus`, `MaintenancePriority`, `DocumentCategory`, `ReminderStatus`, `ReminderType`

---

## 3. Existing Repositories

| # | Repository | Entity | Custom Queries |
|---|-----------|--------|----------------|
| 1 | `UserRepository` | User | `findByEmailIgnoreCase`, `existsByEmailIgnoreCase`, `findByPhoneNumber`, `findByEmailVerificationToken`, `findByResetToken`, `findByRefreshToken`, `findByRole`, `findByActiveTrue`, `searchUsers`, `searchTenants`, `searchLandlords`, `countByRole`, `countByCreatedAtAfter`, `findByEmailVerifiedFalse`, `findUsersWithExpiredEmailVerification`, `findUsersWithExpiredResetTokens`, `clearExpiredEmailVerificationTokens`, `clearExpiredResetTokens`, `clearExpiredRefreshTokens` |
| 2 | `PropertyRepository` | Property | `findByLandlordId`, `findByLandlordIdAndUnitNumber`, `findByStatus`, `findByCity`, `findByUnitNumber` |
| 3 | `LeaseRepository` | Lease | `findByTenantId`, `findByPropertyId`, `findByStatus`, `findByTenantIdAndStatus`, `findByPropertyIdAndStatus` |
| 4 | `PaymentRepository` | Payment | `findByTenantId`, `findByPropertyId`, `findByStatus`, `findByDueDateBetween`, `findByPropertyIdAndRentPeriodOrderByCreatedAtDesc`, `findByTenantIdOrderByPaymentDateDesc` |
| 5 | `MaintenanceRequestRepository` | MaintenanceRequest | `findByTenantId`, `findByPropertyId`, `findByStatus`, `findByPriority` |
| 6 | `MaintenanceCommentRepository` | MaintenanceComment | `findByMaintenanceRequestId` |
| 7 | `DocumentRepository` | Document | `findByUserId`, `findByPropertyId`, `findByCategory` |
| 8 | `ChatHistoryRepository` | ChatHistory | `findByUserIdOrderByCreatedAtDesc` |
| 9 | `ReminderRepository` | Reminder | `findByTenantId`, `findByStatus`, `findByDueDateBetween` |
| 10 | `WaterMeterReadingRepository` | WaterMeterReading | `findByPropertyIdAndReadingDate`, `findByUnitNumberOrderByReadingDateDesc`, `findByPropertyIdOrderByReadingDateDesc`, `findTopByUnitNumberOrderByReadingDateDesc`, `findByReadingDateBetween` |

---

## 4. Existing Services

| # | Service | Key Methods | Notes |
|---|---------|------------|-------|
| 1 | `AuthService` | `register`, `login`, `refreshToken`, `logout`, `verifyEmail`, `resendVerificationEmail`, `requestPasswordReset`, `resetPassword`, `changePassword` | Full auth lifecycle; uses `JwtUtil` for token generation; sends verification/reset emails via `EmailService` |
| 2 | `DashboardService` | `getDashboardSummary`, `getOwnerUnits` | Aggregates stats across properties, payments, leases, maintenance; builds unit cards sorted by floor/unit |
| 3 | `EmailService` | `sendVerificationEmail`, `sendWelcomeEmail`, `sendPasswordResetEmail`, `sendPasswordChangedConfirmation`, `sendRentReminderEmail`, `sendPaymentConfirmationEmail`, `sendOverdueNoticeEmail`, `sendMaintenanceRequestConfirmation`, `sendMaintenanceNotificationToLandlord`, `sendMaintenanceStatusUpdate`, `sendDocumentNotification`, `sendSimpleEmail`, `sendTemplatedEmail` | Async email sending via JavaMail + Thymeleaf templates |
| 4 | `LeaseService` | `create`, `update`, `delete`, `getById`, `findAll`, `findByTenant`, `findByProperty`, `findActiveLeases` | Standard CRUD + filter operations |
| 5 | `MaintenanceService` | `create`, `updateStatus`, `addComment`, `getById`, `findAll`, `findByTenant`, `findByProperty` | CRUD + status updates with resolution tracking |
| 6 | `PaymentService` | `create`, `markAsPaid`, `getById`, `findAll`, `findByTenant`, `findByProperty`, `calculatePendingDues`, `markPaidByUnitNumber`, `findAllWithFilters` | Payment lifecycle; generates receipt numbers; supports filtering by month/unit |
| 7 | `PropertyService` | `create`, `update`, `delete`, `getById`, `findAll`, `findByLandlord`, `findAvailable`, `findByUnitNumber`, `findByTenantId` | Standard CRUD + lookup by tenant via in-memory iteration (inefficient) |
| 8 | `TenantService` | `listTenants`, `assignTenant`, `updateTenant`, `removeTenant`, `updateTenantByUnitNumber`, `removeTenantByUnitNumber` | Tenant assignment/removal with lease + payment creation |
| 9 | `WaterMeterService` | `readMeterFromPhoto`, `calculateWaterBill`, `saveReading`, `getReadingsByUnit`, `getLatestReadingsForOccupiedUnits` | Water meter management; simulated AI reading; bill calculation at ₹8/unit |

---

## 5. Missing Features

### 5.1 Critical Gaps

| # | Missing Feature | Evidence | Impact |
|---|----------------|----------|--------|
| 1 | **Thymeleaf email templates** | `EmailService.java` references 11 templates (`email/verification`, `email/welcome`, `email/password-reset`, etc.) but no files exist under `src/main/resources/templates/` | **Runtime error** — Thymeleaf `templateEngine.process()` will throw `TemplateInputException` on any email send |
| 2 | **`/auth/me` endpoint broken** | `AuthController.getCurrentUser()` tries to cast `UserDetails` to `com.smartrental.model.User`, but `JwtUtil.getAuthentication()` sets a `org.springframework.security.core.userdetails.User` as the principal. The cast always fails → always returns 401. | **Broken endpoint** — Frontend cannot fetch current user profile |
| 3 | **`OwnerController` role check broken** | `@PreAuthorize("hasAnyRole('LANDLORD', 'OWNER', 'ADMIN')")` references role `OWNER` which does not exist in the `Role` enum. Spring Security checks for `ROLE_OWNER` — no user has this. | All `OwnerController` endpoints return 403 for legitimate LANDLORD users |
| 4 | **Missing scheduled reminder job** | `app.rent-reminder.cron-expression` is configured in `application.yml`, `Reminder` entity + repository exist, but no `@Scheduled` component sends reminders. | Rent reminders never fire |
| 5 | **Missing SMS service** | Twilio SDK (`9.3.0`) is declared in `pom.xml` but no service class uses it. | SMS reminders/notifications cannot be sent |
| 6 | **Missing Razorpay webhook handler** | Razorpay SDK included, `Payment` entity has razorpay fields, but no controller handles payment webhook callbacks. | Cannot confirm online payments asynchronously |
| 7 | **Missing file upload service** | Cloudinary SDK included, `Document` entity exists, but no `DocumentService` or file upload endpoint. | Document feature has no API |
| 8 | **Missing AI chatbot service** | OpenAI SDK included, `ChatHistory` entity exists, but no chatbot controller or service. | AI assistant feature has no API |

### 5.2 Missing Controllers/Services

| # | Entity | Repository Exists | Controller | Service |
|---|--------|-------------------|------------|---------|
| 1 | `Document` | Yes | ❌ | ❌ |
| 2 | `ChatHistory` | Yes | ❌ | ❌ |
| 3 | `Reminder` | Yes | ❌ | ❌ |

### 5.3 Missing Repository Queries

| # | Repository | Missing Query | Use Case |
|---|-----------|---------------|----------|
| 1 | `PropertyRepository` | `findByTenantIdViaActiveLease` | `PropertyService.findByTenantId()` iterates all properties in memory — O(n) instead of a single query |
| 2 | `MaintenanceRequestRepository` | `countByPropertyIdAndStatusIn` | Dashboard counts active maintenance requests inefficiently |
| 3 | `PaymentRepository` | `sumAmountByPropertyIdAndStatus` | Dashboard payment aggregation does in-memory iteration |

---

## 6. Compile Errors & Runtime Bugs

### 6.1 Compile Errors

None — the project compiles successfully (generated `target/` directory exists with compiled classes, including `UserMapperImpl.java` from MapStruct).

### 6.2 Runtime Bugs

| # | Severity | File | Line(s) | Bug | Fix |
|---|----------|------|---------|-----|-----|
| 1 | **HIGH** | `AuthController.java` | 224 | `userDetails instanceof com.smartrental.model.User` always false because `JwtUtil.getAuthentication()` creates `org.springframework.security.core.userdetails.User`, not the entity. | Change `JwtUtil.getAuthentication()` to return the actual `User` entity as principal, or modify the controller to look up by email |
| 2 | **HIGH** | `OwnerController.java` | 32 | `@PreAuthorize("hasAnyRole('LANDLORD', 'OWNER', 'ADMIN')")` — role `OWNER` doesn't exist. | Change to `"hasAnyRole('LANDLORD', 'ADMIN')"` |
| 3 | **HIGH** | `EmailService.java` | 354 | Thymeleaf templates missing — `templateEngine.process(templateName, context)` will throw exception | Create template files under `src/main/resources/templates/email/` |
| 4 | **MEDIUM** | `SecurityConfig.java` | 192 | `.logoutUrl("/api/auth/logout")` hardcodes `/api` context-path, inconsistent with other patterns (e.g., `requestMatchers("/auth/**")` work relative to context-path) | Change to `"/auth/logout"` to match config |
| 5 | **MEDIUM** | `DashboardService.java` | 72-83 | Payment aggregation does in-memory summation over all payments for all properties — loads unnecessary data | Use JPQL `SUM` query with `GROUP BY` |
| 6 | **MEDIUM** | `PropertyService.java` | 172-182 | `findByTenantId()` iterates ALL properties + LAZY loads leases for each — N+1 query problem | Add a repository query: `@Query("SELECT p FROM Property p JOIN p.leases l WHERE l.tenant.id = :tenantId AND l.status = 'ACTIVE'")` |
| 7 | **LOW** | `WaterMeterController.java` | 36-41 | `@PreAuthorize` on individual methods inconsistent with SecurityConfig pattern | Consider consistent annotation or config-based approach |
| 8 | **LOW** | `AuthController.java` | 97-111 | `logout()` method is a no-op — never actually invalidates the token on the server side | Implement token blacklist or rely on token expiry |
| 9 | **LOW** | `AuthController.java` | 194-207 | `changePassword()` always returns success without actually changing the password | Implement actual password change logic using `authService.changePassword()` |

---

## 7. Suggested Improvements

### 7.1 Architecture & Design

| # | Suggestion | Priority | Details |
|---|-----------|----------|---------|
| 1 | **Fix the JWT principal to use the User entity** | HIGH | Modify `JwtUtil.getAuthentication()` to return a principal containing the actual `User` entity (with userId, role, etc.) so `SecurityUtil.getCurrentUser()` doesn't need a second DB lookup |
| 2 | **Add a Scheduled reminder component** | HIGH | Create `ReminderScheduler.java` with `@Scheduled` methods using the cron expressions from `application.yml`. Query `PaymentRepository` for approaching/overdue payments, create `Reminder` records, and send via `EmailService` (and optionally SMS) |
| 3 | **Add Razorpay webhook controller** | HIGH | Create a public webhook endpoint to receive payment callbacks from Razorpay, verify signatures, and update payment statuses |
| 4 | **Add Cloudinary file upload service** | MEDIUM | Create `DocumentService` and `DocumentController` for file upload/download with Cloudinary integration |
| 5 | **Add AI chatbot service** | MEDIUM | Create `ChatController` and `ChatService` using OpenAI SDK to power the chatbot with RAG over rental data |
| 6 | **Eliminate duplicate endpoints** | MEDIUM | `/v1/dashboard/units` and `/v1/owner/units` are identical. Consolidate to one. Same for `/v1/payments/tenant/me` and `/v1/tenant/my-payments`. |

### 7.2 Performance

| # | Suggestion | Priority | Details |
|---|-----------|----------|---------|
| 1 | **Add database-level aggregations** | MEDIUM | Replace in-memory payment sum/overdue calculations in `DashboardService` with JPQL `SUM` + `GROUP BY` queries |
| 2 | **Fix N+1 queries in `PropertyService.findByTenantId()`** | MEDIUM | Add a proper `@Query` in `PropertyRepository` to join Lease and Property |
| 3 | **Add pagination to list endpoints** | LOW | `GET /v1/properties`, `GET /v1/payments`, etc. return all records. Add `Pageable` support. |

### 7.3 Security

| # | Suggestion | Priority | Details |
|---|-----------|----------|---------|
| 1 | **Add rate limiting on auth endpoints** | MEDIUM | `/auth/login`, `/auth/register`, `/auth/forgot-password` should have rate limiting to prevent brute force |
| 2 | **Add token blacklist** | LOW | Implement server-side token invalidation on logout using Redis or a database table |
| 3 | **Remove secrets from application.properties** | HIGH | `jwt.secret=sapthagiri_residency_secret_key_2026_elumalai` is hardcoded in `application.properties`. Move to environment variable. |

### 7.4 Code Quality

| # | Suggestion | Priority | Details |
|---|-----------|----------|---------|
| 1 | **Add missing DTO validation** | MEDIUM | `PropertyRequestDTO` is missing validation for `unitNumber` and `floorLabel` which are critical for the Sapthagiri use case |
| 2 | **Make `SecurityUtil` more efficient** | LOW | Extract user ID directly from JWT claims instead of doing a DB lookup by email on every request |
| 3 | **Make `PaymentService.RECEIPT_SEQ` persistent** | LOW | The `AtomicInteger` receipt counter resets on restart. Use a database sequence or table-based counter. |
| 4 | **Fix application.properties vs application.yml inconsistency** | LOW | DB name is `sapthagiri_db` in `.properties` but `smart_rental_db` in `.yml`. The `.yml` overrides, so the `.properties` value is dead config. |

### 7.5 Testing

| # | Suggestion | Priority | Details |
|---|-----------|----------|---------|
| 1 | **Add service tests for missing services** | MEDIUM | No tests exist for `AuthService`, `TenantService`, `WaterMeterService`, `EmailService` |
| 2 | **Add integration tests** | MEDIUM | Add `@SpringBootTest` integration tests with Testcontainers for PostgreSQL |
| 3 | **Add controller tests** | LOW | No `WebMvcTest` tests exist for REST controllers |

### 7.6 Documentation & Operations

| # | Suggestion | Priority | Details |
|---|-----------|----------|---------|
| 1 | **Add Swagger/OpenAPI annotations** | LOW | `springdoc` is configured but no `@Operation` or `@ApiResponse` annotations on controllers |
| 2 | **Add Docker Compose for full stack** | MEDIUM | `docker-compose.yml` exists but only has PostgreSQL. Add services for Redis, and optionally the app itself. |
| 3 | **Add health check endpoint** | LOW | `AuthController` has `/auth/health` but no centralized health check aggregating DB, mail, and external services |
