CREATE TABLE payout_transactions (
    id UUID PRIMARY KEY,
    redemption_id UUID NOT NULL UNIQUE REFERENCES gift_card_redemptions(id),
    merchant_id VARCHAR(36) NOT NULL,
    operator_id UUID NOT NULL REFERENCES operators(id),
    recipient_number VARCHAR(255) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    fee_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'XOF',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    operator_transaction_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payout_redemption ON payout_transactions(redemption_id);
CREATE INDEX idx_payout_merchant ON payout_transactions(merchant_id);
CREATE INDEX idx_payout_status ON payout_transactions(status);
CREATE INDEX idx_payout_operator ON payout_transactions(operator_id);
CREATE INDEX idx_payout_idempotency ON payout_transactions(idempotency_key);
