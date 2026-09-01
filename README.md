# 💳 Customer Transaction Management System

**Author:** Thotapalli Chaitanya Kumar Reddy  
**Technology Stack:** Java 17+, Spring Boot 3.5.5, Spring Data JPA, H2 Database, Maven, JUnit 5, Mockito  

---

## 📌 Project Overview

This repository contains a robust, enterprise-ready **Customer Transaction Management Service** built with **Spring Boot**. The service enables financial transaction lifecycle processing, validation, state machine transition enforcement, and querying transactions by customer ID or transaction ID.

---

## ✨ Key Features

- **Transaction Lifecycle & State Machine**: Strictly enforces status state transitions (`PENDING` ➔ `PROCESSING` ➔ `COMPLETED` / `FAILED` / `CANCELLED`). Terminal states cannot be altered.
- **Automated & Custom ID Generation**: Client can supply a custom transaction ID or let the system auto-generate a unique `TXN-<uuid>`.
- **Validation & Integrity**: Enforces mandatory fields, positive transaction amounts, 3-letter uppercase ISO-4217 currency codes, and transaction type enums.
- **Global Error Handling**: Standardized error response structure returning clean HTTP status codes (`400 Bad Request`, `404 Not Found`, `409 Conflict`).
- **Embedded Database & UI**: Pre-configured H2 in-memory database (`/h2-console`) and an interactive Web Dashboard (`/index.html`).
- **Comprehensive Testing**: 100% passing test coverage (20 unit and integration tests covering models, services, controllers, and edge cases).

---

## 📁 Complete Project Structure

```
toucan/
├── .gitignore                          # Git ignore rules for Java, IDEs, and build artifacts
├── README.md                           # Main repository documentation
└── Transaction-assignment/             # Spring Boot Application Root
    ├── pom.xml                         # Maven dependencies & project build configuration
    ├── mvnw / mvnw.cmd                 # Cross-platform Maven wrappers
    ├── README.md                       # Assignment specifications & guidelines
    ├── STUDENT_CHECKLIST.md            # Completed assignment requirements checklist
    └── src/
        ├── main/
        │   ├── java/com/example/transactionstarter/
        │   │   ├── TransactionStarterApplication.java   # Spring Boot Main Entry Point
        │   │   ├── controller/
        │   │   │   └── TransactionController.java      # REST API Endpoints Handler
        │   │   ├── dto/
        │   │   │   ├── CreateTransactionRequest.java   # Request payload for creating a transaction
        │   │   │   ├── UpdateTransactionStatusRequest.java # Request payload for status updates
        │   │   │   ├── TransactionResponse.java        # Standardized API response payload
        │   │   │   └── ErrorResponse.java              # Global error response payload format
        │   │   ├── exception/
        │   │   │   ├── GlobalExceptionHandler.java     # Controller Advice for global error handling
        │   │   │   ├── DuplicateTransactionException.java  # Exception thrown on 409 Conflict
        │   │   │   ├── InvalidStatusTransitionException.java # Exception thrown on invalid state change
        │   │   │   └── TransactionNotFoundException.java   # Exception thrown on 404 Not Found
        │   │   ├── model/
        │   │   │   ├── Transaction.java                # JPA Entity mapped to H2 database table
        │   │   │   ├── TransactionStatus.java          # State Machine Enum (PENDING, PROCESSING, etc.)
        │   │   │   └── TransactionType.java            # Enum (PAYMENT, REFUND, TRANSFER, etc.)
        │   │   ├── repository/
        │   │   │   └── TransactionRepository.java      # Spring Data JPA Repository
        │   │   └── service/
        │   │       └── TransactionService.java         # Core Business Logic & State Machine Service
        │   └── resources/
        │       ├── application.yml                 # Database, server, and H2 console settings
        │       └── static/
        │           └── index.html                  # Interactive Browser Web Dashboard
        └── test/
            └── java/com/example/transactionstarter/
                ├── TransactionStarterApplicationTests.java # Context loading test
                ├── controller/
                │   └── TransactionControllerTest.java    # MockMvc REST API integration tests
                ├── model/
                │   └── TransactionStatusTest.java        # Unit tests for state machine transitions
                └── service/
                    └── TransactionServiceTest.java       # Unit tests for business logic & errors
```

---

## 🔄 Transaction State Machine

Transactions adhere strictly to the following transition lifecycle:

```
[ PENDING ] ───► [ PROCESSING ] ───► [ COMPLETED ] (Terminal)
     │                 │
     ├───► [FAILED]    ├───► [FAILED] (Terminal)
     │                 │
     └───► [CANCELLED] └───► [CANCELLED] (Terminal)
```

- **Valid Transitions**:
  - `PENDING` ➔ `PROCESSING`, `COMPLETED`, `FAILED`, `CANCELLED`
  - `PROCESSING` ➔ `COMPLETED`, `FAILED`, `CANCELLED`
- **Terminal States**: `COMPLETED`, `FAILED`, `CANCELLED` (No further transitions allowed).

---

## 🚀 API Documentation & Endpoints

### 1. Create Transaction
- **HTTP Method:** `POST`
- **Endpoint:** `/api/transactions`
- **Request Body:**
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
- **Response (`201 Created`):**
```json
{
  "transactionId": "TXN-1001",
  "customerId": "CUST-001",
  "amount": 150.00,
  "currency": "USD",
  "type": "PAYMENT",
  "status": "PENDING",
  "createdAt": "2026-09-01T21:00:00Z",
  "updatedAt": "2026-09-01T21:00:00Z"
}
```

---

### 2. Get Transaction by ID
- **HTTP Method:** `GET`
- **Endpoint:** `/api/transactions/{id}`
- **Response (`200 OK`):** Returns the matching transaction or `404 Not Found`.

---

### 3. Update Transaction Status
- **HTTP Method:** `PATCH` or `PUT`
- **Endpoint:** `/api/transactions/{id}/status`
- **Request Body:**
```json
{
  "status": "COMPLETED",
  "reason": "Settled successfully"
}
```
- **Response (`200 OK`):** Updated transaction details or `400 Bad Request` if state transition is invalid.

---

### 4. Get Customer Transactions
- **HTTP Method:** `GET`
- **Endpoint:** `/api/customers/{customerId}/transactions`
- **Response (`200 OK`):** List of all transactions associated with the given customer.

---

## 🛠️ How to Build & Run

### Prerequisites
- JDK 17 or higher installed.

### 1. Run Unit & Integration Tests
Navigate to the project root and run:

**Linux / macOS:**
```bash
cd Transaction-assignment
./mvnw clean test
```

**Windows:**
```cmd
cd Transaction-assignment
mvnw.cmd clean test
```

### 2. Run the Application
```cmd
mvnw.cmd spring-boot:run
```

Once running:
- **Web UI Dashboard:** Open `http://localhost:8080` in your browser.
- **H2 Database Console:** Open `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:transactions`, User: `SA`).

---

## 🧪 Testing Results

All 20 automated tests execute cleanly with 0 failures:
- `TransactionStatusTest` (5 tests) – Verifies valid and invalid state transitions.
- `TransactionServiceTest` (8 tests) – Verifies business logic, auto-generation of IDs, duplicate detection, and retrieval.
- `TransactionControllerTest` (6 tests) – Verifies REST endpoints, payloads, HTTP status codes, and exception mapping.
- `TransactionStarterApplicationTests` (1 test) – Context initialization check.

