CREATE TABLE merchants (
    id                       UUID PRIMARY KEY,
    code                     VARCHAR(10)  NOT NULL UNIQUE,
    name                     VARCHAR(100) NOT NULL,
    logo_url                 VARCHAR(500),
    phone                    VARCHAR(15)  NOT NULL,
    business_type            VARCHAR(100),
    email                    VARCHAR(100) NOT NULL,
    address                  VARCHAR(255),
    country                  VARCHAR(2)   NOT NULL,
    rccm                     VARCHAR(100),
    ninea                    VARCHAR(50),

    -- Owner / Manager
    owner_full_name          VARCHAR(150),
    owner_email              VARCHAR(100),
    owner_phone              VARCHAR(15),
    owner_cni                VARCHAR(50),

    -- Compensation account (embedded)
    comp_type                VARCHAR(20)  CHECK (comp_type IN ('BANK', 'OPERATOR')),

    -- Bank account fields
    comp_bank_name           VARCHAR(100),
    comp_account_number      VARCHAR(50),
    comp_account_holder      VARCHAR(150),
    comp_iban                VARCHAR(34),
    comp_swift               VARCHAR(11),

    -- Operator (mobile money) fields
    comp_operator_id         UUID REFERENCES operators(id),
    comp_operator_phone      VARCHAR(15),
    comp_operator_holder_name VARCHAR(150),

    -- Symmetry onboarding fields
    symmetry_merchant_id     VARCHAR(50) UNIQUE,
    agency_code              VARCHAR(50),
    status                   VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                 CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'BLOCKED')),

    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_merchant_code    ON merchants(code);
CREATE INDEX idx_merchant_status  ON merchants(status);
CREATE INDEX idx_merchant_country ON merchants(country);
