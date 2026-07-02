# Deployment Checklist — Sapthagiri Residency

## Pre-Deployment Verification

### Code Quality
- [ ] `mvn compile -q` passes (use `JAVA_HOME` pointing to JDK 21 on JDK 22+ hosts)
- [ ] `npm run build` completes without errors (frontend/dist produced)
- [ ] `mvn test` — 56/57 pass (1 Docker-dependent integration test expected to skip)
- [ ] No TODO comments or `console.log` debug statements in production code
- [ ] All API endpoints return correct status codes (200/201/204/400/401/404)
- [ ] JWT token expiry & refresh flow works in all roles

### Security
- [ ] `JWT_SECRET` changed from default to a secure 256+ bit random key
- [ ] CORS `allowed-origins` set to production frontend URL(s) only
- [ ] Rate limiting enabled on auth endpoints
- [ ] Spring Security CSRF handling reviewed (stateless JWT — CSRF disabled)
- [ ] Razorpay webhook HMAC verification enabled with correct secret
- [ ] File upload size limits enforced (10 MB per file)
- [ ] Multipart file type restrictions active (PDF, JPEG, PNG, DOC, DOCX)

### Database
- [ ] PostgreSQL running on target host
- [ ] Database `sapthagiri_db` (or configured name) created
- [ ] Flyway migrations run without errors on fresh database
- [ ] `spring.jpa.hibernate.ddl-auto` set to `validate` or `update` (not `create-drop`)
- [ ] Database connection pool tuned (defaults: Hikari max 20, min 5)
- [ ] DB credentials use environment variables, not hardcoded values
- [ ] Regular backup strategy in place (pg_dump or provider-managed)

### External Services
- [ ] SMTP credentials configured (Gmail app password or transactional email provider)
- [ ] Razorpay key ID + secret set (test mode OK for staging)
- [ ] Razorpay webhook endpoint registered in Razorpay dashboard → `POST /webhook/razorpay`
- [ ] Cloudinary cloud name, API key, API secret configured
- [ ] Twilio account SID, auth token, from-number configured
- [ ] OpenAI API key set (only needed if chatbot feature is active)

---

## Backend Deployment (Railway / Render / AWS / VPS)

### Minimum Requirements
- Java 21 runtime (Lombok 1.18.32 incompatible with JDK 22)
- 512 MB RAM (1 GB recommended)
- PostgreSQL 15+ (managed or self-hosted)

### Environment Variables (Railway)
| Variable | Example |
|----------|---------|
| `DB_URL` | `jdbc:postgresql://<host>:5432/sapthagiri_db` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `<secure-password>` |
| `JWT_SECRET` | `<256-bit-hex-or-base64-key>` |
| `MAIL_USERNAME` | `your-email@gmail.com` |
| `MAIL_PASSWORD` | `<gmail-app-password>` |
| `RAZORPAY_KEY_ID` | `rzp_live_...` |
| `RAZORPAY_KEY_SECRET` | `<secret>` |
| `RAZORPAY_WEBHOOK_SECRET` | `<webhook-secret>` |
| `TWILIO_ACCOUNT_SID` | `AC...` |
| `TWILIO_AUTH_TOKEN` | `<token>` |
| `TWILIO_FROM_NUMBER` | `+1XXXXXXXXXX` |
| `CLOUDINARY_CLOUD_NAME` | `your-cloud` |
| `CLOUDINARY_API_KEY` | `<key>` |
| `CLOUDINARY_API_SECRET` | `<secret>` |
| `OPENAI_API_KEY` | `sk-...` |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend.vercel.app` |
| `SKIP_EMAIL_VERIFICATION` | `true` (set `false` in production if email verification active) |

### Build & Deploy
```bash
# Build with JDK 21
JAVA_HOME=/path/to/jdk-21 mvn clean package -DskipTests

# Run
java -jar target/smart-rental-management-0.0.1-SNAPSHOT.jar
```

### Health Check
```
GET /api/actuator/health
# Expected: {"status":"UP"}
```

---

## Frontend Deployment (Vercel)

### Environment Variables
| Variable | Value |
|----------|-------|
| `VITE_API_URL` | `https://your-backend.up.railway.app/api` |

### Build Settings (Vercel)
- **Framework Preset:** Vite
- **Root Directory:** `frontend/`
- **Build Command:** `npm run build`
- **Output Directory:** `dist`
- **Node.js Version:** 18.x or 20.x

### Post-Deployment Checks
- [ ] Login page loads and authenticates
- [ ] All pages render without runtime console errors
- [ ] API calls return real data (not mock fallback)
- [ ] File upload/download works via Cloudinary
- [ ] Toast notifications appear for success/error states
- [ ] Mobile layout renders correctly (test on 375px width)
- [ ] Sidebar navigation works on mobile (hamburger menu)

---

## Post-Deployment Verification

### Critical Paths
- [ ] **Owner login** → Dashboard loads with real stats
- [ ] **Owner** → Tenants page → Add / Edit / Remove tenant
- [ ] **Owner** → Rent Tracker page → Mark payment as paid
- [ ] **Owner** → Payments page → View history → Export CSV
- [ ] **Owner** → Maintenance page → View requests
- [ ] **Owner** → Documents page → Upload / Download / Delete
- [ ] **Owner** → Reminders page → View reminders → Send reminder
- [ ] **Tenant login** → Dashboard shows unit info & payment status

### Edge Cases
- [ ] Expired JWT → redirect to login (401 interceptor)
- [ ] Empty property → empty states on all pages
- [ ] Network failure → graceful toast error (not blank page)
- [ ] Large file upload (>10 MB) → rejected with error message
- [ ] Concurrent payment marking → no duplicate transactions
- [ ] Razorpay webhook retry → idempotent processing

### Monitoring
- [ ] Spring Boot Actuator health endpoint accessible
- [ ] `GET /api/actuator/metrics` returning application metrics
- [ ] Application logs writing to file (configured in `application.yml`)
- [ ] Error tracking configured (optional: Sentry, Rollbar)

---

## Rollback Plan

### Database
```bash
# Identify current Flyway migration version
SELECT version, description FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;

# Rollback by restoring from backup
pg_restore -d sapthagiri_db latest_backup.dump
```

### Backend
```bash
# Deploy previous JAR version
java -jar smart-rental-management-<previous-version>.jar
```

### Frontend
- Vercel: Go to **Deployments** → Select previous deployment → **Promote to Production**

---

## Performance Checklist

- [ ] Frontend JS/CSS bundles under 1 MB (948 kB JS, 68 kB CSS — OK)
- [ ] Database queries indexed (JPA `@Index` on foreign keys)
- [ ] Scheduled tasks (`@Scheduled`) do not overlap (configured pool size = 10)
- [ ] HikariCP connection pool tuned for expected concurrent users
- [ ] Static assets served via CDN (Vercel handles this automatically)
- [ ] API responses paginated for large datasets (default page size = 20)

---

## Security Checklist

- [ ] HTTPS enforced at reverse proxy / CDN level
- [ ] JWT stored in `localStorage` — XSS protection via Content-Security-Policy
- [ ] Razorpay webhook secret stored as environment variable, never in code
- [ ] Flyway migrations reviewed for destructive operations
- [ ] `spring.jpa.show-sql` set to `false` in production
- [ ] `management.endpoints.web.exposure.include` limited (not `*`)
- [ ] CORS allows only production frontend origin
