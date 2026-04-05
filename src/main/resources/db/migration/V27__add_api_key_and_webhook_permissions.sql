-- ============================================================================
-- Migration V27: Add API Key and Webhook Permissions
-- ============================================================================
-- This migration adds permissions for managing API keys and webhooks
-- ============================================================================

-- API Key Permissions
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('api-key:read', 'View API Keys', 'api-key', 'read', 'Ability to view API keys', true),
  ('api-key:create', 'Create API Keys', 'api-key', 'create', 'Ability to create new API keys', true),
  ('api-key:update', 'Update API Keys', 'api-key', 'update', 'Ability to update existing API keys', true),
  ('api-key:delete', 'Delete API Keys', 'api-key', 'delete', 'Ability to delete API keys', true);

-- Webhook Permissions
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('webhook:read', 'View Webhooks', 'webhook', 'read', 'Ability to view webhook configurations', true),
  ('webhook:create', 'Create Webhooks', 'webhook', 'create', 'Ability to create new webhook configurations', true),
  ('webhook:update', 'Update Webhooks', 'webhook', 'update', 'Ability to update existing webhook configurations', true),
  ('webhook:delete', 'Delete Webhooks', 'webhook', 'delete', 'Ability to delete webhook configurations', true);

-- ============================================================================
-- Link API Key and Webhook Permissions to Roles
-- ============================================================================

-- SUPER_ADMIN: Full access to API keys and webhooks
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
AND p.code IN (
    'api-key:read', 'api-key:create', 'api-key:update', 'api-key:delete',
    'webhook:read', 'webhook:create', 'webhook:update', 'webhook:delete'
);

-- ADMIN: Full access to API keys and webhooks
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
AND p.code IN (
    'api-key:read', 'api-key:create', 'api-key:update', 'api-key:delete',
    'webhook:read', 'webhook:create', 'webhook:update', 'webhook:delete'
);

-- MERCHANT: Full access to their own API keys and webhooks
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MERCHANT'
AND p.code IN (
    'api-key:read', 'api-key:create', 'api-key:update', 'api-key:delete',
    'webhook:read', 'webhook:create', 'webhook:update', 'webhook:delete'
);

-- FINANCE_MANAGER: Read-only access to API keys and webhooks
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'FINANCE_MANAGER'
AND p.code IN ('api-key:read', 'webhook:read');

-- VIEWER: Read-only access to API keys and webhooks
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
AND p.code IN ('api-key:read', 'webhook:read');
