# Plan: cadoobi-transactions — Internal Payment Gateway

## Context

Replace Symmetry (external Core Banking partner) with an internal gateway service `cadoobi-transactions` that communicates directly with PSPs and operator APIs.

From the architecture diagrams, the **Payment Gateway** is the central hub between:
- **Left**: Merchant brands (Maraz, Orca, Skin Biology) on Site Cadoobi
- **Right**: Merchant back-office accounts managed by BO Symmetry (now internalized)

`cadoobi-transactions` takes over: payment orchestration, QR/voucher generation, gift card lifecycle, redemptions, and merchant CashIn — for **any number of operators** (Wave, Orange Money, Expresso, Free Money, banks, etc.).

---

## Architecture

```
Client → Cadoobi (backend + BO)
              ↓  REST API calls
    cadoobi-transactions  ←→  Wave API / OM API / Expresso API / ...
              ↓  Outbound webhooks
         Cadoobi (backend)
```

`cadoobi-transactions` is a **separate Spring Boot service** with its own PostgreSQL database.

---

## New Service: cadoobi-transactions

**Location:** `/home/mamadou-abass-diallo-sre/Freelance/Cadoobi/cadoobi-transactions/`
**Tech stack:** Spring Boot 4.x · Java 21 · Gradle · PostgreSQL · Flyway · Lombok
**Extra dependency:** ZXing (QR code generation)

---

## Entities & Flyway Migrations

### 1. `Operator` → `V1__create_operators.sql`
Dynamic operator registry — no hardcoded enums for operator list. New operators (Expresso, Free Money, banks…) are added as rows, not code changes.

| Field | Type | Constraint | Notes |
|---|---|---|---|
| `id` | UUID | PK | Auto-generated |
| `code` | VARCHAR(30) | UNIQUE NOT NULL | e.g. "WAVE", "ORANGE_MONEY", "EXPRESSO", "FREE_MONEY" |
| `name` | VARCHAR(100) | NOT NULL | e.g. "Wave", "Orange Money" |
| `country` | VARCHAR(2) | NOT NULL | "SN", "CI", etc. |
| `supports_payin` | BOOLEAN | NOT NULL DEFAULT TRUE | Can receive payments |
| `supports_payout` | BOOLEAN | NOT NULL DEFAULT TRUE | Can send payouts |
| `api_base_url` | VARCHAR(500) | | Integration URL |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

Seed data: WAVE, ORANGE_MONEY (at minimum) inserted in a `V1_1__seed_operators.sql`.

---

### 2. `OperatorFee` → `V2__create_operator_fees.sql`
Fee configuration per operator × operation type. Supports percentage, fixed, or mixed fee structures.

| Field | Type | Constraint | Notes |
|---|---|---|---|
| `id` | UUID | PK | |
| `operator_id` | UUID | FK NOT NULL → operators | |
| `operation_type` | VARCHAR(10) | NOT NULL | **PAYIN** \| **PAYOUT** |
| `fee_type` | VARCHAR(15) | NOT NULL | PERCENTAGE \| FIXED \| MIXED |
| `fee_percentage` | NUMERIC(6,4) | | e.g. 0.0100 = 1% — required if PERCENTAGE or MIXED |
| `fee_fixed` | NUMERIC(15,2) | | e.g. 100 XOF — required if FIXED or MIXED |
| `min_amount` | NUMERIC(15,2) | NOT NULL DEFAULT 0 | Minimum transaction amount this fee applies to |
| `max_amount` | NUMERIC(15,2) | | NULL = no upper limit |
| `currency` | VARCHAR(3) | NOT NULL DEFAULT 'XOF' | |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | |
| `effective_from` | DATE | NOT NULL | When this fee takes effect |
| `effective_to` | DATE | | NULL = still active |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

Unique constraint: `(operator_id, operation_type, effective_from)` — no two active fee rules for same operator+type on same date.

**Fee calculation logic (in service):**
```
computed_fee = 0
if PERCENTAGE or MIXED: computed_fee += amount × fee_percentage
if FIXED or MIXED:      computed_fee += fee_fixed
```

---

### 3. `PaymentTransaction` → `V3__create_payment_transactions.sql`
Tracks each PAYIN attempt with an operator.

| Field | Type | Constraint | Notes |
|---|---|---|---|
| `id` | UUID | PK | |
| `reference` | VARCHAR(100) | UNIQUE NOT NULL | Cadoobi order ID (idempotency key) |
| `merchant_id` | VARCHAR(36) | NOT NULL | Cadoobi merchant UUID |
| `merchant_code` | VARCHAR(10) | NOT NULL | |
| `operator_id` | UUID | FK NOT NULL → operators | Dynamic — no hardcoded enum |
| `amount` | NUMERIC(15,2) | NOT NULL | Gross amount from client |
| `fee_amount` | NUMERIC(15,2) | NOT NULL DEFAULT 0 | Computed fee at transaction time (snapshot) |
| `net_amount` | NUMERIC(15,2) | NOT NULL | amount - fee_amount |
| `currency` | VARCHAR(3) | NOT NULL DEFAULT 'XOF' | |
| `payer_phone` | VARCHAR(20) | NOT NULL | |
| `payer_full_name` | VARCHAR(150) | | |
| `recipient_phone` | VARCHAR(20) | NOT NULL | Beneficiary |
| `recipient_name` | VARCHAR(150) | | |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'INITIATED' | INITIATED \| PENDING \| COMPLETED \| FAILED \| EXPIRED \| CANCELLED |
| `operator_transaction_id` | VARCHAR(100) | UNIQUE | ID from Wave/OM/etc. |
| `payment_url` | VARCHAR(1000) | | Redirect URL for client |
| `callback_url` | VARCHAR(500) | NOT NULL | Cadoobi's notification URL |
| `expires_at` | TIMESTAMPTZ | | |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

Indexes: `status`, `merchant_id`, `operator_id`, `operator_transaction_id`

---

### 4. `OperatorCallback` → `V4__create_operator_callbacks.sql`
Raw callback payloads received from any operator (audit + idempotency).

| Field | Type | Constraint | Notes |
|---|---|---|---|
| `id` | UUID | PK | |
| `payment_transaction_id` | UUID | FK → payment_transactions | Nullable (some callbacks arrive before we match them) |
| `operator_id` | UUID | FK NOT NULL → operators | |
| `operator_reference` | VARCHAR(100) | UNIQUE NOT NULL | Operator's own callback/event ID |
| `raw_payload` | TEXT | NOT NULL | Full raw JSON from operator |
| `operator_status` | VARCHAR(50) | | Operator's status string |
| `processed_at` | TIMESTAMPTZ | | |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

---

### 5. `GiftCard` → `V5__create_gift_cards.sql`
Created after a `PaymentTransaction` reaches COMPLETED. cadoobi-transactions generates the code and QR.

| Field | Type | Constraint | Notes |
|---|---|---|---|
| `id` | UUID | PK | |
| `payment_transaction_id` | UUID | UNIQUE FK NOT NULL | One-to-one with payment |
| `merchant_id` | VARCHAR(36) | NOT NULL | |
| `card_code` | VARCHAR(50) | UNIQUE NOT NULL | Text voucher "XYZ1234BON" — generated internally |
| `qr_code_data` | TEXT | | Base64 PNG — generated via ZXing |
| `initial_amount` | NUMERIC(15,2) | NOT NULL | |
| `balance` | NUMERIC(15,2) | NOT NULL | Decremented on each redemption |
| `currency` | VARCHAR(3) | NOT NULL DEFAULT 'XOF' | |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'ACTIVE' | ACTIVE \| PARTIALLY_USED \| FULLY_USED \| EXPIRED \| BLOCKED |
| `expires_at` | TIMESTAMPTZ | | |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

Indexes: `card_code`, `merchant_id`, `status`

---

### 6. `GiftCardRedemption` → `V6__create_gift_card_redemptions.sql`
Each cramage event by a merchant.

| Field | Type | Constraint | Notes |
|---|---|---|---|
| `id` | UUID | PK | |
| `gift_card_id` | UUID | FK NOT NULL | |
| `merchant_id` | VARCHAR(36) | NOT NULL | |
| `idempotency_key` | VARCHAR(255) | UNIQUE NOT NULL | Prevents double-cramage |
| `amount_redeemed` | NUMERIC(15,2) | NOT NULL | |
| `remaining_balance` | NUMERIC(15,2) | | Balance after this redemption |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING \| COMPLETED \| FAILED |
| `redeemed_at` | TIMESTAMPTZ | | |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

---

### 7. `PayoutTransaction` → `V7__create_payout_transactions.sql`
CashIn to merchant wallet after a redemption. Uses the operator's PAYOUT fee config.

| Field | Type | Constraint | Notes |
|---|---|---|---|
| `id` | UUID | PK | |
| `redemption_id` | UUID | UNIQUE FK NOT NULL | One-to-one with redemption |
| `merchant_id` | VARCHAR(36) | NOT NULL | |
| `operator_id` | UUID | FK NOT NULL → operators | Merchant's compensation operator |
| `recipient_number` | VARCHAR(255) | NOT NULL | Wallet or bank account |
| `amount` | NUMERIC(15,2) | NOT NULL | Gross payout |
| `fee_amount` | NUMERIC(15,2) | NOT NULL DEFAULT 0 | Snapshot of fee at time of payout |
| `net_amount` | NUMERIC(15,2) | NOT NULL | amount - fee_amount |
| `currency` | VARCHAR(3) | NOT NULL DEFAULT 'XOF' | |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING \| COMPLETED \| FAILED |
| `idempotency_key` | VARCHAR(255) | UNIQUE NOT NULL | |
| `operator_transaction_id` | VARCHAR(100) | | From Wave/OM confirmation |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

Indexes: `merchant_id`, `status`, `operator_id`

---

### 8. `OutboundNotification` → `V8__create_outbound_notifications.sql`
Events pushed back to Cadoobi (replaces Symmetry webhooks). Has built-in retry with exponential backoff.

| Field | Type | Constraint | Notes |
|---|---|---|---|
| `id` | UUID | PK | |
| `event_id` | VARCHAR(100) | UNIQUE NOT NULL | Unique event ID for Cadoobi idempotency |
| `event_type` | VARCHAR(50) | NOT NULL | PAYMENT_COMPLETED \| PAYMENT_FAILED \| CARD_REDEEMED \| CASHIN_COMPLETED \| CARD_EXPIRED |
| `target_url` | VARCHAR(500) | NOT NULL | Cadoobi webhook endpoint |
| `payload` | TEXT | NOT NULL | JSON body |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING \| SENT \| FAILED |
| `attempts` | INT | NOT NULL DEFAULT 0 | |
| `next_retry_at` | TIMESTAMPTZ | | Backoff schedule: 5s, 30s, 2m, 10m, 1h |
| `last_attempt_at` | TIMESTAMPTZ | | |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

Indexes: `status`, `next_retry_at`

---

## Enums (cadoobi-transactions — only stable domain values)

| Enum | Values | Notes |
|---|---|---|
| `OperationType` | PAYIN, PAYOUT | Used in OperatorFee |
| `FeeType` | PERCENTAGE, FIXED, MIXED | |
| `PaymentStatus` | INITIATED, PENDING, COMPLETED, FAILED, EXPIRED, CANCELLED | |
| `CardStatus` | ACTIVE, PARTIALLY_USED, FULLY_USED, EXPIRED, BLOCKED | |
| `RedemptionStatus` | PENDING, COMPLETED, FAILED | |
| `PayoutStatus` | PENDING, COMPLETED, FAILED | |
| `NotificationEventType` | PAYMENT_COMPLETED, PAYMENT_FAILED, CARD_REDEEMED, CASHIN_COMPLETED, CARD_EXPIRED | |
| `NotificationStatus` | PENDING, SENT, FAILED | |

> **Note:** Operator names (WAVE, ORANGE_MONEY, EXPRESSO…) are stored as `code VARCHAR(30)` in the `operators` table — **not an enum** — so new operators need only a DB row, not a code deployment.

---

## API Endpoints exposed by cadoobi-transactions

| Method | URL | Caller | Purpose |
|---|---|---|---|
| POST | `/payments` | Cadoobi backend | Initiate PAYIN (any operator) |
| GET | `/payments/{reference}` | Cadoobi backend | Poll payment status |
| POST | `/payments/callbacks/{operatorCode}` | Operators | Dynamic callback per operator |
| GET | `/cards/{cardCode}/balance` | Cadoobi backend | Real-time balance check |
| POST | `/cards/{cardCode}/redeem` | Cadoobi backend | Cramage by merchant |
| GET | `/operators` | Cadoobi BO | List active operators |
| POST | `/operators` | Cadoobi BO | Add new operator |
| GET | `/operators/{id}/fees` | Cadoobi BO | Get fees for operator |
| POST | `/operators/{id}/fees` | Cadoobi BO | Create/update fee config |

---

## Modifications to existing `cadoobi` project

### Entity field changes
- **`Merchant`** — remove `symmetryMerchantId`, `agencyCode`
- **`Purchase`** — remove `symmetryPurchaseId`; add `gatewayPaymentId VARCHAR(100)`
- **`GiftCard`** — remove `symmetryCardId`, `symmetryTransactionId`; add `gatewayCardId VARCHAR(100)`
- **`Redemption`** — remove `symmetryTransactionId`; add `gatewayRedemptionId VARCHAR(100)`
- **`CashIn`** — remove `symmetryTransactionId`; add `gatewayPayoutId VARCHAR(100)`
- **`WebhookEvent`** — rename column `symmetry_event_id` → `gateway_event_id`

### New Flyway migration in `cadoobi`
`V7__migrate_symmetry_to_gateway.sql` — drop Symmetry columns, add gateway reference columns, rename webhook column

### Replace `SymmetryClient` with `GatewayClient`
Points to `cadoobi-transactions` internal REST API.

---

## Critical Files

### New (cadoobi-transactions)
```
cadoobi-transactions/
├── build.gradle
├── src/main/resources/application.properties
├── src/main/java/.../domain/entity/
│   ├── BaseEntity.java
│   ├── Operator.java
│   ├── OperatorFee.java
│   ├── PaymentTransaction.java
│   ├── OperatorCallback.java
│   ├── GiftCard.java
│   ├── GiftCardRedemption.java
│   ├── PayoutTransaction.java
│   └── OutboundNotification.java
├── src/main/java/.../domain/enums/  (8 enums)
├── src/main/java/.../repository/    (8 repositories)
├── src/main/java/.../config/JpaConfig.java
└── src/main/resources/db/migration/ (V1–V8 + V1_1 seed)
```

### Modified (cadoobi)
```
cadoobi/src/main/java/.../domain/entity/  (6 entities updated)
cadoobi/src/main/resources/db/migration/V7__migrate_symmetry_to_gateway.sql
cadoobi/src/main/java/.../client/GatewayClient.java  (replaces SymmetryClient)
```

---

## Verification

1. Create two DBs: `cadoobi` and `cadoobi_transactions`
2. Start `cadoobi-transactions` → Flyway V1–V8 run, 8 tables created, operators seeded
3. Call `GET /operators` → returns WAVE, ORANGE_MONEY rows
4. Call `POST /operators/{id}/fees` to configure PAYIN fee for WAVE (e.g. 1%)
5. Call `POST /payments` with Wave → fee computed, `PaymentTransaction` created (INITIATED)
6. Simulate operator callback `POST /payments/callbacks/WAVE` → status → COMPLETED, `GiftCard` created with `cardCode` + `qrCodeData`
7. Call `POST /cards/{cardCode}/redeem` → `GiftCardRedemption` + `PayoutTransaction` created (PAYOUT fee applied)
8. `OutboundNotification` fired → Cadoobi webhook receives `CASHIN_COMPLETED` with `gateway_event_id`
9. Start `cadoobi` → Flyway V7 runs, Symmetry columns dropped, gateway columns added
