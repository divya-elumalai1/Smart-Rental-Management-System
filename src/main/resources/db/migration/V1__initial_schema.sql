-- ===========================================
-- Smart Rental Management — Initial Schema
-- Migration: V1__initial_schema.sql
-- ===========================================
-- Creates all 9 tables matching JPA entity mappings.
-- ddl-auto is set to 'validate', so every column
-- mapped by Hibernate must exist here exactly.
-- ===========================================

-- pgcrypto provides gen_random_uuid() for PostgreSQL < 13
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ===========================================
-- 1. USERS
-- ===========================================
CREATE TABLE users (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name                      VARCHAR(50)  NOT NULL,
    last_name                       VARCHAR(50)  NOT NULL,
    email                           VARCHAR(100) NOT NULL,
    password                        VARCHAR(100) NOT NULL,
    phone_number                    VARCHAR(20)  NOT NULL,
    role                            VARCHAR(20)  NOT NULL,
    date_of_birth                   DATE,
    address                         VARCHAR(500),
    city                            VARCHAR(100),
    state                           VARCHAR(100),
    postal_code                     VARCHAR(20),
    country                         VARCHAR(100),
    profile_image_url               VARCHAR(500),
    is_email_verified               BOOLEAN      NOT NULL DEFAULT FALSE,
    is_phone_verified               BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active                       BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at                   TIMESTAMP,
    email_verification_token        VARCHAR(500),
    email_verification_token_expiry TIMESTAMP,
    reset_token                     VARCHAR(500),
    reset_token_expiry              TIMESTAMP,
    refresh_token                   VARCHAR(1000),
    refresh_token_expiry            TIMESTAMP,
    two_factor_enabled              BOOLEAN      NOT NULL DEFAULT FALSE,
    two_factor_secret               VARCHAR(100),
    backup_codes                    VARCHAR(1000),
    created_at                      TIMESTAMP    NOT NULL,
    updated_at                      TIMESTAMP    NOT NULL,
    is_deleted                      BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at                      TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone_number)
);

CREATE INDEX idx_users_email      ON users (email);
CREATE INDEX idx_users_role       ON users (role);
CREATE INDEX idx_users_deleted    ON users (is_deleted);
CREATE INDEX idx_users_created_at ON users (created_at);

-- ===========================================
-- 2. PROPERTIES
-- ===========================================
CREATE TABLE properties (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    landlord_id        UUID          NOT NULL REFERENCES users (id),
    address            VARCHAR(500)  NOT NULL,
    city               VARCHAR(100)  NOT NULL,
    state              VARCHAR(100),
    postal_code        VARCHAR(20),
    rent_amount        NUMERIC(12,2) NOT NULL,
    deposit            NUMERIC(12,2),
    bedrooms           INTEGER,
    bathrooms          NUMERIC(3,1),
    area_sqft          INTEGER,
    furnishing_status  VARCHAR(20),
    amenities          VARCHAR(2000),
    description        VARCHAR(2000),
    status             VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE',
    image_url          VARCHAR(500),
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL,
    is_deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at         TIMESTAMP,
    CONSTRAINT chk_properties_rent CHECK (rent_amount > 0)
);

CREATE INDEX idx_properties_landlord_id ON properties (landlord_id);
CREATE INDEX idx_properties_status     ON properties (status);
CREATE INDEX idx_properties_city       ON properties (city);
CREATE INDEX idx_properties_deleted    ON properties (is_deleted);

-- ===========================================
-- 3. LEASES
-- ===========================================
CREATE TABLE leases (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID          NOT NULL REFERENCES users (id),
    property_id           UUID          NOT NULL REFERENCES properties (id),
    start_date            DATE          NOT NULL,
    end_date              DATE          NOT NULL,
    rent_amount           NUMERIC(12,2) NOT NULL,
    deposit_amount        NUMERIC(12,2),
    status                VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    lease_document_url    VARCHAR(500),
    terms_and_conditions  VARCHAR(5000),
    created_at            TIMESTAMP     NOT NULL,
    updated_at            TIMESTAMP     NOT NULL,
    is_deleted            BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at            TIMESTAMP,
    CONSTRAINT chk_leases_dates CHECK (end_date > start_date),
    CONSTRAINT chk_leases_rent  CHECK (rent_amount > 0)
);

CREATE INDEX idx_leases_tenant_id   ON leases (tenant_id);
CREATE INDEX idx_leases_property_id ON leases (property_id);
CREATE INDEX idx_leases_status      ON leases (status);
CREATE INDEX idx_leases_end_date    ON leases (end_date);
CREATE INDEX idx_leases_deleted     ON leases (is_deleted);

-- ===========================================
-- 4. PAYMENTS
-- ===========================================
CREATE TABLE payments (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            UUID          NOT NULL REFERENCES users (id),
    property_id          UUID          NOT NULL REFERENCES properties (id),
    lease_id             UUID          REFERENCES leases (id),
    amount               NUMERIC(12,2) NOT NULL,
    currency             VARCHAR(3)    NOT NULL DEFAULT 'INR',
    razorpay_order_id    VARCHAR(100),
    razorpay_payment_id  VARCHAR(100),
    razorpay_signature   VARCHAR(500),
    status               VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    payment_date         TIMESTAMP,
    due_date             DATE          NOT NULL,
    rent_period          DATE,
    receipt_url          VARCHAR(500),
    notes                VARCHAR(1000),
    created_at           TIMESTAMP     NOT NULL,
    updated_at           TIMESTAMP     NOT NULL,
    is_deleted           BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at           TIMESTAMP,
    CONSTRAINT chk_payments_amount CHECK (amount > 0)
);

CREATE INDEX idx_payments_tenant_id        ON payments (tenant_id);
CREATE INDEX idx_payments_property_id      ON payments (property_id);
CREATE INDEX idx_payments_status           ON payments (status);
CREATE INDEX idx_payments_due_date        ON payments (due_date);
CREATE INDEX idx_payments_payment_date     ON payments (payment_date);
CREATE INDEX idx_payments_razorpay_order   ON payments (razorpay_order_id);

-- ===========================================
-- 5. MAINTENANCE REQUESTS
-- ===========================================
CREATE TABLE maintenance_requests (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID          NOT NULL REFERENCES users (id),
    property_id      UUID          NOT NULL REFERENCES properties (id),
    title            VARCHAR(200)  NOT NULL,
    description      VARCHAR(5000) NOT NULL,
    priority         VARCHAR(20)   NOT NULL DEFAULT 'MEDIUM',
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    image_url        VARCHAR(500),
    resolved_at      TIMESTAMP,
    resolved_by      UUID,
    resolution_notes VARCHAR(2000),
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    is_deleted       BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at       TIMESTAMP
);

CREATE INDEX idx_maintreq_tenant_id   ON maintenance_requests (tenant_id);
CREATE INDEX idx_maintreq_property_id ON maintenance_requests (property_id);
CREATE INDEX idx_maintreq_status      ON maintenance_requests (status);
CREATE INDEX idx_maintreq_priority    ON maintenance_requests (priority);
CREATE INDEX idx_maintreq_created_at  ON maintenance_requests (created_at);

-- ===========================================
-- 6. MAINTENANCE COMMENTS
-- ===========================================
CREATE TABLE maintenance_comments (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID          NOT NULL REFERENCES maintenance_requests (id) ON DELETE CASCADE,
    user_id    UUID          NOT NULL REFERENCES users (id),
    comment    VARCHAR(2000) NOT NULL,
    image_url  VARCHAR(500),
    created_at TIMESTAMP     NOT NULL,
    updated_at TIMESTAMP     NOT NULL,
    is_deleted BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_maintcomment_request_id ON maintenance_comments (request_id);
CREATE INDEX idx_maintcomment_user_id    ON maintenance_comments (user_id);
CREATE INDEX idx_maintcomment_created_at ON maintenance_comments (created_at);

-- ===========================================
-- 7. DOCUMENTS
-- ===========================================
CREATE TABLE documents (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID          NOT NULL REFERENCES users (id),
    property_id UUID          REFERENCES properties (id),
    file_url    VARCHAR(1000) NOT NULL,
    file_name   VARCHAR(255)  NOT NULL,
    file_type   VARCHAR(100),
    file_size   BIGINT,
    public_id   VARCHAR(500),
    category    VARCHAR(20)   NOT NULL,
    description  VARCHAR(500),
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL,
    is_deleted  BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_documents_user_id     ON documents (user_id);
CREATE INDEX idx_documents_property_id ON documents (property_id);
CREATE INDEX idx_documents_category    ON documents (category);
CREATE INDEX idx_documents_created_at  ON documents (created_at);

-- ===========================================
-- 8. CHAT HISTORY
-- ===========================================
CREATE TABLE chat_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL REFERENCES users (id),
    message         VARCHAR(5000) NOT NULL,
    response        VARCHAR(10000),
    model_used      VARCHAR(50),
    tokens_used     INTEGER,
    conversation_id  UUID,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_chathistory_user_id    ON chat_history (user_id);
CREATE INDEX idx_chathistory_created_at ON chat_history (created_at);

-- ===========================================
-- 9. REMINDERS
-- ===========================================
CREATE TABLE reminders (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID         NOT NULL REFERENCES users (id),
    payment_id   UUID         REFERENCES payments (id),
    due_date     DATE         NOT NULL,
    type          VARCHAR(20)  NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    sent_at      TIMESTAMP,
    channel      VARCHAR(20)  DEFAULT 'EMAIL',
    error_message VARCHAR(1000),
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    is_deleted   BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at   TIMESTAMP
);

CREATE INDEX idx_reminders_tenant_id ON reminders (tenant_id);
CREATE INDEX idx_reminders_due_date  ON reminders (due_date);
CREATE INDEX idx_reminders_type      ON reminders (type);
CREATE INDEX idx_reminders_status    ON reminders (status);
CREATE INDEX idx_reminders_sent_at   ON reminders (sent_at);
