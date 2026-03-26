CREATE TABLE gift_cards (
    id UUID PRIMARY KEY,
    payment_transaction_id UUID NOT NULL UNIQUE REFERENCES payment_transactions(id),
    merchant_id VARCHAR(36) NOT NULL,
    card_code VARCHAR(50) NOT NULL UNIQUE,
    qr_code_data TEXT,
    initial_amount NUMERIC(15,2) NOT NULL,
    balance NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'XOF',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PARTIALLY_USED', 'FULLY_USED', 'EXPIRED', 'BLOCKED')),
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_card_code ON gift_cards(card_code);
CREATE INDEX idx_card_merchant ON gift_cards(merchant_id);
CREATE INDEX idx_card_status ON gift_cards(status);
CREATE INDEX idx_card_payment ON gift_cards(payment_transaction_id);
