# Transaction Rollback - Testing Guide

## Test Scenarios

### Test 1: Delete INCOME Transaction - Verify Rollback

#### Setup
```bash
# Create account
POST /api/accounts
{
  "name": "Main Account",
  "type": "BANK",
  "balance": 5000
}
# Response: id=1, balance=5000
```

#### Create INCOME Transaction
```bash
POST /api/transactions
{
  "type": "INCOME",
  "amount": 1000,
  "description": "Salary",
  "accountId": 1
}
# Response: id=1, type=INCOME, amount=1000
```

#### Verify Balance Increased
```bash
GET /api/accounts/1
# Response: balance=6000 (5000 + 1000)
```

#### Delete Transaction
```bash
DELETE /api/transactions/1
# Response: 204 No Content
```

#### Verify Balance Rolled Back
```bash
GET /api/accounts/1
# Response: balance=5000 (6000 - 1000 = rolled back)
```

**Expected Result:** ✅ Balance returns to 5000

---

### Test 2: Delete EXPENSE Transaction - Verify Rollback

#### Setup
```bash
# Create account
POST /api/accounts
{
  "name": "Expense Account",
  "type": "BANK",
  "balance": 5000
}
# Response: id=1, balance=5000
```

#### Create EXPENSE Transaction
```bash
POST /api/transactions
{
  "type": "EXPENSE",
  "amount": 500,
  "description": "Groceries",
  "accountId": 1
}
# Response: id=1, type=EXPENSE, amount=500
```

#### Verify Balance Decreased
```bash
GET /api/accounts/1
# Response: balance=4500 (5000 - 500)
```

#### Delete Transaction
```bash
DELETE /api/transactions/1
# Response: 204 No Content
```

#### Verify Balance Rolled Back
```bash
GET /api/accounts/1
# Response: balance=5000 (4500 + 500 = rolled back)
```

**Expected Result:** ✅ Balance returns to 5000

---

### Test 3: Delete TRANSFER Transaction - Verify Both Accounts Rollback

#### Setup
```bash
# Create source account
POST /api/accounts
{
  "name": "Account A",
  "type": "BANK",
  "balance": 5000
}
# Response: id=1, balance=5000

# Create destination account
POST /api/accounts
{
  "name": "Account B",
  "type": "BANK",
  "balance": 2000
}
# Response: id=2, balance=2000
```

#### Create TRANSFER Transaction
```bash
POST /api/transactions
{
  "type": "TRANSFER",
  "amount": 1000,
  "description": "Transfer to B",
  "sourceAccountId": 1,
  "destinationAccountId": 2
}
# Response: id=1, type=TRANSFER, amount=1000
```

#### Verify Balances Changed
```bash
GET /api/accounts/1
# Response: balance=4000 (5000 - 1000)

GET /api/accounts/2
# Response: balance=3000 (2000 + 1000)
```

#### Delete Transaction
```bash
DELETE /api/transactions/1
# Response: 204 No Content
```

#### Verify Both Balances Rolled Back
```bash
GET /api/accounts/1
# Response: balance=5000 (4000 + 1000 = rolled back)

GET /api/accounts/2
# Response: balance=2000 (3000 - 1000 = rolled back)
```

**Expected Result:** ✅ Both balances return to original values

---

### Test 4: Delete Non-existent Transaction

#### Request
```bash
DELETE /api/transactions/999
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

### Test 5: Multiple Transactions - Delete One

#### Setup
```bash
# Create account with initial balance 10000
POST /api/accounts
{
  "name": "Test Account",
  "type": "BANK",
  "balance": 10000
}
# Response: id=1, balance=10000
```

#### Create Multiple INCOME Transactions
```bash
# Transaction 1
POST /api/transactions
{
  "type": "INCOME",
  "amount": 500,
  "description": "Income 1",
  "accountId": 1
}
# Response: id=1, amount=500

# Transaction 2
POST /api/transactions
{
  "type": "INCOME",
  "amount": 300,
  "description": "Income 2",
  "accountId": 1
}
# Response: id=2, amount=300

# Transaction 3
POST /api/transactions
{
  "type": "INCOME",
  "amount": 200,
  "description": "Income 3",
  "accountId": 1
}
# Response: id=3, amount=200
```

#### Verify Balance
```bash
GET /api/accounts/1
# Response: balance=11000 (10000 + 500 + 300 + 200)
```

#### Delete Transaction 2
```bash
DELETE /api/transactions/2
# Response: 204 No Content
```

#### Verify Balance (only Transaction 2 rolled back)
```bash
GET /api/accounts/1
# Response: balance=10700 (11000 - 300 = only txn 2 rolled back)
```

#### Verify Other Transactions Still Exist
```bash
GET /api/transactions/1
# Response: 200 OK with transaction 1

GET /api/transactions/2
# Response: 404 Not Found (deleted)

GET /api/transactions/3
# Response: 200 OK with transaction 3
```

**Expected Result:** ✅ Only Transaction 2 rolled back and deleted

---

### Test 6: Complex Scenario - Multiple Transfers

#### Setup
```bash
# Account A: 5000
POST /api/accounts {"name":"Account A","type":"BANK","balance":5000}
# Response: id=1

# Account B: 3000
POST /api/accounts {"name":"Account B","type":"BANK","balance":3000}
# Response: id=2

# Account C: 1000
POST /api/accounts {"name":"Account C","type":"BANK","balance":1000}
# Response: id=3
```

#### Create Transfers
```bash
# Transfer 1: A → B (500)
POST /api/transactions
{
  "type": "TRANSFER",
  "amount": 500,
  "sourceAccountId": 1,
  "destinationAccountId": 2
}
# A: 4500, B: 3500, C: 1000

# Transfer 2: B → C (200)
POST /api/transactions
{
  "type": "TRANSFER",
  "amount": 200,
  "sourceAccountId": 2,
  "destinationAccountId": 3
}
# A: 4500, B: 3300, C: 1200

# Transfer 3: C → A (100)
POST /api/transactions
{
  "type": "TRANSFER",
  "amount": 100,
  "sourceAccountId": 3,
  "destinationAccountId": 1
}
# A: 4600, B: 3300, C: 1100
```

#### Verify Balances
```bash
GET /api/accounts/1 # balance=4600
GET /api/accounts/2 # balance=3300
GET /api/accounts/3 # balance=1100
```

#### Delete Transfer 2 (B → C for 200)
```bash
DELETE /api/transactions/2
# Response: 204 No Content
```

#### Verify Balances After Rollback
```bash
GET /api/accounts/1 # balance=4600 (unchanged)
GET /api/accounts/2 # balance=3500 (3300 + 200 added back)
GET /api/accounts/3 # balance=1000 (1100 - 200 subtracted back)
```

**Expected Result:** ✅ Only Account B and C balances changed, A unchanged

---

## cURL Command Templates

### Delete Transaction (Generic)
```bash
curl -X DELETE http://localhost:8080/api/transactions/{transactionId}
```

### Delete and Verify (Script)
```bash
#!/bin/bash

# Get initial balance
INITIAL=$(curl -s http://localhost:8080/api/accounts/1 | grep -o '"balance":[0-9.]*' | cut -d':' -f2)
echo "Initial Balance: $INITIAL"

# Delete transaction
curl -X DELETE http://localhost:8080/api/transactions/1
echo "Transaction deleted"

# Get final balance
FINAL=$(curl -s http://localhost:8080/api/accounts/1 | grep -o '"balance":[0-9.]*' | cut -d':' -f2)
echo "Final Balance: $FINAL"

# Check if rolled back
if [ "$INITIAL" == "$FINAL" ]; then
  echo "✅ Balance rolled back successfully"
else
  echo "❌ Balance not rolled back"
fi
```

---

## Expected Behavior Summary

| Scenario | Before Delete | After Delete | Status |
|----------|---------------|--------------|--------|
| INCOME - Account | +1000 | -1000 (rollback) | ✅ |
| EXPENSE - Account | -500 | +500 (rollback) | ✅ |
| TRANSFER - Source | -1000 | +1000 (rollback) | ✅ |
| TRANSFER - Dest | +1000 | -1000 (rollback) | ✅ |
| Non-existent | N/A | 404 Error | ✅ |
| Other Txns | Unaffected | Unaffected | ✅ |

---

## Troubleshooting

### Balance Not Rolled Back
1. Check server logs for rollback messages
2. Verify transaction type is correct (INCOME/EXPENSE/TRANSFER)
3. Verify account exists and is active
4. Check database transaction isolation level

### Partial Rollback
1. Check if both source and destination accounts were found
2. Verify accounts are active (isActive = true)
3. Check for database constraints

### Delete Returns 404
1. Verify transaction ID exists
2. Check if transaction was already deleted
3. Confirm transaction type is valid

### Delete Returns 500
1. Check server logs for exception details
2. Verify database connectivity
3. Verify account records exist in database
4. Check for foreign key constraint issues

---

## Performance Considerations

- Rollback is atomic - all or nothing
- Uses database transactions for consistency
- Minimal overhead (same as create operation)
- Scales well with large account balances
- No performance impact on other accounts

