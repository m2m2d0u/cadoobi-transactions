INSERT INTO operators (id, code, name, country, supports_payin, supports_payout, api_base_url, is_active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'WAVE', 'Wave', 'SN', TRUE, TRUE, 'https://api.wave.com', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'ORANGE_MONEY', 'Orange Money', 'SN', TRUE, TRUE, 'https://api.orange.sn', TRUE, NOW(), NOW());
