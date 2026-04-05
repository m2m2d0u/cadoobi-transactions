-- ============================================================================
-- Migration V25: Create API Keys Table
-- ============================================================================
-- This migration creates the api_keys table for managing API key authentication
-- ============================================================================

-- Create api_keys table
CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    allowed_referrers TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_api_key_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE UNIQUE INDEX idx_api_key_token ON api_keys(api_key);
CREATE INDEX idx_api_key_user ON api_keys(user_id);
CREATE INDEX idx_api_key_active ON api_keys(is_active);

-- Add comments
COMMENT ON TABLE api_keys IS 'API keys for external authentication';
COMMENT ON COLUMN api_keys.api_key IS 'The actual API key token (unique, prefixed with pk_)';
COMMENT ON COLUMN api_keys.allowed_referrers IS 'Comma-separated list of allowed referrer domains/URLs for security';
COMMENT ON COLUMN api_keys.expires_at IS 'Optional expiration timestamp for the API key';
COMMENT ON COLUMN api_keys.last_used_at IS 'Last time the API key was used for authentication';
