-- ============================================================
-- V17: Cadoobi platform system account
-- Tracks all earnings collected by the platform (merchant fees).
-- ============================================================

CREATE TABLE system_accounts (
    id          UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    currency    VARCHAR(3)    NOT NULL UNIQUE,
    balance     NUMERIC(15,2) NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_system_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE system_account_entries (
    id                    UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    system_account_id     UUID          NOT NULL REFERENCES system_accounts(id),
    direction             VARCHAR(6)    NOT NULL,   -- CREDIT | DEBIT
    entry_type            VARCHAR(30)   NOT NULL,   -- see SystemEntryType enum
    amount                NUMERIC(15,2) NOT NULL,
    currency              VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    description           VARCHAR(255),
    idempotency_key       VARCHAR(100)  NOT NULL UNIQUE,
    payout_transaction_id UUID          REFERENCES payout_transactions(id),
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sys_entry_account    ON system_account_entries(system_account_id);
CREATE INDEX idx_sys_entry_type       ON system_account_entries(entry_type);
CREATE INDEX idx_sys_entry_created    ON system_account_entries(created_at);
CREATE INDEX idx_sys_entry_payout_tx  ON system_account_entries(payout_transaction_id);

-- Seed the default XOF system account
INSERT INTO system_accounts (currency) VALUES ('XOF');
