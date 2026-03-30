-- Link each merchant to a managing user (ManyToOne: many merchants → one user)
ALTER TABLE merchants
    ADD COLUMN user_id UUID REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_merchant_user_id ON merchants(user_id);
