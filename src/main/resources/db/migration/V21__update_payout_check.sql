-- Update the status check of payout
ALTER TABLE payout_transactions
    DROP CONSTRAINT payout_transactions_status_check;

ALTER TABLE outbound_notifications
    ADD CONSTRAINT payout_transactions_status_check CHECK (status IN ('PENDING', 'PROCESSING',  'COMPLETED', 'FAILED'));
