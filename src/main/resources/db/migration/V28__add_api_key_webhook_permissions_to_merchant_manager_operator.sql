-- ============================================================================
-- Migration V28: Add API Key and Webhook Permissions to MERCHANT_MANAGER and OPERATOR
-- ============================================================================
-- This migration grants API key and webhook management permissions to
-- MERCHANT_MANAGER and OPERATOR roles
-- ============================================================================

-- MERCHANT_MANAGER: Full access to API keys and webhooks
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MERCHANT_MANAGER'
AND p.code IN (
    'api-key:read', 'api-key:create', 'api-key:update', 'api-key:delete',
    'webhook:read', 'webhook:create', 'webhook:update', 'webhook:delete'
);

-- OPERATOR: Full access to API keys and webhooks
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'OPERATOR'
AND p.code IN (
    'api-key:read', 'api-key:create', 'api-key:update', 'api-key:delete',
    'webhook:read', 'webhook:create', 'webhook:update', 'webhook:delete'
);
