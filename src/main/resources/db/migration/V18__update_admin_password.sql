-- Update admin user password hash to correct BCrypt encoding
UPDATE users
SET password_hash = '$2a$10$EekAt/Jw0KRXOjuDtjIsx.JEXhLfyvnnXWCKuUrryqWDFNcB7tGUW' -- 'password' encoded with BCrypt
WHERE email = 'admin@cadoobi.sn';
