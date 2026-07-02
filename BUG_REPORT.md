# Bug Report

**Project:** Smart Rental Management System  
**Date:** July 2, 2026  
**Build:** `mvn clean install` ✅ | `npm run build` ✅

---

## Fixed Bugs

### Bug #1: PaymentController lacks authorization — any authenticated user can access any payment
**Severity:** Critical  
**File:** `src/main/java/com/smartrental/controller/PaymentController.java` (lines 42-167)  
**Fix:** Added `@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")` to all payment management endpoints. Tenant-specific endpoints (`/tenant/me`, `/tenant/me/dues`) accessible by TENANT role.

### Bug #2: `/auth/logout/{userId}` endpoint is publicly accessible
**Severity:** Critical  
**File:** `src/main/java/com/smartrental/controller/AuthController.java` (lines 112-119)  
**Impact:** Anyone could invalidate any user's refresh token by guessing their UUID.  
**Fix:** Removed the endpoint entirely.

### Bug #3: JWT secret fallback is a hardcoded known string
**Severity:** Critical  
**File:** `src/main/java/com/smartrental/security/JwtUtil.java` (lines 54-56)  
**Impact:** If `JWT_SECRET` env var is not set, the system uses `"your-super-secret-jwt-key-min-256-bits-for-hs256-algorithm-change-in-production"` — a publicly known string.  
**Fix:** Changed to throw `IllegalStateException` at startup if secret is missing/weak.

### Bug #4: `hideUserNotFoundExceptions(false)` enables email enumeration
**Severity:** Critical  
**File:** `src/main/java/com/smartrental/security/SecurityConfig.java` (line 67)  
**Impact:** Login endpoint returns "User not found with email: X" vs "Bad credentials", allowing attackers to determine which emails are registered.  
**Fix:** Set to `true` (Spring Security default).

### Bug #5: Razorpay webhook endpoint is JWT-protected and unreachable
**Severity:** Critical  
**File:** `src/main/java/com/smartrental/security/SecurityConfig.java` (line 186)  
**Impact:** Razorpay cannot authenticate via JWT, so all webhook requests are rejected as 401. Online payments would never be processed.  
**Fix:** Added `.requestMatchers("/webhook/**").permitAll()`.

### Bug #6: `/forgot-password` leaks whether email exists
**Severity:** Critical  
**File:** `src/main/java/com/smartrental/service/AuthService.java` (lines 289-290)  
**Impact:** Throws `IllegalArgumentException("User not found")` for unknown emails before the controller can return a safe message.  
**Fix:** Changed to return silently for unknown emails.

### Bug #7: `resendVerificationEmail` leaks whether email exists
**Severity:** High  
**File:** `src/main/java/com/smartrental/service/AuthService.java` (lines 258-266)  
**Impact:** Throws exception for unknown emails, enabling enumeration.  
**Fix:** Changed to return silently.

### Bug #8: `X-Content-Type-Options: nosniff` explicitly disabled
**Severity:** High  
**File:** `src/main/java/com/smartrental/security/SecurityConfig.java` (line 130)  
**Impact:** MIME-sniffing protection removed, enabling XSS attacks via uploaded files.  
**Fix:** Removed `.contentTypeOptions(...).disable()` call.

### Bug #9: Missing `@Valid` on `TenantPortalController.raiseMaintenanceRequest()`
**Severity:** High  
**File:** `src/main/java/com/smartrental/controller/TenantPortalController.java` (line 100)  
**Impact:** Request body bypasses ALL bean validation constraints.  
**Fix:** Added `@Valid` annotation.

### Bug #10: `application.properties` and `application.yml` specify different database names
**Severity:** Critical  
**File:** `src/main/resources/application.properties` (line 2) vs `application.yml` (line 34)  
**Impact:** Different databases used depending on load order (`sapthagiri_db` vs `smart_rental_db`).  
**Fix:** Consolidated to env-var references.

### Bug #11: `application.properties` hardcodes DB credentials
**Severity:** High  
**File:** `src/main/resources/application.properties` (lines 3-4)  
**Impact:** `spring.datasource.username=postgres` and `spring.datasource.password=postgres` override any env vars set in production.  
**Fix:** Replaced with `${DB_USERNAME:postgres}` and `${DB_PASSWORD:postgres}`.

### Bug #12: DataSeeder logs plaintext passwords
**Severity:** High  
**File:** `src/main/java/com/smartrental/config/DataSeeder.java` (lines 126-127)  
**Fix:** Removed password strings from log output.

### Bug #13: No file upload type/size validation
**Severity:** High  
**File:** `src/main/java/com/smartrental/service/DocumentService.java` (lines 43-44), `WaterMeterService.java` (line 48)  
**Impact:** Any file type could be uploaded (including executables, scripts). No size limit enforced in code.  
**Fix:** Added MIME type allowlist and size checks.

### Bug #14: Missing global exception handlers (11 types unhandled)
**Severity:** High  
**File:** `src/main/java/com/smartrental/exception/GlobalExceptionHandler.java`  
**Impact:** `AuthenticationException`, `DataIntegrityViolationException`, `MaxUploadSizeExceededException`, `HttpMessageNotReadableException`, and 7 others returned raw Spring error responses.  
**Fix:** Added handlers for all common exception types.

### Bug #15: Error responses lack trace IDs
**Severity:** Medium  
**File:** `src/main/java/com/smartrental/exception/ErrorResponse.java`  
**Impact:** Cannot correlate error logs with API responses.  
**Fix:** Added `traceId` field (UUID).

### Bug #16: Lombok 1.18.32 incompatible with JDK 26+
**Severity:** High  
**File:** `pom.xml`  
**Impact:** Build fails with `ExceptionInInitializerError: TypeTag` on JDK 26.  
**Fix:** Upgraded to Lombok 1.18.46, added `maven.compiler.proc=full`.

---

## Open Bugs

### Bug #17: Refresh tokens stored in plaintext in database
**Severity:** Critical  
**File:** `src/main/java/com/smartrental/service/AuthService.java` (lines 90, 143-145, 188-190)  
**Impact:** If the database is breached, all active refresh tokens are exposed, allowing attackers to generate new access tokens indefinitely.  
**Fix:** Hash refresh tokens with SHA-256 before storing.

### Bug #18: No account lockout mechanism
**Severity:** Critical  
**File:** (entire codebase)  
**Impact:** Unlimited brute force attempts against any user account.  
**Fix:** Add `failedLoginAttempts` counter on User entity, lock after N failures, implement cooldown.

### Bug #19: Login has no dedicated rate limiting
**Severity:** Critical  
**File:** `src/main/java/com/smartrental/security/RateLimitingFilter.java`  
**Impact:** Global rate limit (100 req/min) applies to ALL endpoints. Login can be brute-forced at 100 attempts/minute.  
**Fix:** Add per-IP rate limiting on `/auth/login` (e.g., 5 attempts/minute).

### Bug #20: JWT tokens stored in localStorage (XSS vulnerable)
**Severity:** Critical  
**File:** `frontend/src/context/AuthContext.jsx` (lines 30-32)  
**Impact:** Any XSS vulnerability can exfiltrate both access and refresh tokens.  
**Fix:** Use httpOnly cookies for token storage.

### Bug #21: N+1 query in `PropertyService.findByTenantId()`
**Severity:** High  
**File:** `src/main/java/com/smartrental/service/PropertyService.java` (lines 168-182)  
**Impact:** Loads ALL properties, then lazy-loads leases for each. Comment on line 170 says "In production add proper query" but never implemented.  
**Fix:** Add JPQL query with JOIN FETCH.

### Bug #22: In-memory payment filtering without pagination
**Severity:** High  
**File:** `src/main/java/com/smartrental/service/PaymentService.java` (lines 204-227)  
**Impact:** Loads ALL payments into memory for filtering. Performance degrades with data growth.  
**Fix:** Move filtering to database query level.

### Bug #23: ChatService constructs JSON via string concatenation
**Severity:** High  
**File:** `src/main/java/com/smartrental/service/ChatService.java` (lines 110-118)  
**Impact:** JSON injection risk if `escapeJson()` misses edge cases.  
**Fix:** Use ObjectMapper for JSON construction.

### Bug #24: ChatService parses OpenAI response via manual string search
**Severity:** High  
**File:** `src/main/java/com/smartrental/service/ChatService.java` (lines 195-224)  
**Impact:** Extremely fragile; will break on any API format change (whitespace, field order, nesting changes).  
**Fix:** Use ObjectMapper for JSON parsing.

### Bug #25: `Math.random()` used for billing estimates
**Severity:** Medium  
**File:** `src/main/java/com/smartrental/service/WaterMeterService.java` (lines 103, 106)  
**Impact:** Billing amounts are randomized. Can produce wildly inaccurate invoices.  
**Fix:** Use deterministic fallback (e.g., average of last 3 readings).
