CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY,
    reference VARCHAR(100) NOT NULL UNIQUE,
    merchant_id VARCHAR(36) NOT NULL,
    merchant_code VARCHAR(10) NOT NULL,
    operator_id UUID NOT NULL REFERENCES operators(id),
    amount NUMERIC(15,2) NOT NULL,
    fee_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'XOF',
    payer_phone VARCHAR(20) NOT NULL,
    payer_full_name VARCHAR(150),
    recipient_phone VARCHAR(20) NOT NULL,
    recipient_name VARCHAR(150),
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED' CHECK (status IN ('INITIATED', 'PENDING', 'COMPLETED', 'FAILED', 'EXPIRED', 'CANCELLED')),
    operator_transaction_id VARCHAR(100) UNIQUE,
    payment_url VARCHAR(1000),
    callback_url VARCHAR(500) NOT NULL,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_reference ON payment_transactions(reference);
CREATE INDEX idx_payment_status ON payment_transactions(status);
CREATE INDEX idx_payment_merchant ON payment_transactions(merchant_id);
CREATE INDEX idx_payment_operator ON payment_transactions(operator_id);
CREATE INDEX idx_payment_operator_txn ON payment_transactions(operator_transaction_id);
