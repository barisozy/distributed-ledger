# API Documentation

## Overview

The Distributed Ledger System exposes RESTful APIs for managing accounts and transactions.

**Base URL**: `http://localhost:8080`
**API Version**: v1
**Content-Type**: `application/json`

## Authentication

All API endpoints make use of **HTTP Basic Authentication**. You must include the `Authorization` header in your requests.

**Header**:
`Authorization: Basic <base64-encoded-credentials>`

Default credentials (configurable via environment variables):
- **Username**: `admin`
- **Password**: `password` (see `application.yml` or `SecurityConfig`)

---

## Endpoints

### 1. Health Check

Check the health status of the application.

**Endpoint**: `GET /api/v1/health`

**Note**: This endpoint requires authentication. For public health checks, use Actuator endpoint `/actuator/health` if enabled.

**Response**:
```json
{
  "status": "UP",
  "service": "distributed-ledger"
}
```

**Status Codes**:
- `200 OK`: Service is healthy
- `401 Unauthorized`: Invalid credentials

**Example**:
```bash
curl -u admin:password http://localhost:8080/api/v1/health
```

---

### 2. Send Money

Transfer money from one account to another.

**Endpoint**: `POST /api/v1/transactions/send`

**Request Body**:
```json
{
  "fromAccountId": "550e8400-e29b-41d4-a716-446655440000",
  "toAccountId": "550e8400-e29b-41d4-a716-446655440001",
  "amount": 100.00,
  "currency": "USD",
  "reference": "TXN-2025-001"
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| fromAccountId | UUID | Yes | Source account identifier |
| toAccountId | UUID | Yes | Target account identifier |
| amount | Decimal | Yes | Amount to transfer (must be positive, min 0.01) |
| currency | String | Yes | ISO 4217 currency code (e.g., USD, EUR) |
| reference | String | Yes | Unique transaction reference for idempotency |

**Success Response** (200 OK):
Returns HTTP 200 with an empty body upon successful transaction processing.

**Error Responses**:

#### Insufficient Funds (400 Bad Request)
```json
{
  "timestamp": "2025-12-19T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient funds. Balance: 50.00 USD, Attempted: 100.00 USD",
  "path": "/api/v1/transactions/send"
}
```

#### Account Not Found (404 Not Found)
```json
{
  "timestamp": "2025-12-19T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Account not found: 550e8400-e29b-41d4-a716-446655440000",
  "path": "/api/v1/transactions/send"
}
```

#### Duplicate Transaction Reference (409 Conflict)
```json
{
  "timestamp": "2025-12-19T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Transaction with reference 'TXN-2025-001' already exists",
  "path": "/api/v1/transactions/send"
}
```

#### Concurrent Update Conflict (409 Conflict - Auto-Retry)
```json
{
  "errorCode": "CONCURRENCY_ERROR",
  "message": "Transaction failed after 3 retry attempts due to concurrent updates",
  "timestamp": "2025-12-19T10:30:00Z"
}
```

**Status Codes**:
- `200 OK`: Transfer completed successfully
- `400 Bad Request`: Invalid request (insufficient funds, validation errors)
- `404 Not Found`: Account not found
- `409 Conflict`: Duplicate reference or concurrent update failure
- `500 Internal Server Error`: Unexpected server error

**Example - Successful Transfer**:
```bash
curl -X POST http://localhost:8080/api/v1/transactions/send \
  -u admin:password \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "550e8400-e29b-41d4-a716-446655440000",
    "toAccountId": "550e8400-e29b-41d4-a716-446655440001",
    "amount": 100.00,
    "currency": "USD",
    "reference": "TXN-2025-001"
  }'
```

**Example - Insufficient Funds**:
```bash
curl -X POST http://localhost:8080/api/v1/transactions/send \
  -u admin:password \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "550e8400-e29b-41d4-a716-446655440000",
    "toAccountId": "550e8400-e29b-41d4-a716-446655440001",
    "amount": 10000.00,
    "currency": "USD",
    "reference": "TXN-2025-002"
  }'
```

---

## Business Rules

### Money Transfer Rules

1. **Source Account Validation**:
   - Account must exist
   - Account must be ACTIVE (not SUSPENDED or CLOSED)
   - Account must have sufficient balance

2. **Target Account Validation**:
   - Account must exist
   - Account must be ACTIVE

3. **Amount Validation**:
   - Amount must be positive (> 0)
   - Currency must match both accounts

4. **Reference Uniqueness**:
   - Each transaction reference must be unique (idempotency)
   - Retrying with same reference returns original transaction result

5. **Atomicity**:
   - Either both debit and credit succeed, or neither occurs
   - Transaction is marked as COMPLETED only if both operations succeed
   - Failed transactions are marked as FAILED

6. **Concurrency**:
   - Optimistic locking prevents lost updates
   - Concurrent updates trigger automatic retry (max 3 attempts)
   - 100ms backoff between retries

---

## Transaction Lifecycle

```
1. PENDING → Transaction created
2. Validation → Check accounts, balance, reference
3. Debit → Withdraw from source account
4. Credit → Deposit to target account
5. COMPLETED → Transaction successful
   OR
5. FAILED → Transaction rolled back
```

---

## Error Handling

All errors follow a consistent structure:

```json
{
  "errorCode": "Error code string (e.g., VALIDATION_ERROR)",
  "message": "Detailed error message",
  "timestamp": "ISO-8601 timestamp"
}
```

### Common Error Scenarios

| Scenario | Status Code | Description |
|----------|-------------|-------------|
| Insufficient balance | 400 | Source account doesn't have enough funds |
| Invalid amount | 400 | Amount is zero or negative |
| Account not found | 404 | Account ID doesn't exist |
| Duplicate reference | 409 | Transaction reference already used |
| Concurrent update | 409 | Multiple requests updating same account |
| Account suspended | 400 | Account is not in ACTIVE status |
| Currency mismatch | 400 | Accounts have different currencies |

---

## Idempotency

The API supports idempotent operations using the `reference` field:

- **Same reference + same data** → Returns original transaction result (200 OK)
- **Same reference + different data** → Returns conflict error (409 Conflict)

**Example**:
```bash
# First request
curl -X POST http://localhost:8080/api/v1/transactions/send \
  -u admin:password \
  -d '{"reference": "TXN-001", "amount": 100, ...}'
# Returns: 200 OK with transaction ID

# Second request (same reference)
curl -X POST http://localhost:8080/api/v1/transactions/send \
  -u admin:password \
  -d '{"reference": "TXN-001", "amount": 100, ...}'
# Returns: 200 OK with same transaction ID (idempotent)

# Third request (same reference, different amount)
curl -X POST http://localhost:8080/api/v1/transactions/send \
  -u admin:password \
  -d '{"reference": "TXN-001", "amount": 200, ...}'
# Returns: 409 Conflict (reference already used)
```

---

## Rate Limiting

**Status**: Not implemented yet

**Planned**:
- 100 requests per minute per IP
- 429 Too Many Requests status code when exceeded

---

## Authentication & Authorization

The API is secured using HTTP Basic Authentication.
All endpoints (except `/api/v1/health` and Swagger UI) require valid credentials.

**Default Credentials**:
- Username: `admin`
- Password: `password`

**Example**:
```bash
curl -X POST http://localhost:8080/api/v1/transactions/send \
  -u admin:password \
  -H "Content-Type: application/json" \
  -d '{...}'
```

---

## Testing with Postman

### Collection Variables
```json
{
  "base_url": "http://localhost:8080",
  "api_version": "v1"
}
```

### Pre-request Script (Generate UUID)
```javascript
pm.environment.set("accountId1", pm.variables.replaceIn('{{$guid}}'));
pm.environment.set("accountId2", pm.variables.replaceIn('{{$guid}}'));
pm.environment.set("reference", "TXN-" + Date.now());
```

---

## Future Endpoints (Planned)

### Account Management
- `POST /api/v1/accounts` - Create new account
- `GET /api/v1/accounts/{id}` - Get account details
- `GET /api/v1/accounts/{id}/balance` - Get account balance
- `PUT /api/v1/accounts/{id}/status` - Update account status

### Transaction Queries
- `GET /api/v1/transactions/{id}` - Get transaction details
- `GET /api/v1/accounts/{id}/transactions` - Get account transaction history
- `GET /api/v1/transactions` - List all transactions (paginated)

### Direct Operations
- `POST /api/v1/money/deposit` - Deposit money to account
- `POST /api/v1/money/withdraw` - Withdraw money from account

---

## WebSocket Support (Future)

Real-time notifications for:
- Transaction status updates
- Account balance changes
- Failed transaction alerts

**Endpoint**: `ws://localhost:8080/ws/notifications`

---

## API Versioning

- Current version: **v1**
- Versioning strategy: URI path versioning (`/api/v1/`, `/api/v2/`)
- Breaking changes will result in new version
- Old versions supported for 6 months after new version release

---

## Support

For API issues or questions:
- GitHub Issues: [repository-url]/issues
- Documentation: This file and README.md
- Architecture Decisions: `docs/adr/` directory

