INSERT INTO operator_fees (id, operator_id, operation_type, fee_type, fee_percentage, fee_fixed, min_amount, max_amount, currency, is_active, effective_from, effective_to, created_at, updated_at)
SELECT
    gen_random_uuid(),
    o.id,
    ops.operation_type,
    'PERCENTAGE',
    0.0200,
    NULL,
    0,
    NULL,
    'XOF',
    TRUE,
    CURRENT_DATE,
    NULL,
    NOW(),
    NOW()
FROM operators o
CROSS JOIN (VALUES ('PAYIN'), ('PAYOUT')) AS ops(operation_type)
WHERE o.is_active = TRUE;
