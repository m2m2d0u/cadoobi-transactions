-- ============================================================================
-- Migration V26: Create Webhook Configurations Table
-- ============================================================================
-- This migration creates the webhook_configurations table for managing webhook endpoints
-- ============================================================================

-- Create webhook_configurations table
CREATE TABLE webhook_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    secret VARCHAR(64) NOT NULL,
    subscribed_events TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_triggered_at TIMESTAMP,
    max_retries INTEGER NOT NULL DEFAULT 3,
    timeout_seconds INTEGER NOT NULL DEFAULT 30,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_webhook_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_webhook_user ON webhook_configurations(user_id);
CREATE INDEX idx_webhook_active ON webhook_configurations(is_active);

-- Add comments
COMMENT ON TABLE webhook_configurations IS 'Webhook configurations for event notifications';
COMMENT ON COLUMN webhook_configurations.url IS 'The webhook callback URL where events will be sent';
COMMENT ON COLUMN webhook_configurations.secret IS 'Secret key used to sign webhook payloads for verification';
COMMENT ON COLUMN webhook_configurations.subscribed_events IS 'Comma-separated list of event types to subscribe to (null = all events)';
COMMENT ON COLUMN webhook_configurations.last_triggered_at IS 'Last time a webhook was triggered for this configuration';
COMMENT ON COLUMN webhook_configurations.max_retries IS 'Maximum number of retry attempts for failed webhook deliveries';
COMMENT ON COLUMN webhook_configurations.timeout_seconds IS 'Timeout in seconds for webhook HTTP requests';
