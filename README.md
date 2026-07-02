<div align="center">

# 🏠 Sapthagiri Residency
### Smart Property Management System

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-blue?logo=react&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-red?logo=jsonwebtokens&logoColor=white)
![Tailwind](https://img.shields.io/badge/Tailwind%20CSS-4-06B6D4?logo=tailwindcss&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white)

**A production-ready full-stack rental management platform — owner dashboard, tenant portal, AI chatbot, automated reminders, Razorpay payments, document management, and premium glassmorphism UI.**

[Backend API](https://smartrental-api.up.railway.app/api) · [Frontend](https://sapthagiri-residency.vercel.app)

</div>

---

## ✨ Features

### 🏠 Property Management
- Manage 10+ rental units across multiple floors
- Real-time occupancy tracking with visual status badges
- Floor-wise unit organization with glassmorphism cards
- Status badges: Paid (green) • Pending (yellow) • Overdue (red pulse) • Vacant (grey)

### 💰 Rent & Payment Tracking
- Monthly payment status per unit with one-click **Mark as Paid**
- Overdue alerts with glow animations and pulse effects
- Razorpay payment gateway integration with webhook verification
- Payment history, CSV export, and collection charts
- Monthly income bar charts and annual trend lines

### 👥 Tenant Management
- Full CRUD with modal forms
- One-click Call & WhatsApp integration
- Role-based access (Owner/Landlord/Admin/Tenant)
- Search and filter tenants
- Lease start date tracking

### 📄 Document Management
- Upload rental agreements, ID proofs, NOC, receipts per property
- Drag-and-drop upload to Cloudinary
- File type indicators (PDF=red, IMG=blue, DOC=indigo)
- Download and delete with confirmation

### 🔔 Automated Reminders
- Email reminder 7 days before due date
- Email + SMS 3 days before due date
- Overdue alert at 3+ days past due
- Full reminder history with delivery status
- Configurable cron scheduling

### 💬 AI Chatbot Assistant (OpenAI)
- Tenants ask: *"When is my rent due?"*, *"What's my pending amount?"*
- Real-time database queries via OpenAI GPT
- Natural language responses with chat history

### 📊 Reports & Analytics
- Monthly income bar charts with gradient fills
- Annual income trend line charts
- Payment collection rate dashboard
- CSV export for all payment data

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21 + Spring Boot 3.2.5 |
| Frontend | React 19 + Vite 8 |
| Database | PostgreSQL 15 |
| Security | JWT + Spring Security + BCrypt |
| Styling | Tailwind CSS v4 + Framer Motion |
| Charts | Victory |
| Icons | Lucide React |
| Payments | Razorpay (webhook-verified) |
| Storage | Cloudinary |
| SMS | Twilio API |
| AI | OpenAI GPT |
| Email | JavaMail (SMTP) |
| Deployment | Railway (Backend) + Vercel (Frontend) |

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────┐
│                 Frontend                      │
│  React 19 + Vite 8 + Tailwind v4             │
│  Glassmorphism Dark UI • JWT in localStorage  │
│              Port: 5173                       │
└──────────────────┬────────────────────────────┘
                   │ Axios (JWT Bearer Token)
                   ▼
┌──────────────────────────────────────────────┐
│                 Backend (context-path: /api)  │
│  Spring Boot 3.2.5 • Spring Security • JWT   │
│  Controller → Service → Repository → Model   │
│              Port: 8080                       │
└──────────────────┬────────────────────────────┘
                   │ JDBC
                   ▼
┌──────────────────────────────────────────────┐
│                Database                       │
│    PostgreSQL (15+ tables, Flyway migrations) │
│              Port: 5432                       │
└──────────────────────────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21 (JDK — **Lombok 1.18.32 requires JDK 21 or lower**)
- Node.js 18+
- PostgreSQL 15+
- Maven 3.8+

### 1. Clone
```bash
git clone https://github.com/yourusername/sapthagiri-residency-management.git
cd sapthagiri-residency-management
```

### 2. Database Setup
```bash
createdb -U postgres sapthagiri_db
```

### 3. Backend
```bash
# Configure application.properties (or set env vars)
# Key: spring.datasource.url, jwt.secret, mail/payment/cloudinary keys

mvn spring-boot:run
# → http://localhost:8080/api
```

> **Note:** On **JDK 22+** hosts, Lombok 1.18.32 throws `TypeTag.UNKNOWN`.  
> Set `JAVA_HOME` to point to JDK 21:  
> `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home mvn compile`

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### Default Login
| Role | Email | Password |
|------|-------|----------|
| Owner | `elumalai@sapthagiri.com` | `owner123` |

---

## 📁 Project Structure

```
sapthagiri-residency-management/
├── src/main/java/com/smartrental/
│   ├── controller/           # REST Controllers (10+ modules)
│   ├── service/              # Business logic + Scheduled tasks
│   ├── repository/           # Spring Data JPA repositories
│   ├── model/                # Entities + Enums + DTOs
│   ├── security/             # JWT filter + Security config
│   ├── config/               # App configuration classes
│   └── exception/            # Global exception handler
├── src/main/resources/
│   ├── application.yml       # Primary config (context-path: /api)
│   ├── application.properties# Overrides (DB, JWT secret, mail)
│   └── db/migration/         # Flyway schema migrations
├── frontend/
│   ├── src/
│   │   ├── components/       # Layout, UI, modals, ErrorBoundary
│   │   ├── pages/            # 11 pages (Dashboard, Tenants, etc.)
│   │   ├── context/          # AuthContext (JWT management)
│   │   ├── hooks/            # useCountUp custom hook
│   │   ├── utils/            # axios.js, api.js, cn.js
│   │   └── data/             # Mock data fallbacks
│   └── package.json
├── pom.xml
├── DEPLOYMENT_CHECKLIST.md
└── README.md
```

---

## 📡 API Reference

### Auth (`/auth`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | ❌ | Register new user |
| POST | `/auth/login` | ❌ | Login → JWT tokens |
| GET | `/auth/me` | ✅ | Current user profile |
| POST | `/auth/logout` | ✅ | Invalidate session |

### Dashboard (`/v1/dashboard`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/dashboard/summary` | ✅ | Owner dashboard stats |
| GET | `/v1/dashboard/units` | ✅ | Units with tenant/rent status |
| GET | `/v1/dashboard/tenant` | ✅ | Tenant dashboard data |

### Properties (`/v1/properties`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/properties` | ✅ | List all properties |
| POST | `/v1/properties` | ✅ | Create property |
| PUT | `/v1/properties/{id}` | ✅ | Update property |
| DELETE | `/v1/properties/{id}` | ✅ | Soft-delete property |

### Leases / Tenants (`/v1/leases` + `/v1/tenants`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/leases/active` | ✅ | Active leases (owner) |
| GET | `/v1/leases/tenant/me` | ✅ | My lease (tenant) |
| POST | `/v1/tenants` | ✅ | Assign tenant to unit |
| PUT | `/v1/tenants/{leaseId}` | ✅ | Update tenant details |
| DELETE | `/v1/tenants/{leaseId}` | ✅ | Remove tenant |

### Payments (`/v1/payments`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/payments` | ✅ | All payments |
| GET | `/v1/payments/tenant/me` | ✅ | My payments (tenant) |
| POST | `/v1/payments` | ✅ | Create pending payment |
| PUT | `/v1/payments/{id}/mark-paid` | ✅ | Mark as paid |
| DELETE | `/v1/payments/{id}` | ✅ | Soft-delete payment |
| GET | `/v1/payments/export/csv` | ✅ | CSV export |

### Maintenance (`/v1/maintenance`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/maintenance` | ✅ | All requests |
| POST | `/v1/maintenance` | ✅ | Create request |
| PUT | `/v1/maintenance/{id}/status` | ✅ | Update status |
| POST | `/v1/maintenance/{id}/comments` | ✅ | Add comment |

### Documents (`/v1/documents`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/documents` | ✅ | All documents |
| POST | `/v1/documents` | ✅ | Upload (multipart) |
| DELETE | `/v1/documents/{id}` | ✅ | Delete (incl. Cloudinary) |

### Reminders (`/v1/reminders`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/reminders` | ✅ | List reminders |
| GET | `/v1/reminders/logs` | ✅ | Sent reminder logs |
| POST | `/v1/reminders/{id}/send` | ✅ | Send reminder now |

### Water Meter (`/v1/water-meter`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/water-meter/read-photo` | ✅ | OCR meter reading |
| POST | `/v1/water-meter/calculate` | ✅ | Calculate bill |
| POST | `/v1/water-meter/save` | ✅ | Save & send bill |
| GET | `/v1/water-meter/occupied` | ✅ | Occupied units |

### Owner Portal (`/v1/owner`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/owner/units` | ✅ | Units with rent status |
| POST | `/v1/owner/payments/mark-paid` | ✅ | Mark paid by unit |
| GET | `/v1/owner/dashboard/stats` | ✅ | Dashboard stats |

### Webhooks
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/webhook/razorpay` | HMAC | Payment.webhook |

---

## ⚙️ Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/sapthagiri_db` | Database URL |
| `JWT_SECRET` | *(embedded)* | HMAC-SHA256 key (256+ bits) |
| `MAIL_USERNAME` | — | Gmail SMTP user |
| `MAIL_PASSWORD` | — | Gmail app password |
| `RAZORPAY_KEY_ID` | — | Razorpay API key |
| `RAZORPAY_KEY_SECRET` | — | Razorpay API secret |
| `RAZORPAY_WEBHOOK_SECRET` | — | Webhook HMAC secret |
| `TWILIO_ACCOUNT_SID` | — | Twilio SID |
| `TWILIO_AUTH_TOKEN` | — | Twilio auth token |
| `CLOUDINARY_CLOUD_NAME` | — | Cloudinary cloud name |
| `OPENAI_API_KEY` | — | OpenAI API key |

---

## 🧪 Testing

```bash
# Unit & integration tests (56/57 pass; 1 Docker-dependent)
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home mvn test
```

The single skipped test (`PaymentServiceIntegrationTest`) requires a running PostgreSQL + Redis (Docker). All unit tests pass.

### Test coverage areas:
- Auth: register, login, logout, token refresh
- Payments: create, mark paid, overdue detection, CSV export
- Maintenance: CRUD, status transitions, comments
- Documents: upload, download, delete
- Reminders: scheduled creation, send, overdue flagging
- Water meter: CRUD, calculation, billing

---

## 🌐 Deployment

See [DEPLOYMENT_CHECKLIST.md](./DEPLOYMENT_CHECKLIST.md) for a complete production deployment guide.

---

## 📄 License

MIT
