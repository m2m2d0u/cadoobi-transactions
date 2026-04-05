-- ============================================================================
-- Migration V24: Add Parameter Permissions
-- ============================================================================
-- This migration adds permissions for managing system parameters
-- ============================================================================

-- Parameter Permissions
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('parameter:read', 'View Parameters', 'parameter', 'read', 'Ability to view system parameters', true),
  ('parameter:create', 'Create Parameters', 'parameter', 'create', 'Ability to create new system parameters', true),
  ('parameter:update', 'Update Parameters', 'parameter', 'update', 'Ability to update existing system parameters', true),
  ('parameter:delete', 'Delete Parameters', 'parameter', 'delete', 'Ability to delete non-system parameters', true);

-- ============================================================================
-- Link Parameter Permissions to Roles
-- ============================================================================

-- SUPER_ADMIN: Full access to parameters (read, create, update, delete)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
AND p.code IN ('parameter:read', 'parameter:create', 'parameter:update', 'parameter:delete');

-- ADMIN: Full access to parameters (read, create, update, delete)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
AND p.code IN ('parameter:read', 'parameter:create', 'parameter:update', 'parameter:delete');

-- FINANCE_MANAGER: Read-only access to parameters
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'FINANCE_MANAGER'
AND p.code = 'parameter:read';

-- VIEWER: Read-only access to parameters
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
AND p.code = 'parameter:read';
