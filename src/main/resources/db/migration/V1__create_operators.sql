CREATE TABLE operators (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(2) NOT NULL,
    supports_payin BOOLEAN NOT NULL DEFAULT TRUE,
    supports_payout BOOLEAN NOT NULL DEFAULT TRUE,
    api_base_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_operator_code ON operators(code);
CREATE INDEX idx_operator_country ON operators(country);
CREATE INDEX idx_operator_active ON operators(is_active);
