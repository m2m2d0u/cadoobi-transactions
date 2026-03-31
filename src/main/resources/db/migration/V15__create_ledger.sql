-- ============================================================
-- V15: Merchant ledger — account balances + entry log
-- ============================================================

-- One balance row per merchant per currency
CREATE TABLE merchant_accounts (
    id              UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    merchant_id     UUID          NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    currency        VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    balance         NUMERIC(15,2) NOT NULL DEFAULT 0,
    locked_balance  NUMERIC(15,2) NOT NULL DEFAULT 0,
    version         BIGINT        NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_merchant_account UNIQUE (merchant_id, currency),
    CONSTRAINT chk_balance_non_negative     CHECK (balance >= 0),
    CONSTRAINT chk_locked_non_negative      CHECK (locked_balance >= 0),
    CONSTRAINT chk_locked_lte_balance       CHECK (locked_balance <= balance)
);

CREATE INDEX idx_merchant_account_merchant ON merchant_accounts(merchant_id);

-- ============================================================
-- Immutable ledger entry log
-- ============================================================

CREATE TABLE ledger_entries (
    id                       UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    merchant_account_id      UUID          NOT NULL REFERENCES merchant_accounts(id),
    direction                VARCHAR(6)    NOT NULL,        -- CREDIT | DEBIT
    entry_type               VARCHAR(30)   NOT NULL,        -- see LedgerEntryType enum
    amount                   NUMERIC(15,2) NOT NULL,
    currency                 VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    description              VARCHAR(255),
    idempotency_key          VARCHAR(100)  NOT NULL UNIQUE,
    payment_transaction_id   UUID          REFERENCES payment_transactions(id),
    payout_transaction_id    UUID          REFERENCES payout_transactions(id),
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_account   ON ledger_entries(merchant_account_id);
CREATE INDEX idx_ledger_direction  ON ledger_entries(direction);
CREATE INDEX idx_ledger_type       ON ledger_entries(entry_type);
CREATE INDEX idx_ledger_created    ON ledger_entries(created_at);
CREATE INDEX idx_ledger_payment_tx ON ledger_entries(payment_transaction_id);
CREATE INDEX idx_ledger_payout_tx  ON ledger_entries(payout_transaction_id);

-- ============================================================
-- Add merchant_fee_amount to payout_transactions
-- (stores the Cadoobi fee charged on top of the declared amount)
-- ============================================================

ALTER TABLE payout_transactions
    ADD COLUMN IF NOT EXISTS merchant_fee_amount NUMERIC(15,2) NOT NULL DEFAULT 0;
