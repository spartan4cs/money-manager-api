# Transaction Rollback Feature

## Overview
When a transaction is deleted, the account balances are automatically rolled back to reverse the changes made by that transaction. This ensures data consistency and maintains accurate account balances.

## How It Works

### Before Delete
When a transaction is created with `saveWithBalanceUpdate()`:
- **INCOME**: Account balance increases
- **EXPENSE**: Account balance decreases  
- **TRANSFER**: Source decreases, Destination increases

### On Delete
When a transaction is deleted with `deleteById()`:
- **INCOME**: Account balance decreases (reverse of income)
- **EXPENSE**: Account balance increases (reverse of expense)
- **TRANSFER**: Source increases (reverse of deduct), Destination decreases (reverse of add)

## Implementation Details

### INCOME Transaction
```
CREATE (saveWithBalanceUpdate):
  Account Balance: 1000 + 500 = 1500

DELETE (deleteById):
  Account Balance: 1500 - 500 = 1000 (rollback)
```

### EXPENSE Transaction
```
CREATE (saveWithBalanceUpdate):
  Account Balance: 1000 - 300 = 700

DELETE (deleteById):
  Account Balance: 700 + 300 = 1000 (rollback)
```

### TRANSFER Transaction
```
CREATE (saveWithBalanceUpdate):
  Source Account Balance: 1000 - 500 = 500
  Destination Account Balance: 500 + 500 = 1000

DELETE (deleteById):
  Source Account Balance: 500 + 500 = 1000 (rollback)
  Destination Account Balance: 1000 - 500 = 500 (rollback)
```

## Key Features

### ✅ Transactional Safety
- Uses `@Transactional` annotation
- All balance updates or none (ACID compliance)
- If rollback fails, entire operation is rolled back

### ✅ Comprehensive Logging
- Logs each rollback operation
- Logs new balances after rollback
- Helps with debugging and auditing

### ✅ Error Handling
- Throws `RuntimeException` if rollback fails
- Prevents orphaned transactions
- Clear error messages for debugging

### ✅ All Transaction Types
- INCOME transactions rollback correctly
- EXPENSE transactions rollback correctly
- TRANSFER transactions rollback both accounts

## Code Changes

### File: `TransactionService.java`
**Method:** `deleteById(Long id)` (added `@Transactional`)

**Changes:**
1. Added `@Transactional` annotation for atomic operations
2. Fetch the transaction before deletion
3. Reverse the balance changes based on transaction type:
   - INCOME: Subtract from account
   - EXPENSE: Add to account
   - TRANSFER: Add to source, subtract from destination
4. Save updated account balances
5. Delete the transaction record
6. Log all rollback operations

## Usage Example

### Scenario: Delete an INCOME Transaction

**Initial State:**
- Account Balance: 5000

**Create INCOME Transaction:**
```bash
POST /api/transactions
{
  "type": "INCOME",
  "amount": 1000,
  "accountId": 1
}
```
- Account Balance: 5000 + 1000 = 6000

**Delete the Transaction:**
```bash
DELETE /api/transactions/1
```
- Account Balance: 6000 - 1000 = 5000 (rolled back)

### Scenario: Delete a TRANSFER Transaction

**Initial State:**
- Account A Balance: 5000
- Account B Balance: 3000

**Create TRANSFER Transaction:**
```bash
POST /api/transactions
{
  "type": "TRANSFER",
  "amount": 1000,
  "sourceAccountId": 1,
  "destinationAccountId": 2
}
```
- Account A Balance: 5000 - 1000 = 4000
- Account B Balance: 3000 + 1000 = 4000

**Delete the Transaction:**
```bash
DELETE /api/transactions/1
```
- Account A Balance: 4000 + 1000 = 5000 (rolled back)
- Account B Balance: 4000 - 1000 = 3000 (rolled back)

## Error Scenarios

### Transaction Not Found
```
DELETE /api/transactions/999
```
**Response:** 404 Not Found
```json
{
  "status": 404,
  "message": "Transaction not found with id: 999",
  "error": "Not Found",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/transactions/999"
}
```

**Behavior:**
- No rollback attempted
- No transaction deleted
- Error returned to client

### Rollback Failure
If an error occurs during balance rollback:

**Response:** 400 Bad Request
```json
{
  "status": 400,
  "message": "Failed to rollback account balances: <error details>",
  "error": "Validation Error",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/transactions/1"
}
```

**Behavior:**
- Entire delete operation is rolled back
- Transaction remains in database
- Balances remain unchanged
- Error details logged on server

## Logging Output

When deleting a transaction, you'll see logs like:

```
2025-12-28 10:30:15 DEBUG TransactionService: Attempting to delete transaction with id=1
2025-12-28 10:30:15 INFO TransactionService: Rolling back TRANSFER transaction: source id=1, destination id=2, amount=1000
2025-12-28 10:30:15 INFO TransactionService: Source account balance rolled back: account id=1, new balance=5000
2025-12-28 10:30:15 INFO TransactionService: Destination account balance rolled back: account id=2, new balance=3000
2025-12-28 10:30:15 INFO TransactionService: Transaction deleted successfully with balance rollback: id=1, type=TRANSFER
```

## Testing

### Test Case 1: Delete INCOME Transaction
1. Create account with balance 1000
2. Create INCOME transaction for 500
3. Verify balance is 1500
4. Delete the transaction
5. Verify balance is back to 1000

### Test Case 2: Delete EXPENSE Transaction
1. Create account with balance 1000
2. Create EXPENSE transaction for 300
3. Verify balance is 700
4. Delete the transaction
5. Verify balance is back to 1000

### Test Case 3: Delete TRANSFER Transaction
1. Create two accounts: A (1000), B (500)
2. Create TRANSFER transaction 300 from A to B
3. Verify A = 700, B = 800
4. Delete the transaction
5. Verify A = 1000, B = 500

### Test Case 4: Delete Non-existent Transaction
1. Try to delete transaction with invalid ID
2. Verify 404 Not Found response
3. Verify no changes to any account balances

## Benefits

1. **Data Consistency** - Account balances always match transaction history
2. **Atomic Operations** - All or nothing approach with @Transactional
3. **Audit Trail** - Full logging of all rollback operations
4. **Error Safety** - Prevents partial updates (transaction succeeds or fails completely)
5. **User-Friendly** - Clear error messages when rollback fails
6. **Complete Support** - Works for all transaction types (INCOME, EXPENSE, TRANSFER)

## Related Features

- **Create Transaction:** `saveWithBalanceUpdate()` - Creates transaction and updates balances
- **Update Balance:** `updateBalance()` in AccountService - Manual balance adjustments
- **Soft Delete:** Accounts use soft delete (isActive flag)
- **Hard Delete:** Transactions use hard delete with balance rollback

## Future Enhancements

1. Add audit table to track all balance changes
2. Implement edit transaction (rollback old + create new)
3. Add bulk delete with rollback
4. Add transaction reversal (creates reversing transaction)
5. Add balance history tracking

