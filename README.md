# Cadoobi Transactions - Internal Payment Gateway

Internal payment gateway service that replaces Symmetry (external Core Banking partner) with direct communication to PSPs and operator APIs.

## Overview

`cadoobi-transactions` is a Spring Boot microservice that handles:
- Payment orchestration with multiple operators (Wave, Orange Money, Expresso, Free Money, banks, etc.)
- QR code and voucher generation
- Gift card lifecycle management
- Redemptions and merchant cash-in operations
- Outbound webhooks with retry mechanism

## Tech Stack

- **Java**: 21
- **Spring Boot**: 4.0.5
- **Database**: PostgreSQL
- **Build Tool**: Gradle
- **Migration**: Flyway
- **QR Code**: ZXing 3.5.3
- **Utilities**: Lombok

## Architecture

```
Client → Cadoobi (backend + BO)
              ↓  REST API calls
    cadoobi-transactions  ←→  Wave API / OM API / Expresso API / ...
              ↓  Outbound webhooks
         Cadoobi (backend)
```

## Quick Start

### Option 1: Docker (Recommended)

The fastest way to get started:

```bash
# Start all services (PostgreSQL + Application)
docker-compose up -d

# View logs
docker-compose logs -f

# Test the API
curl http://localhost:8081/operators
```

Services available:
- **API**: http://localhost:8081
- **PgAdmin**: http://localhost:5050 (admin@cadoobi.com / admin123)

See [DOCKER_GUIDE.md](DOCKER_GUIDE.md) for complete Docker documentation.

### Option 2: Local Development

#### Prerequisites
- Java 21
- PostgreSQL 16
- Gradle 8.5+

#### Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE cadoobi_transactions;
```

Update `src/main/resources/application.yaml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cadoobi_transactions
    username: your_username
    password: your_password
```

Flyway will automatically run migrations on startup.

#### Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The service will start on port `8081` by default.

## API Endpoints

### Payment Operations

#### Initiate Payment
```http
POST /payments
Content-Type: application/json

{
  "reference": "ORDER-123",
  "merchantId": "merchant-uuid",
  "merchantCode": "MARAZ",
  "operatorCode": "WAVE",
  "amount": 10000,
  "currency": "XOF",
  "payerPhone": "221771234567",
  "payerFullName": "John Doe",
  "recipientPhone": "221779876543",
  "recipientName": "Store Merchant",
  "callbackUrl": "https://cadoobi.com/webhooks/payment"
}
```

#### Get Payment Status
```http
GET /payments/{reference}
```

#### Operator Callback (Dynamic)
```http
POST /payments/callbacks/{operatorCode}
Content-Type: application/json

{
  "operatorTransactionId": "WV-12345",
  "status": "COMPLETED"
}
```

### Gift Card Operations

#### Check Balance
```http
GET /cards/{cardCode}/balance
```

#### Redeem Gift Card
```http
POST /cards/{cardCode}/redeem
Content-Type: application/json

{
  "merchantId": "merchant-uuid",
  "amountToRedeem": 5000,
  "idempotencyKey": "unique-key-123"
}
```

### Operator Management (Back Office)

#### List Active Operators
```http
GET /operators
```

#### Create Operator
```http
POST /operators
Content-Type: application/json

{
  "code": "EXPRESSO",
  "name": "Expresso Money",
  "country": "SN",
  "supportsPayin": true,
  "supportsPayout": true,
  "apiBaseUrl": "https://api.expresso.sn",
  "isActive": true
}
```

#### Get Operator Fees
```http
GET /operators/{id}/fees
```

#### Create Operator Fee
```http
POST /operators/{id}/fees
Content-Type: application/json

{
  "operationType": "PAYIN",
  "feeType": "PERCENTAGE",
  "feePercentage": 0.01,
  "minAmount": 0,
  "currency": "XOF",
  "isActive": true,
  "effectiveFrom": "2026-01-01"
}
```

## Key Features

### Dynamic Operator Registry

Operators are stored in the database, not hardcoded as enums. Adding new operators (Expresso, Free Money, banks) requires only a database row, not code changes.

### Fee Calculation

Supports three fee types:
- **PERCENTAGE**: e.g., 1% of amount
- **FIXED**: e.g., 100 XOF flat fee
- **MIXED**: combination of percentage + fixed

Fee calculation logic:
```
computed_fee = 0
if PERCENTAGE or MIXED: computed_fee += amount × fee_percentage
if FIXED or MIXED:      computed_fee += fee_fixed
```

### Gift Card Generation

When a payment is COMPLETED:
1. Unique card code generated (12 alphanumeric characters)
2. QR code created using ZXing library (300x300 PNG, Base64 encoded)
3. Gift card stored with initial balance = payment net_amount

### Redemption & Payout

When a merchant redeems a gift card:
1. Balance is decremented
2. Card status updated (PARTIALLY_USED, FULLY_USED)
3. Payout transaction created for merchant compensation
4. Payout fee applied based on operator configuration

### Outbound Notifications

Webhook retry mechanism with exponential backoff:
- **Delays**: 5s, 30s, 2m, 10m, 1h
- **Max Attempts**: 5
- **Scheduled Job**: Runs every 10 seconds to retry pending notifications

Event types:
- `PAYMENT_COMPLETED`
- `PAYMENT_FAILED`
- `CARD_REDEEMED`
- `CASHIN_COMPLETED`
- `CARD_EXPIRED`

## Database Schema

8 main tables:
1. **operators** - Dynamic operator registry
2. **operator_fees** - Fee configuration per operator × operation type
3. **payment_transactions** - PAYIN transactions
4. **operator_callbacks** - Raw callback audit trail
5. **gift_cards** - Generated vouchers with QR codes
6. **gift_card_redemptions** - Cramage events
7. **payout_transactions** - Merchant CashIn
8. **outbound_notifications** - Webhooks to Cadoobi with retry

## Migration from Symmetry

For existing Cadoobi project integration:

1. Remove Symmetry columns from entities
2. Add gateway reference columns
3. Replace `SymmetryClient` with `GatewayClient`
4. Point to `cadoobi-transactions` service at `http://localhost:8081`

See the plan document for detailed migration steps.

## Development

### Project Structure

```
src/main/java/sn/symmetry/cadoobi/
├── config/           # Application configuration
├── controller/       # REST API endpoints
├── domain/
│   ├── entity/      # JPA entities
│   └── enums/       # Domain enums
├── dto/             # Request/Response DTOs
├── exception/       # Custom exceptions
├── repository/      # JPA repositories
└── service/         # Business logic
```

### Running Tests

```bash
./gradlew test
```

## Health Check

The service exposes health endpoints via Spring Boot Actuator:

```bash
# Health check
curl http://localhost:8081/actuator/health

# Application info
curl http://localhost:8081/actuator/info

# Metrics
curl http://localhost:8081/actuator/metrics
```

## Logging

Logging levels can be configured in `application.yaml`:

```yaml
logging:
  level:
    root: INFO
    sn.symmetry.cadoobi: DEBUG
```

## Production Considerations

1. **Security**: Add authentication/authorization (Spring Security, JWT)
2. **Rate Limiting**: Implement rate limiting for public endpoints
3. **Monitoring**: Add Prometheus/Grafana for metrics
4. **API Documentation**: Add Springdoc OpenAPI for Swagger UI
5. **Database**: Configure connection pooling (HikariCP already included)
6. **Operator Integration**: Implement actual operator API clients
7. **Webhooks**: Add signature verification for callback security

## License

Internal use only - Cadoobi
