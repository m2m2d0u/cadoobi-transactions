-- ============================================================================
-- Migration V23: Create Parameters Table
-- ============================================================================
-- This migration creates the parameters table for system configuration
-- and seeds it with default values
-- ============================================================================

-- Create parameters table
CREATE TABLE parameters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    param_key VARCHAR(100) NOT NULL UNIQUE,
    param_value TEXT,
    category VARCHAR(50),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_parameter_key ON parameters(param_key);
CREATE INDEX idx_parameter_category ON parameters(category);

-- ============================================================================
-- Seed Default Parameters
-- ============================================================================

-- PAYMENT Parameters
INSERT INTO parameters (param_key, param_value, category, description, is_active, is_system)
VALUES
  ('payment.timeout.seconds', '120', 'PAYMENT', 'Payment transaction timeout in seconds', true, true),
  ('payment.expiry.hours', '24', 'PAYMENT', 'Payment expiration time in hours', true, true),
  ('payment.max.retries', '3', 'PAYMENT', 'Maximum number of payment retry attempts', true, true),
  ('payment.min.amount', '100', 'PAYMENT', 'Minimum payment amount allowed', true, true),
  ('payment.max.amount', '5000000', 'PAYMENT', 'Maximum payment amount allowed', true, true);

-- PAYOUT Parameters
INSERT INTO parameters (param_key, param_value, category, description, is_active, is_system)
VALUES
  ('payout.timeout.seconds', '180', 'PAYOUT', 'Payout transaction timeout in seconds', true, true),
  ('payout.max.amount', '1000000', 'PAYOUT', 'Maximum payout amount allowed per transaction', true, true),
  ('payout.daily.limit', '5000000', 'PAYOUT', 'Daily payout limit per merchant', true, true),
  ('payout.min.amount', '500', 'PAYOUT', 'Minimum payout amount allowed', true, true),
  ('payout.require.approval', 'false', 'PAYOUT', 'Require manual approval for payouts', true, true);

-- NOTIFICATION Parameters
INSERT INTO parameters (param_key, param_value, category, description, is_active, is_system)
VALUES
  ('notification.retry.max', '5', 'NOTIFICATION', 'Maximum notification retry attempts', true, true),
  ('notification.retry.delay.seconds', '60', 'NOTIFICATION', 'Delay between notification retries in seconds', true, true),
  ('notification.timeout.seconds', '30', 'NOTIFICATION', 'Notification HTTP request timeout in seconds', true, true);

-- SYSTEM Parameters
INSERT INTO parameters (param_key, param_value, category, description, is_active, is_system)
VALUES
  ('system.maintenance.mode', 'false', 'SYSTEM', 'Enable/disable maintenance mode', true, true),
  ('system.api.version', '1.0.0', 'SYSTEM', 'Current API version', true, true),
  ('system.max.page.size', '100', 'SYSTEM', 'Maximum page size for paginated endpoints', true, true),
  ('system.session.timeout.minutes', '30', 'SYSTEM', 'User session timeout in minutes', true, true);

-- FEES Parameters
INSERT INTO parameters (param_key, param_value, category, description, is_active, is_system)
VALUES
  ('fees.apply.on.create', 'true', 'FEES', 'Automatically apply default fees to new merchants', true, true),
  ('fees.require.approval', 'false', 'FEES', 'Require approval for merchant fee changes', true, true);

-- SECURITY Parameters
INSERT INTO parameters (param_key, param_value, category, description, is_active, is_system)
VALUES
  ('security.max.login.attempts', '5', 'SECURITY', 'Maximum failed login attempts before lockout', true, true),
  ('security.lockout.duration.minutes', '30', 'SECURITY', 'Account lockout duration after max failed attempts', true, true),
  ('security.password.min.length', '8', 'SECURITY', 'Minimum password length', true, true);
