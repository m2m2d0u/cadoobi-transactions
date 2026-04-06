-- Fix V21 migration bug: Remove incorrectly placed constraint from outbound_notifications
-- and add it back to the correct table (payout_transactions)

-- Remove the misplaced constraint from outbound_notifications
ALTER TABLE outbound_notifications
    DROP CONSTRAINT IF EXISTS payout_transactions_status_check;

-- Add the constraint to the correct table (payout_transactions)
ALTER TABLE payout_transactions
    DROP CONSTRAINT IF EXISTS payout_transactions_status_check;

ALTER TABLE payout_transactions
    ADD CONSTRAINT payout_transactions_status_check CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED'));
