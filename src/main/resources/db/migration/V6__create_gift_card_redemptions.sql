CREATE TABLE gift_card_redemptions (
    id UUID PRIMARY KEY,
    gift_card_id UUID NOT NULL REFERENCES gift_cards(id),
    merchant_id VARCHAR(36) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    amount_redeemed NUMERIC(15,2) NOT NULL,
    remaining_balance NUMERIC(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    redeemed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_redemption_card ON gift_card_redemptions(gift_card_id);
CREATE INDEX idx_redemption_merchant ON gift_card_redemptions(merchant_id);
CREATE INDEX idx_redemption_status ON gift_card_redemptions(status);
CREATE INDEX idx_redemption_idempotency ON gift_card_redemptions(idempotency_key);
