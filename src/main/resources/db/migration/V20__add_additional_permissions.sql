-- ============================================================================
-- Migration V20: Add Additional Permissions and Link to Roles
-- ============================================================================
-- This migration adds comprehensive permissions for payouts, ledger,
-- dashboard, analytics, audit logs, and fine-grained resource controls.
-- ============================================================================

-- ============================================================================
-- Priority 1: CRITICAL Permissions
-- ============================================================================

-- Payout Permissions
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('payout:create', 'Create Payout', 'payout', 'create', 'Ability to create new payout transactions', true),
  ('payout:read', 'View Payouts', 'payout', 'read', 'Ability to view payout transactions', true),
  ('payout:update', 'Update Payout', 'payout', 'update', 'Ability to update payout status', true),
  ('payout:delete', 'Delete Payout', 'payout', 'delete', 'Ability to cancel pending payouts', true);

-- Ledger Permissions
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('ledger:read', 'View Ledger', 'ledger', 'read', 'Ability to view ledger entries', true),
  ('ledger:write', 'Manage Ledger', 'ledger', 'write', 'Ability to create manual ledger adjustments', true);

-- Complete Permission CRUD (read already exists from V11)
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('permission:create', 'Create Permission', 'permission', 'create', 'Ability to create new permissions', true),
  ('permission:update', 'Update Permission', 'permission', 'update', 'Ability to update permission details', true),
  ('permission:delete', 'Delete Permission', 'permission', 'delete', 'Ability to delete permissions', true);

-- ============================================================================
-- Priority 2: RECOMMENDED Permissions (Enhanced Security)
-- ============================================================================

-- Dashboard & Analytics
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('dashboard:view', 'View Dashboard', 'dashboard', 'view', 'Ability to access dashboard metrics', true),
  ('analytics:advanced', 'Advanced Analytics', 'analytics', 'view', 'Ability to view detailed analytics', true);

-- Audit & Logging
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('audit:read', 'View Audit Logs', 'audit', 'read', 'Ability to view audit logs', true),
  ('system:logs', 'View System Logs', 'system', 'logs', 'Ability to view system logs and errors', true);

-- ============================================================================
-- Priority 3: OPTIONAL Permissions (Future Enhancements)
-- ============================================================================

-- Merchant-specific fine-grained permissions
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('merchant:approve', 'Approve Merchant', 'merchant', 'approve', 'Ability to approve pending merchants', true),
  ('merchant:suspend', 'Suspend Merchant', 'merchant', 'suspend', 'Ability to suspend active merchants', true),
  ('merchant:ledger', 'View Merchant Ledger', 'merchant', 'ledger', 'Ability to view merchant ledger', true);

-- Operator fee management
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('operator:fee:create', 'Create Operator Fee', 'operator', 'fee:create', 'Ability to create operator fee configurations', true),
  ('operator:fee:update', 'Update Operator Fee', 'operator', 'fee:update', 'Ability to update operator fee configurations', true);

-- User-specific actions
INSERT INTO permissions (code, name, resource, action, description, is_active)
VALUES
  ('user:suspend', 'Suspend User', 'user', 'suspend', 'Ability to suspend user accounts', true),
  ('user:reset-password', 'Reset User Password', 'user', 'reset-password', 'Ability to reset user passwords', true);

-- ============================================================================
-- Link New Permissions to Roles
-- ============================================================================

-- SUPER_ADMIN: Grant all new permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
AND p.code IN (
    -- Payout
    'payout:create', 'payout:read', 'payout:update', 'payout:delete',
    -- Ledger
    'ledger:read', 'ledger:write',
    -- Permission CRUD
    'permission:create', 'permission:update', 'permission:delete',
    -- Dashboard & Analytics
    'dashboard:view', 'analytics:advanced',
    -- Audit & Logging
    'audit:read', 'system:logs',
    -- Merchant fine-grained
    'merchant:approve', 'merchant:suspend', 'merchant:ledger',
    -- Operator fee
    'operator:fee:create', 'operator:fee:update',
    -- User actions
    'user:suspend', 'user:reset-password'
);

-- ADMIN: Grant all new permissions except system:logs (SUPER_ADMIN only)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
AND p.code IN (
    -- Payout
    'payout:create', 'payout:read', 'payout:update', 'payout:delete',
    -- Ledger
    'ledger:read', 'ledger:write',
    -- Permission CRUD
    'permission:create', 'permission:update', 'permission:delete',
    -- Dashboard & Analytics
    'dashboard:view', 'analytics:advanced',
    -- Audit
    'audit:read',
    -- Merchant fine-grained
    'merchant:approve', 'merchant:suspend', 'merchant:ledger',
    -- Operator fee
    'operator:fee:create', 'operator:fee:update',
    -- User actions
    'user:suspend', 'user:reset-password'
);

-- FINANCE_MANAGER: Financial operations and analytics
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'FINANCE_MANAGER'
AND p.code IN (
    -- Payout (full CRUD)
    'payout:create', 'payout:read', 'payout:update', 'payout:delete',
    -- Ledger (read + write)
    'ledger:read', 'ledger:write',
    -- Dashboard & Analytics
    'dashboard:view', 'analytics:advanced',
    -- Audit
    'audit:read',
    -- Merchant ledger view
    'merchant:ledger'
);

-- MERCHANT_MANAGER: Merchant management and limited financial ops
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MERCHANT_MANAGER'
AND p.code IN (
    -- Payout (read only)
    'payout:read',
    -- Ledger (read only)
    'ledger:read',
    -- Dashboard
    'dashboard:view',
    -- Merchant specific
    'merchant:approve', 'merchant:ledger'
);

-- OPERATOR: Basic operational access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'OPERATOR'
AND p.code IN (
    -- Payout (read + create)
    'payout:create', 'payout:read',
    -- Ledger (read only)
    'ledger:read',
    -- Dashboard
    'dashboard:view'
);

-- VIEWER: Read-only access to all resources
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
AND p.code IN (
    -- All read permissions
    'payout:read',
    'ledger:read',
    'dashboard:view',
    'audit:read',
    'merchant:ledger'
);
