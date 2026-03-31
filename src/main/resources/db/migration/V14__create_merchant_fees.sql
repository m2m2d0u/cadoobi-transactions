-- ============================================================
-- V14: Merchant payout fee configurations
-- Merchant fees are payout-only — no operation_type column.
-- ============================================================

CREATE TABLE merchant_fees (
    id             UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    merchant_id    UUID          NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    fee_type       VARCHAR(15)   NOT NULL,
    fee_percentage NUMERIC(6,4),
    fee_fixed      NUMERIC(15,2),
    min_amount     NUMERIC(15,2) NOT NULL DEFAULT 0,
    max_amount     NUMERIC(15,2),
    currency       VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE,
    effective_from DATE          NOT NULL,
    effective_to   DATE,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_merchant_fee_merchant ON merchant_fees(merchant_id);
CREATE INDEX idx_merchant_fee_active   ON merchant_fees(is_active);
CREATE INDEX idx_merchant_fee_dates    ON merchant_fees(effective_from, effective_to);

-- ============================================================
-- Default merchant fee templates
-- Active records are copied to every new merchant at creation.
-- ============================================================

CREATE TABLE default_merchant_fees (
    id             UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    description    VARCHAR(255),
    fee_type       VARCHAR(15)   NOT NULL,
    fee_percentage NUMERIC(6,4),
    fee_fixed      NUMERIC(15,2),
    min_amount     NUMERIC(15,2) NOT NULL DEFAULT 0,
    max_amount     NUMERIC(15,2),
    currency       VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE,
    effective_to   DATE,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_default_fee_active ON default_merchant_fees(is_active);

-- ============================================================
-- Seed: single default payout fee of 2%
-- ============================================================

INSERT INTO default_merchant_fees
    (id, description, fee_type, fee_percentage, fee_fixed,
     min_amount, max_amount, currency, is_active, effective_to, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Default payout fee (2%)', 'PERCENTAGE', 0.0200, NULL,
     0, NULL, 'XOF', TRUE, NULL, NOW(), NOW());
