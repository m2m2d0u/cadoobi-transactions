-- Create a default admin user if one doesn't exist
INSERT INTO users (email, password_hash, full_name, status, email_verified)
VALUES (
    'admin@cadoobi.sn',
    '$2a$10$fWJp1cO5h5R8E.Uq9/N8pODWpU/Iq5K29x3S7v9uEIn.8gS/t8/W6', -- 'password' encoded with BCrypt
    'System Administrator',
    'ACTIVE',
    true
)
ON CONFLICT (email) DO NOTHING;

-- Link default admin user to the SUPER_ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email = 'admin@cadoobi.sn' AND r.code = 'SUPER_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);
