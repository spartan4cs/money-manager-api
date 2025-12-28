# Update Transaction - Testing Guide

## Test Scenarios

### Test 1: Update Amount - INCOME Transaction

#### Setup
```bash
# Create account
POST /api/accounts
{
  "name": "Test Account",
  "type": "BANK",
  "balance": 5000
}
# Response: id=1, balance=5000

# Create INCOME transaction
POST /api/transactions
{
  "type": "INCOME",
  "amount": 1000,
  "description": "Initial salary",
  "accountId": 1
}
# Response: id=1, amount=1000
# Account balance: 5000 + 1000 = 6000
```

#### Verify Initial State
```bash
GET /api/accounts/1
# Response: balance=6000
```

#### Update Transaction Amount
```bash
PUT /api/transactions/1
{
  "type": "INCOME",
  "amount": 1500,
  "description": "Initial salary",
  "dateTime": "2025-12-28T10:00:00"
}
```

**Expected Response: 200 OK**
```json
{
  "id": 1,
  "type": "INCOME",
  "amount": 1500,
  "description": "Initial salary",
  "dateTime": "2025-12-28T10:00:00"
}
```

#### Verify Balance Updated
```bash
GET /api/accounts/1
# Response: balance=6500
# Calculation: 6000 - 1000 (rollback) + 1500 (apply new) = 6500
```

**Expected Result:** ✅ Balance = 6500

---

### Test 2: Update Amount - EXPENSE Transaction

#### Setup
```bash
# Create account with balance 5000
POST /api/accounts
{
  "name": "Expense Account",
  "type": "BANK",
  "balance": 5000
}
# Response: id=1, balance=5000

# Create EXPENSE transaction for 500
POST /api/transactions
{
  "type": "EXPENSE",
  "amount": 500,
  "description": "Groceries",
  "accountId": 1
}
# Response: id=1, amount=500
# Account balance: 5000 - 500 = 4500
```

#### Update Expense Amount
```bash
PUT /api/transactions/1
{
  "type": "EXPENSE",
  "amount": 300,
  "description": "Groceries",
  "dateTime": "2025-12-28T10:00:00"
}
```

**Expected Response: 200 OK**

#### Verify Balance Updated
```bash
GET /api/accounts/1
# Response: balance=4700
# Calculation: 4500 + 500 (rollback) - 300 (apply new) = 4700
```

**Expected Result:** ✅ Balance = 4700

---

### Test 3: Update Type - INCOME to EXPENSE

#### Setup
```bash
# Create account with balance 5000
POST /api/accounts
{
  "name": "Type Change Account",
  "type": "BANK",
  "balance": 5000
}
# Response: id=1, balance=5000

# Create INCOME transaction for 1000
POST /api/transactions
{
  "type": "INCOME",
  "amount": 1000,
  "description": "Salary",
  "accountId": 1
}
# Response: id=1, type=INCOME, amount=1000
# Account balance: 5000 + 1000 = 6000
```

#### Update Type from INCOME to EXPENSE
```bash
PUT /api/transactions/1
{
  "type": "EXPENSE",
  "amount": 1000,
  "description": "Changed to expense",
  "dateTime": "2025-12-28T10:00:00"
}
```

**Expected Response: 200 OK**

#### Verify Type and Balance Updated
```bash
GET /api/accounts/1
# Response: balance=4000
# Calculation: 6000 - 1000 (rollback INCOME) - 1000 (apply EXPENSE) = 4000

GET /api/transactions/1
# Response: type=EXPENSE (changed), amount=1000
```

**Expected Result:** ✅ Type changed to EXPENSE, Balance = 4000

---

### Test 4: Update Description and Date Only

#### Setup
```bash
# Create transaction with 500 amount, EXPENSE type
POST /api/accounts {"name":"Account","type":"BANK","balance":5000}
# Response: id=1

POST /api/transactions
{
  "type": "EXPENSE",
  "amount": 500,
  "description": "Old description",
  "accountId": 1
}
# Response: id=1
# Account balance: 5000 - 500 = 4500
```

#### Update Description and Date
```bash
PUT /api/transactions/1
{
  "type": "EXPENSE",
  "amount": 500,
  "description": "Updated description",
  "dateTime": "2025-12-29T15:30:00"
}
```

**Expected Response: 200 OK**

#### Verify Balance Unchanged
```bash
GET /api/accounts/1
# Response: balance=4500 (NO CHANGE)
# Reason: Amount and type are same, so no balance adjustment
```

**Expected Result:** ✅ Balance unchanged = 4500

---

### Test 5: Update TRANSFER Transaction - Change Amount

#### Setup
```bash
# Create two accounts
POST /api/accounts {"name":"Account A","type":"BANK","balance":5000}
# Response: id=1, balance=5000

POST /api/accounts {"name":"Account B","type":"BANK","balance":3000}
# Response: id=2, balance=3000

# Create TRANSFER transaction: 500 from A to B
POST /api/transactions
{
  "type": "TRANSFER",
  "amount": 500,
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "description": "Initial transfer"
}
# A balance: 5000 - 500 = 4500
# B balance: 3000 + 500 = 3500
```

#### Verify Initial Balances
```bash
GET /api/accounts/1  # A = 4500
GET /api/accounts/2  # B = 3500
```

#### Update Transfer Amount to 750
```bash
PUT /api/transactions/1
{
  "type": "TRANSFER",
  "amount": 750,
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "description": "Updated transfer"
}
```

**Expected Response: 200 OK**

#### Verify Both Balances Updated
```bash
GET /api/accounts/1
# Response: balance=4250
# Calculation: 4500 + 500 (rollback) - 750 (apply new) = 4250

GET /api/accounts/2
# Response: balance=3750
# Calculation: 3500 - 500 (rollback) + 750 (apply new) = 3750
```

**Expected Result:** ✅ A = 4250, B = 3750

---

### Test 6: Update Non-existent Transaction

#### Request
```bash
PUT /api/transactions/999
{
  "type": "INCOME",
  "amount": 1000,
  "description": "Test",
  "dateTime": "2025-12-28T10:00:00"
}
```

#### Expected Response: 404 Not Found
```json
{
  "status": 404,
  "message": "Transaction not found with id: 999",
  "error": "Not Found",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/transactions/999"
}
```

**Expected Result:** ✅ Returns 404 error

---

### Test 7: Invalid Amount (Zero or Negative)

#### Setup
```bash
# Create valid transaction
POST /api/transactions
{
  "type": "INCOME",
  "amount": 1000,
  "accountId": 1
}
# Response: id=1
```

#### Try Update with Zero Amount
```bash
PUT /api/transactions/1
{
  "type": "INCOME",
  "amount": 0,
  "description": "Invalid"
}
```

#### Expected Response: 400 Bad Request
```json
{
  "status": 400,
  "message": "Transaction amount must be greater than zero",
  "error": "Validation Error",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/transactions/1"
}
```

**Expected Result:** ✅ Returns 400 error

---

### Test 8: Invalid Type

#### Request
```bash
PUT /api/transactions/1
{
  "type": "INVALID_TYPE",
  "amount": 1000,
  "description": "Test"
}
```

#### Expected Response: 400 Bad Request
```json
{
  "status": 400,
  "message": "Type must be one of: INCOME, EXPENSE, TRANSFER",
  "error": "Validation Error",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/transactions/1"
}
```

**Expected Result:** ✅ Returns 400 error

---

### Test 9: Null Amount

#### Request
```bash
PUT /api/transactions/1
{
  "type": "INCOME",
  "amount": null,
  "description": "Test"
}
```

#### Expected Response: 400 Bad Request
```json
{
  "status": 400,
  "message": "Transaction amount must be greater than zero",
  "error": "Validation Error",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/transactions/1"
}
```

**Expected Result:** ✅ Returns 400 error

---

### Test 10: Multiple Consecutive Updates

#### Setup
```bash
# Create transaction with amount 1000
POST /api/transactions
{
  "type": "EXPENSE",
  "amount": 1000,
  "accountId": 1
}
# Balance: 5000 - 1000 = 4000
```

#### First Update: 1500
```bash
PUT /api/transactions/1
{
  "type": "EXPENSE",
  "amount": 1500,
  "description": "First update"
}
# Balance: 4000 + 1000 - 1500 = 3500
```

#### Second Update: 800
```bash
PUT /api/transactions/1
{
  "type": "EXPENSE",
  "amount": 800,
  "description": "Second update"
}
# Balance: 3500 + 1500 - 800 = 4200
```

#### Third Update: 200
```bash
PUT /api/transactions/1
{
  "type": "EXPENSE",
  "amount": 200,
  "description": "Third update"
}
# Balance: 4200 + 800 - 200 = 4800
```

#### Verify Final Balance
```bash
GET /api/accounts/1
# Response: balance=4800
# Correct: Original 5000 - 200 (final amount) = 4800
```

**Expected Result:** ✅ Final balance = 4800 (correct after 3 updates)

---

## cURL Command Examples

### Update Amount
```bash
curl -X PUT http://localhost:8080/api/transactions/1 \
  -H "Content-Type: application/json" \
  -d '{
    "type": "INCOME",
    "amount": 1500,
    "description": "Updated salary",
    "dateTime": "2025-12-28T10:00:00"
  }'
```

### Update Type
```bash
curl -X PUT http://localhost:8080/api/transactions/1 \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EXPENSE",
    "amount": 1000,
    "description": "Changed to expense",
    "dateTime": "2025-12-28T10:00:00"
  }'
```

### Update Description Only
```bash
curl -X PUT http://localhost:8080/api/transactions/1 \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EXPENSE",
    "amount": 500,
    "description": "New description",
    "dateTime": "2025-12-28T10:00:00"
  }'
```

---

## Expected Behavior Summary

| Test Case | Action | Expected Result |
|-----------|--------|-----------------|
| Update Amount (↑) | Change 1000 to 1500 | Balance adjusted (+500) ✅ |
| Update Amount (↓) | Change 500 to 300 | Balance adjusted (+200) ✅ |
| Change Type | INCOME to EXPENSE | Balance double-adjusted ✅ |
| Update Description | Only change text | No balance change ✅ |
| Update Date | Only change date | No balance change ✅ |
| Update TRANSFER | Change amount | Both accounts adjusted ✅ |
| Non-existent | id=999 | 404 Not Found ✅ |
| Invalid Amount | amount=0/-100 | 400 Bad Request ✅ |
| Invalid Type | type=INVALID | 400 Bad Request ✅ |
| Multiple Updates | 1000→1500→800→200 | Final balance correct ✅ |

---

## Verification Steps

After each test:
1. ✅ Verify HTTP status code is correct
2. ✅ Verify response body contains updated transaction
3. ✅ Verify account balance(s) are correct
4. ✅ Verify logs show rollback and apply operations
5. ✅ Verify no other transactions are affected

## Performance Considerations

- Update operations are atomic
- Balance adjustments happen within same transaction
- Scales well with large numbers of transactions
- No impact on other accounts/transactions

