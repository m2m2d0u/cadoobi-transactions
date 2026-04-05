-- ============================================================================
-- Migration V29: Alter API Key and Webhook Secret Column Lengths
-- ============================================================================
-- This migration increases the VARCHAR length for api_key and secret columns
-- to support encrypted values (AES-GCM encrypted data is longer than plaintext)
-- ============================================================================

-- Increase api_key column length to support encrypted values
ALTER TABLE api_keys ALTER COLUMN api_key TYPE VARCHAR(512);

-- Increase secret column length to support encrypted values
ALTER TABLE webhook_configurations ALTER COLUMN secret TYPE VARCHAR(512);

-- Add comments explaining the encryption
COMMENT ON COLUMN api_keys.api_key IS 'The API key token (AES-GCM encrypted, unique, prefixed with pk_ before encryption)';
COMMENT ON COLUMN webhook_configurations.secret IS 'Secret key used to sign webhook payloads (AES-GCM encrypted, prefixed with whsec_ before encryption)';
