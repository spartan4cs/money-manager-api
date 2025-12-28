# Money Manager API

A comprehensive Spring Boot REST API for managing personal finances with support for multiple accounts, transactions, and transfers.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [Database Access](#database-access)
- [API Endpoints](#api-endpoints)
  - [Account Management](#account-management)
  - [Transactions](#transactions)
- [Features](#features)
- [Architecture](#architecture)

---

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Git

## Installation & Setup

1. **Clone the repository:**
```bash
git clone <repository-url>
cd money-manager-api
```

2. **Build the project:**
```bash
mvn clean install -DskipTests
```

This will download all dependencies including the Spring Boot parent POM and build the project.

3. **Install dependencies (if needed):**
```bash
mvn dependency:resolve
```

---

## Running the Application

Start the application with:

```bash
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

---

## Database Access

### H2 Console (Web UI)

Once the application is running, access the H2 console at:

```
http://localhost:8080/h2-console
```

**Login Credentials:**
- JDBC URL: `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- User Name: `sa`
- Password: (leave blank)

Click **Connect** to access the database UI.

---

## API Endpoints

### Base URL
```
http://localhost:8080/api
```

---

## Account Management

### 1. Create Account

**Endpoint:** `POST /api/accounts`

**Description:** Create a new account (Bank, Credit Card, E-Wallet, etc.)

**Request Body:**
```json
{
  "name": "HDFC Bank",
  "type": "BANK",
  "balance": 50000.00,
  "accountNumber": "1234567890",
  "provider": "HDFC",
  "description": "Main savings account"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "HDFC Bank",
  "type": "BANK",
  "balance": 50000.00,
  "accountNumber": "1234567890",
  "provider": "HDFC",
  "description": "Main savings account",
  "createdAt": "2025-12-28T10:30:00",
  "updatedAt": "2025-12-28T10:30:00",
  "isActive": true
}
```

**Validation Rules:**
- `name`: Required, cannot be empty
- `type`: Required, must be one of: BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT
- `balance`: Defaults to 0 if not provided
- `accountNumber`: Optional

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "HDFC Bank",
    "type": "BANK",
    "balance": 50000.00,
    "provider": "HDFC"
  }'
```

---

### 2. Get All Accounts

**Endpoint:** `GET /api/accounts`

**Description:** Retrieve all active accounts

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "HDFC Bank",
    "type": "BANK",
    "balance": 49500.00,
    "accountNumber": "1234567890",
    "provider": "HDFC",
    "description": "Main savings account",
    "createdAt": "2025-12-28T10:30:00",
    "updatedAt": "2025-12-28T10:45:00",
    "isActive": true
  },
  {
    "id": 2,
    "name": "Google Pay",
    "type": "E_WALLET",
    "balance": 5000.00,
    "provider": "Google",
    "description": "Mobile wallet",
    "createdAt": "2025-12-28T10:35:00",
    "updatedAt": "2025-12-28T10:35:00",
    "isActive": true
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/accounts
```

---

### 3. Get Account by ID

**Endpoint:** `GET /api/accounts/{id}`

**Description:** Retrieve a specific account by its ID

**Path Parameters:**
- `id`: Account ID (Long)

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "HDFC Bank",
  "type": "BANK",
  "balance": 49500.00,
  "accountNumber": "1234567890",
  "provider": "HDFC",
  "description": "Main savings account",
  "createdAt": "2025-12-28T10:30:00",
  "updatedAt": "2025-12-28T10:45:00",
  "isActive": true
}
```

**Response (404 Not Found):**
```json
{
  "error": "Account not found"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/accounts/1
```

---

### 4. Get Account by Name

**Endpoint:** `GET /api/accounts/by-name/{name}`

**Description:** Find an account by its name

**Path Parameters:**
- `name`: Account name (String)

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "HDFC Bank",
  "type": "BANK",
  "balance": 49500.00,
  "accountNumber": "1234567890",
  "provider": "HDFC",
  "description": "Main savings account",
  "createdAt": "2025-12-28T10:30:00",
  "updatedAt": "2025-12-28T10:45:00",
  "isActive": true
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/accounts/by-name/HDFC%20Bank
```

---

### 5. Get Accounts by Type

**Endpoint:** `GET /api/accounts/by-type/{type}`

**Description:** List all accounts of a specific type

**Path Parameters:**
- `type`: Account type (BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "HDFC Bank",
    "type": "BANK",
    "balance": 49500.00,
    "accountNumber": "1234567890",
    "provider": "HDFC",
    "description": "Main savings account",
    "createdAt": "2025-12-28T10:30:00",
    "updatedAt": "2025-12-28T10:45:00",
    "isActive": true
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/accounts/by-type/BANK
```

---

### 6. Update Account

**Endpoint:** `PUT /api/accounts/{id}`

**Description:** Update an existing account

**Path Parameters:**
- `id`: Account ID (Long)

**Request Body:**
```json
{
  "name": "HDFC Bank Updated",
  "balance": 60000.00,
  "description": "Updated description"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "HDFC Bank Updated",
  "type": "BANK",
  "balance": 60000.00,
  "accountNumber": "1234567890",
  "provider": "HDFC",
  "description": "Updated description",
  "createdAt": "2025-12-28T10:30:00",
  "updatedAt": "2025-12-28T11:00:00",
  "isActive": true
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/accounts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "HDFC Bank Updated",
    "balance": 60000.00
  }'
```

---

### 7. Delete Account (Soft Delete)

**Endpoint:** `DELETE /api/accounts/{id}`

**Description:** Delete an account (marks as inactive, doesn't remove from DB)

**Path Parameters:**
- `id`: Account ID (Long)

**Response (204 No Content)**

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/accounts/1
```

---

## Transactions

### 1. Create Transaction (INCOME, EXPENSE, or TRANSFER)

**Endpoint:** `POST /api/transactions`

**Description:** Create a new transaction

#### INCOME Transaction

```json
{
  "type": "INCOME",
  "amount": 25000.00,
  "description": "Salary",
  "accountId": 1
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "amount": 25000.00,
  "description": "Salary",
  "dateTime": "2025-12-28T10:45:00",
  "type": "INCOME",
  "accountId": 1,
  "sourceAccountId": null,
  "destinationAccountId": null
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "type": "INCOME",
    "amount": 25000.00,
    "description": "Salary",
    "accountId": 1
  }'
```

---

#### EXPENSE Transaction

```json
{
  "type": "EXPENSE",
  "amount": 500.00,
  "description": "Groceries",
  "accountId": 1
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "amount": 500.00,
  "description": "Groceries",
  "dateTime": "2025-12-28T10:50:00",
  "type": "EXPENSE",
  "accountId": 1,
  "sourceAccountId": null,
  "destinationAccountId": null
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EXPENSE",
    "amount": 500.00,
    "description": "Groceries",
    "accountId": 1
  }'
```

---

#### TRANSFER Transaction

```json
{
  "type": "TRANSFER",
  "amount": 5000.00,
  "description": "Transfer to wallet",
  "sourceAccountId": 1,
  "destinationAccountId": 2
}
```

**Response (201 Created):**
```json
{
  "id": 3,
  "amount": 5000.00,
  "description": "Transfer to wallet",
  "dateTime": "2025-12-28T10:55:00",
  "type": "TRANSFER",
  "accountId": null,
  "sourceAccountId": 1,
  "destinationAccountId": 2
}
```

**Note:** TRANSFER automatically updates both accounts:
- Deducts from source account
- Adds to destination account

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TRANSFER",
    "amount": 5000.00,
    "description": "Transfer to wallet",
    "sourceAccountId": 1,
    "destinationAccountId": 2
  }'
```

---

### 2. Get All Transactions

**Endpoint:** `GET /api/transactions`

**Description:** Retrieve all transactions

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "amount": 25000.00,
    "description": "Salary",
    "dateTime": "2025-12-28T10:45:00",
    "type": "INCOME",
    "accountId": 1,
    "sourceAccountId": null,
    "destinationAccountId": null
  },
  {
    "id": 2,
    "amount": 500.00,
    "description": "Groceries",
    "dateTime": "2025-12-28T10:50:00",
    "type": "EXPENSE",
    "accountId": 1,
    "sourceAccountId": null,
    "destinationAccountId": null
  },
  {
    "id": 3,
    "amount": 5000.00,
    "description": "Transfer to wallet",
    "dateTime": "2025-12-28T10:55:00",
    "type": "TRANSFER",
    "accountId": null,
    "sourceAccountId": 1,
    "destinationAccountId": 2
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/transactions
```

---

### 3. Get Transaction by ID

**Endpoint:** `GET /api/transactions/{id}`

**Description:** Retrieve a specific transaction

**Path Parameters:**
- `id`: Transaction ID (Long)

**Response (200 OK):**
```json
{
  "id": 1,
  "amount": 25000.00,
  "description": "Salary",
  "dateTime": "2025-12-28T10:45:00",
  "type": "INCOME",
  "accountId": 1,
  "sourceAccountId": null,
  "destinationAccountId": null
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/transactions/1
```

---

### 4. Get Transactions by Type

**Endpoint:** `GET /api/transactions/by-type/{type}`

**Description:** Get all transactions of a specific type

**Path Parameters:**
- `type`: Transaction type (INCOME, EXPENSE, or TRANSFER)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "amount": 25000.00,
    "description": "Salary",
    "dateTime": "2025-12-28T10:45:00",
    "type": "INCOME",
    "accountId": 1,
    "sourceAccountId": null,
    "destinationAccountId": null
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/transactions/by-type/INCOME
```

---

### 5. Get Transactions by Account

**Endpoint:** `GET /api/transactions/account/{accountId}`

**Description:** Get all transactions (INCOME/EXPENSE) for a specific account

**Path Parameters:**
- `accountId`: Account ID (Long)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "amount": 25000.00,
    "description": "Salary",
    "dateTime": "2025-12-28T10:45:00",
    "type": "INCOME",
    "accountId": 1,
    "sourceAccountId": null,
    "destinationAccountId": null
  },
  {
    "id": 2,
    "amount": 500.00,
    "description": "Groceries",
    "dateTime": "2025-12-28T10:50:00",
    "type": "EXPENSE",
    "accountId": 1,
    "sourceAccountId": null,
    "destinationAccountId": null
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/transactions/account/1
```

---

### 6. Get Transfers FROM Account

**Endpoint:** `GET /api/transactions/transfers/from/{sourceAccountId}`

**Description:** Get all transfer transactions sent FROM a specific account

**Path Parameters:**
- `sourceAccountId`: Source Account ID (Long)

**Response (200 OK):**
```json
[
  {
    "id": 3,
    "amount": 5000.00,
    "description": "Transfer to wallet",
    "dateTime": "2025-12-28T10:55:00",
    "type": "TRANSFER",
    "accountId": null,
    "sourceAccountId": 1,
    "destinationAccountId": 2
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/transactions/transfers/from/1
```

---

### 7. Get Transfers TO Account

**Endpoint:** `GET /api/transactions/transfers/to/{destAccountId}`

**Description:** Get all transfer transactions received TO a specific account

**Path Parameters:**
- `destAccountId`: Destination Account ID (Long)

**Response (200 OK):**
```json
[
  {
    "id": 3,
    "amount": 5000.00,
    "description": "Transfer to wallet",
    "dateTime": "2025-12-28T10:55:00",
    "type": "TRANSFER",
    "accountId": null,
    "sourceAccountId": 1,
    "destinationAccountId": 2
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/transactions/transfers/to/2
```

---

### 8. Delete Transaction

**Endpoint:** `DELETE /api/transactions/{id}`

**Description:** Delete a transaction

**Path Parameters:**
- `id`: Transaction ID (Long)

**Response (204 No Content)**

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/transactions/1
```

---

## Features

### Account Management
- ✅ Create multiple accounts (Bank, Credit Card, E-Wallet, etc.)
- ✅ Soft delete accounts (mark as inactive)
- ✅ View account balance
- ✅ Search accounts by name or type
- ✅ Update account information

### Transaction Management
- ✅ **INCOME** transactions: Money coming into an account
- ✅ **EXPENSE** transactions: Money going out of an account
- ✅ **TRANSFER** transactions: Money moving between two accounts
- ✅ Automatic balance updates on transactions
- ✅ Query transactions by type, account, or transfer direction
- ✅ Track all transactions with timestamps

### Data Validation
- ✅ Amount validation (must be > 0)
- ✅ Account type validation
- ✅ Required field validation
- ✅ Transfer validation (source ≠ destination)

### Logging
- ✅ Comprehensive logging at DEBUG, INFO, and WARN levels
- ✅ Track all operations for debugging and auditing
- ✅ Error logging for troubleshooting

---

## Architecture

### Technology Stack
- **Framework:** Spring Boot 2.7.0
- **Database:** H2 (In-memory)
- **ORM:** JPA/Hibernate
- **Logging:** SLF4J
- **Build Tool:** Maven

### Project Structure
```
money-manager-api/
├── src/main/java/com/opensource/moneymanager/
│   ├── controller/
│   │   ├── AccountController.java
│   │   └── TransactionController.java
│   ├── service/
│   │   ├── AccountService.java
│   │   └── TransactionService.java
│   ├── model/
│   │   ├── Account.java
│   │   └── Transaction.java
│   ├── dto/
│   │   ├── AccountDto.java
│   │   └── TransactionDto.java
│   ├── mapper/
│   │   ├── AccountMapper.java
│   │   └── TransactionMapper.java
│   ├── repository/
│   │   ├── AccountRepository.java
│   │   └── TransactionRepository.java
│   ├── enums/
│   │   ├── AccountType.java
│   │   └── TransactionType.java
│   └── MoneymanagerApplication.java
├── src/test/java/...
├── src/main/resources/
│   └── application.properties
├── pom.xml
└── README.md
```

### Data Models

#### Account Entity
```
id (Long) - Auto-generated ID
name (String) - Account name (unique)
type (String) - BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT
balance (BigDecimal) - Current balance
accountNumber (String) - Optional account number
provider (String) - Bank/service provider name
description (String) - Optional notes
createdAt (LocalDateTime) - Creation timestamp
updatedAt (LocalDateTime) - Last update timestamp
isActive (Boolean) - Soft delete flag
```

#### Transaction Entity
```
id (Long) - Auto-generated ID
amount (BigDecimal) - Transaction amount
description (String) - Transaction description
dateTime (LocalDateTime) - Transaction timestamp
type (String) - INCOME, EXPENSE, or TRANSFER
account (Account) - Primary account (for INCOME/EXPENSE)
sourceAccount (Account) - Source account (for TRANSFER)
destinationAccount (Account) - Destination account (for TRANSFER)
```

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Account name cannot be null or empty"
}
```

### 404 Not Found
```json
{
  "error": "Account not found with id: 999"
}
```

### 409 Conflict
```json
{
  "error": "Source and destination accounts cannot be the same"
}
```

---

## Testing

Run all tests:
```bash
mvn test
```

Run a specific test:
```bash
mvn test -Dtest=AccountServiceTest
```

---

## Configuration

Edit `src/main/resources/application.properties` to customize:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
```

---

## Support & Troubleshooting

### Application won't start
1. Ensure Java 11+ is installed: `java -version`
2. Clear Maven cache: `mvn clean`
3. Rebuild: `mvn install -DskipTests`

### Port already in use
Change the port in `application.properties`:
```properties
server.port=8081
```

### Database errors
- Access H2 console: `http://localhost:8080/h2-console`
- Clear data and restart the application

---

## License

This project is open source and available under the MIT License.

---

## Quick Start Example

1. **Start the application:**
```bash
mvn spring-boot:run
```

2. **Create an account:**
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"name":"My Bank","type":"BANK","balance":10000}'
```

3. **Create income transaction:**
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"type":"INCOME","amount":5000,"description":"Salary","accountId":1}'
```

4. **View all transactions:**
```bash
curl http://localhost:8080/api/transactions
```

5. **Check database:**
   - Open: `http://localhost:8080/h2-console`
   - Run SQL: `SELECT * FROM TRANSACTIONS;`

---

Last Updated: December 28, 2025

