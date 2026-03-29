-- Create permissions table
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    resource VARCHAR(50),
    action VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permission_code ON permissions(code);
CREATE INDEX idx_permission_resource ON permissions(resource);
CREATE INDEX idx_permission_active ON permissions(is_active);

-- Create roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_system_role BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_role_code ON roles(code);
CREATE INDEX idx_role_active ON roles(is_active);
CREATE INDEX idx_role_system ON roles(is_system_role);

-- Create role_permissions junction table
CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    email_verified BOOLEAN NOT NULL DEFAULT false,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    reset_token VARCHAR(255),
    reset_token_expires_at TIMESTAMP,
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_reset_token ON users(reset_token);

-- Create user_roles junction table
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- Insert default permissions (CRUD for all resources)
INSERT INTO permissions (code, name, description, resource, action) VALUES
-- Payment permissions
('payment:create', 'Create Payment', 'Create new payment transactions', 'payment', 'create'),
('payment:read', 'Read Payment', 'View payment details', 'payment', 'read'),
('payment:update', 'Update Payment', 'Update payment status', 'payment', 'update'),
('payment:delete', 'Delete Payment', 'Delete payment records', 'payment', 'delete'),

-- Merchant permissions
('merchant:create', 'Create Merchant', 'Register new merchants', 'merchant', 'create'),
('merchant:read', 'Read Merchant', 'View merchant details', 'merchant', 'read'),
('merchant:update', 'Update Merchant', 'Update merchant information', 'merchant', 'update'),
('merchant:delete', 'Delete Merchant', 'Delete merchant accounts', 'merchant', 'delete'),

-- Operator permissions
('operator:create', 'Create Operator', 'Register new payment operators', 'operator', 'create'),
('operator:read', 'Read Operator', 'View operator details', 'operator', 'read'),
('operator:update', 'Update Operator', 'Update operator configuration', 'operator', 'update'),
('operator:delete', 'Delete Operator', 'Delete payment operators', 'operator', 'delete'),

-- Gift Card permissions
('gift_card:create', 'Create Gift Card', 'Generate new gift cards', 'gift_card', 'create'),
('gift_card:read', 'Read Gift Card', 'View gift card details', 'gift_card', 'read'),
('gift_card:update', 'Update Gift Card', 'Update gift card status', 'gift_card', 'update'),
('gift_card:redeem', 'Redeem Gift Card', 'Process gift card redemptions', 'gift_card', 'redeem'),

-- User management permissions
('user:create', 'Create User', 'Create new user accounts', 'user', 'create'),
('user:read', 'Read User', 'View user details', 'user', 'read'),
('user:update', 'Update User', 'Update user information', 'user', 'update'),
('user:delete', 'Delete User', 'Delete user accounts', 'user', 'delete'),

-- Role management permissions
('role:create', 'Create Role', 'Create new roles', 'role', 'create'),
('role:read', 'Read Role', 'View role details', 'role', 'read'),
('role:update', 'Update Role', 'Update role configuration', 'role', 'update'),
('role:delete', 'Delete Role', 'Delete roles', 'role', 'delete'),

-- Permission management
('permission:read', 'Read Permission', 'View permission details', 'permission', 'read'),

-- System permissions
('system:admin', 'System Administration', 'Full system access', 'system', 'admin'),
('reports:view', 'View Reports', 'Access system reports', 'reports', 'view');

-- Insert default roles
INSERT INTO roles (code, name, description, is_system_role) VALUES
('SUPER_ADMIN', 'Super Administrator', 'Full system access with all permissions', true),
('ADMIN', 'Administrator', 'Administrative access to manage users and configurations', true),
('FINANCE_MANAGER', 'Finance Manager', 'Manage payments, merchants, and financial operations', true),
('MERCHANT_MANAGER', 'Merchant Manager', 'Manage merchant accounts and basic operations', true),
('OPERATOR', 'Operator', 'Basic operational access', true),
('VIEWER', 'Viewer', 'Read-only access to system data', true);

-- Assign all permissions to SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN';

-- Assign permissions to ADMIN (all except system:admin)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
AND p.code != 'system:admin';

-- Assign permissions to FINANCE_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'FINANCE_MANAGER'
AND p.code IN (
    'payment:create', 'payment:read', 'payment:update',
    'merchant:read', 'merchant:update',
    'operator:read',
    'gift_card:read', 'gift_card:redeem',
    'reports:view'
);

-- Assign permissions to MERCHANT_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MERCHANT_MANAGER'
AND p.code IN (
    'payment:read',
    'merchant:create', 'merchant:read', 'merchant:update',
    'operator:read',
    'gift_card:read'
);

-- Assign permissions to OPERATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'OPERATOR'
AND p.code IN (
    'payment:create', 'payment:read',
    'merchant:read',
    'gift_card:read', 'gift_card:redeem'
);

-- Assign permissions to VIEWER (read-only)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
AND p.action = 'read';
