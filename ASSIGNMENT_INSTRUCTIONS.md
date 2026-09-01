# Transaction Starter Project

This is the starter project for the Customer Transactions exercise.

## Before you start

The first thing you should do after cloning the repository is:

### Linux / macOS

```bash
./mvnw clean test
```

### Windows

```bat
mvnw.cmd clean test
```

The sample test should pass before you begin implementing the exercise.

## What is already provided

- Java 17
- Spring Boot
- Maven wrapper
- Spring Web
- Spring Data JPA
- H2 embedded database
- JUnit / Spring Boot Test
- A sample REST endpoint: `GET /api/sample`
- A sample test that loads the Spring context


## Exercise

Implement these four operations:

1. Create transaction
2. Get transaction
3. Update transaction status
4. Get all transactions for a customer


You may change the surrounding design if you believe your solution is better.

## Transaction fields

Every transaction contains:

- Transaction ID
- Customer ID
- Amount
- Currency
- Transaction Type
- Transaction Status

### Validation rules

Define what makes a transaction valid. At minimum, consider:

- Transaction ID
- Customer ID
- Amount
- Currency
- Transaction type
- Initial status

Also explain any business validation you add beyond the annotations already supplied:

- **Transaction ID**: Client may supply a unique ID; if omitted, system auto-generates `TXN-<uuid>`. If client supplies an existing ID, returns `409 Conflict`.
- **Customer ID**: Mandatory (`@NotBlank`).
- **Amount**: Mandatory positive number (`@NotNull`, `@Positive`).
- **Currency**: Mandatory 3-letter ISO-4217 uppercase code (`@Pattern(regexp = "^[A-Z]{3}$")`, e.g., `USD`, `EUR`, `GBP`).
- **Transaction Type**: Valid enum (`PAYMENT`, `REFUND`, `TRANSFER`, `DEPOSIT`, `WITHDRAWAL`).
- **Initial Status**: Defaults to `PENDING` if omitted.
- **Status State Machine**:
  - `PENDING` $\rightarrow$ `PROCESSING`, `COMPLETED`, `FAILED`, `CANCELLED`
  - `PROCESSING` $\rightarrow$ `COMPLETED`, `FAILED`, `CANCELLED`
  - Terminal states (`COMPLETED`, `FAILED`, `CANCELLED`) cannot transition to any other status. Violation returns `400 Bad Request`.

## API Documentation

### Create Transaction

`POST /api/transactions`

Request:
```json
{
  "transactionId": "TXN-1001",
  "customerId": "CUST-001",
  "amount": 150.00,
  "currency": "USD",
  "type": "PAYMENT",
  "status": "PENDING"
}
```

Response (`201 Created`):
```json
{
  "transactionId": "TXN-1001",
  "customerId": "CUST-001",
  "amount": 150.00,
  "currency": "USD",
  "type": "PAYMENT",
  "status": "PENDING",
  "createdAt": "2026-08-27T22:00:00Z",
  "updatedAt": "2026-08-27T22:00:00Z"
}
```

### Get Transaction

`GET /api/transactions/{id}`

Response (`200 OK`):
```json
{
  "transactionId": "TXN-1001",
  "customerId": "CUST-001",
  "amount": 150.00,
  "currency": "USD",
  "type": "PAYMENT",
  "status": "PENDING",
  "createdAt": "2026-08-27T22:00:00Z",
  "updatedAt": "2026-08-27T22:00:00Z"
}
```

### Update Status

`PATCH /api/transactions/{id}/status` or `PUT /api/transactions/{id}/status`

Request:
```json
{
  "status": "COMPLETED",
  "reason": "Payment settled"
}
```

Response (`200 OK`):
```json
{
  "transactionId": "TXN-1001",
  "customerId": "CUST-001",
  "amount": 150.00,
  "currency": "USD",
  "type": "PAYMENT",
  "status": "COMPLETED",
  "createdAt": "2026-08-27T22:00:00Z",
  "updatedAt": "2026-08-27T22:05:00Z"
}
```

### Get Customer Transactions

`GET /api/customers/{customerId}/transactions` (or `GET /api/transactions?customerId={customerId}`)

Response (`200 OK`):
```json
[
  {
    "transactionId": "TXN-1001",
    "customerId": "CUST-001",
    "amount": 150.00,
    "currency": "USD",
    "type": "PAYMENT",
    "status": "COMPLETED",
    "createdAt": "2026-08-27T22:00:00Z",
    "updatedAt": "2026-08-27T22:05:00Z"
  }
]
```

## Testing expectations

All test requirements are covered by unit and integration tests:
- `TransactionStatusTest`: Tests state machine transitions.
- `TransactionServiceTest`: Tests service layer business rules, duplicate detection, auto-generation, and error handling.
- `TransactionControllerTest`: Full `MockMvc` integration tests for controller endpoints and validation.


