-- ============================================================================
-- Migration V22: Add System Account Permissions
-- ============================================================================
-- This migration adds permissions for viewing and managing system accounts
-- ============================================================================

-- System Account Permissions
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('system-account:read', 'View System Account', 'system-account', 'read', 'Ability to view system account balances and entries', true),
  ('system-account:write', 'Manage System Account', 'system-account', 'write', 'Ability to create manual system account adjustments', true);

-- ============================================================================
-- Link System Account Permissions to Roles
-- ============================================================================

-- SUPER_ADMIN: Full access to system accounts (read + write)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
AND p.code IN ('system-account:read', 'system-account:write');

-- ADMIN: Read-only access to system accounts
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
AND p.code = 'system-account:read';

-- FINANCE_MANAGER: Read-only access to system accounts
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'FINANCE_MANAGER'
AND p.code = 'system-account:read';

-- VIEWER: Read-only access to system accounts
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
AND p.code = 'system-account:read';
