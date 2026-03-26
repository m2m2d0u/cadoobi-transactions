CREATE TABLE operator_callbacks (
    id UUID PRIMARY KEY,
    payment_transaction_id UUID REFERENCES payment_transactions(id),
    operator_id UUID NOT NULL REFERENCES operators(id),
    operator_reference VARCHAR(100) NOT NULL UNIQUE,
    raw_payload TEXT NOT NULL,
    operator_status VARCHAR(50),
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_callback_payment ON operator_callbacks(payment_transaction_id);
CREATE INDEX idx_callback_operator ON operator_callbacks(operator_id);
CREATE INDEX idx_callback_reference ON operator_callbacks(operator_reference);
CREATE INDEX idx_callback_processed ON operator_callbacks(processed_at);
