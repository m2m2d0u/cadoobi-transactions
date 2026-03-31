-- ============================================================
-- V16: Remove gift card and redemption tables
-- Gift card management has been moved to the Cadoobi BO.
-- Payout transactions remain but the redemption_id FK is dropped.
-- ============================================================

-- Remove gift card permissions seeded in V11
DELETE FROM role_permissions rp
USING permissions p
WHERE rp.permission_id = p.id
  AND p.resource = 'gift_card';

DELETE FROM permissions WHERE resource = 'gift_card';

-- Drop redemption_id FK and column from payout_transactions
ALTER TABLE payout_transactions
    DROP CONSTRAINT IF EXISTS payout_transactions_redemption_id_fkey,
    DROP COLUMN IF EXISTS redemption_id;

-- Drop gift card tables (order matters due to FK constraints)
DROP TABLE IF EXISTS gift_card_redemptions;
DROP TABLE IF EXISTS gift_cards;
