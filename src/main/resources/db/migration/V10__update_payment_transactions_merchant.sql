-- Replace the free-text merchant_id / merchant_code columns with a proper FK to merchants.

-- 1. Drop old columns and their index
DROP INDEX IF EXISTS idx_payment_merchant;
ALTER TABLE payment_transactions DROP COLUMN IF EXISTS merchant_id;
ALTER TABLE payment_transactions DROP COLUMN IF EXISTS merchant_code;

-- 2. Add typed FK column
ALTER TABLE payment_transactions
    ADD COLUMN merchant_id UUID NOT NULL REFERENCES merchants(id);

-- 3. Re-create index on the new FK column
CREATE INDEX idx_payment_merchant ON payment_transactions(merchant_id);
