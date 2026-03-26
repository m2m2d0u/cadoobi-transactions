CREATE TABLE outbound_notifications (
    id UUID PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL CHECK (event_type IN ('PAYMENT_COMPLETED', 'PAYMENT_FAILED', 'CARD_REDEEMED', 'CASHIN_COMPLETED', 'CARD_EXPIRED')),
    target_url VARCHAR(500) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    attempts INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMPTZ,
    last_attempt_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_event_id ON outbound_notifications(event_id);
CREATE INDEX idx_notification_status ON outbound_notifications(status);
CREATE INDEX idx_notification_retry ON outbound_notifications(next_retry_at);
CREATE INDEX idx_notification_event_type ON outbound_notifications(event_type);
