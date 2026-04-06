-- Update the event type of outbound check
ALTER TABLE outbound_notifications
    DROP CONSTRAINT outbound_notifications_event_type_check;

ALTER TABLE outbound_notifications
    ADD CONSTRAINT outbound_notifications_event_type_check CHECK (event_type IN ('PAYMENT_INITIATED', 'PAYMENT_PENDING',
                                                                                 'PAYMENT_COMPLETED', 'PAYMENT_FAILED',
                                                                                 'PAYMENT_CANCELLED', 'PAYMENT_EXPIRED',
                                                                                 'CARD_REDEEMED', 'CASHIN_COMPLETED',
                                                                                 'CARD_EXPIRED',
                                                                                 'PAYMENT_STATUS_UPDATE'));
