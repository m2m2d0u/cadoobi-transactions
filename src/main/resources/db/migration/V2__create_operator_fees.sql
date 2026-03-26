CREATE TABLE operator_fees (
    id UUID PRIMARY KEY,
    operator_id UUID NOT NULL REFERENCES operators(id),
    operation_type VARCHAR(10) NOT NULL CHECK (operation_type IN ('PAYIN', 'PAYOUT')),
    fee_type VARCHAR(15) NOT NULL CHECK (fee_type IN ('PERCENTAGE', 'FIXED', 'MIXED')),
    fee_percentage NUMERIC(6,4),
    fee_fixed NUMERIC(15,2),
    min_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    max_amount NUMERIC(15,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'XOF',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_operator_operation_date UNIQUE (operator_id, operation_type, effective_from)
);

CREATE INDEX idx_operator_fee_operator ON operator_fees(operator_id);
CREATE INDEX idx_operator_fee_active ON operator_fees(is_active);
CREATE INDEX idx_operator_fee_dates ON operator_fees(effective_from, effective_to);
