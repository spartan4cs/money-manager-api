# Update Transaction Feature

## Overview
Implemented the ability to update transactions with automatic balance adjustments. Users can update amount, date, description, and type. The system automatically handles balance rollback of old values and application of new values.

## Features

### ✅ Fields Can Be Updated
- **Amount** - Change the transaction amount (balance adjusted)
- **Date** - Change the transaction date
- **Description** - Change the transaction description
- **Type** - Change the transaction type (INCOME/EXPENSE/TRANSFER) (balance adjusted)

### ✅ Automatic Balance Adjustment
When amount or type changes:
1. Old transaction balances are rolled back
2. New transaction balances are applied
3. All changes are atomic (all or nothing)

### ✅ All Transaction Types Supported
- INCOME transactions
- EXPENSE transactions
- TRANSFER transactions (both accounts adjusted)

### ✅ Comprehensive Error Handling
- 404 Not Found if transaction doesn't exist
- 400 Bad Request for validation errors
- 500 Internal Server Error for unexpected issues
- Clear error messages returned to client

## API Endpoint

### PUT /api/transactions/{id}

**Request:**
```json
{
  "amount": 1500.00,
  "description": "Updated description",
  "dateTime": "2025-12-28T14:30:00",
  "type": "EXPENSE"
}
```

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "amount": 1500.00,
  "description": "Updated description",
  "dateTime": "2025-12-28T14:30:00",
  "type": "EXPENSE"
}
```

**Response (Not Found - 404):**
```json
{
  "status": 404,
  "message": "Transaction not found with id: 999",
  "error": "Not Found",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/transactions/999"
}
```

**Response (Validation Error - 400):**
```json
{
  "status": 400,
  "message": "Transaction amount must be greater than zero",
  "error": "Validation Error",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/transactions/1"
}
```

## Usage Examples

### Example 1: Update Amount Only

**Initial State:**
- Account Balance: 5000
- Transaction: INCOME, Amount: 1000

**Request:**
```bash
PUT /api/transactions/1
{
  "amount": 1500,
  "description": "Salary",
  "type": "INCOME"
}
```

**Balance Changes:**
- Old INCOME (1000) rolled back: 6000 - 1000 = 5000
- New INCOME (1500) applied: 5000 + 1500 = 6500

**New Account Balance:** 6500 ✅

### Example 2: Update Type (INCOME to EXPENSE)

**Initial State:**
- Account Balance: 6000
- Transaction: INCOME, Amount: 1000

**Request:**
```bash
PUT /api/transactions/1
{
  "amount": 1000,
  "description": "Changed to expense",
  "type": "EXPENSE"
}
```

**Balance Changes:**
- Old INCOME (1000) rolled back: 6000 - 1000 = 5000
- New EXPENSE (1000) applied: 5000 - 1000 = 4000

**New Account Balance:** 4000 ✅

### Example 3: Update Description and Date Only

**Initial State:**
- Account Balance: 5000
- Transaction: EXPENSE, Amount: 500, Date: 2025-12-27

**Request:**
```bash
PUT /api/transactions/1
{
  "amount": 500,
  "description": "Grocery shopping on Dec 28",
  "dateTime": "2025-12-28T15:30:00",
  "type": "EXPENSE"
}
```

**Balance Changes:**
- No balance change (amount and type unchanged)

**Account Balance:** 5000 (unchanged) ✅

### Example 4: Update TRANSFER Transaction

**Initial State:**
- Account A: 5000
- Account B: 3000
- Transaction: TRANSFER, Source A → B, Amount: 500

**Request:**
```bash
PUT /api/transactions/1
{
  "amount": 750,
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "type": "TRANSFER"
}
```

**Balance Changes:**
- Old TRANSFER (500) rolled back: A = 5000, B = 3000
- New TRANSFER (750) applied: A = 4250, B = 3750

**New Balances:**
- Account A: 4250 ✅
- Account B: 3750 ✅

## Implementation Details

### TransactionService Methods

#### `updateWithBalanceAdjustment(Long id, Transaction updatedTransaction)`

**Process:**
1. Find existing transaction by ID
2. Validate input (amount > 0, type not null)
3. Store old amount and type
4. Rollback old transaction balances using `rollbackBalances()`
5. Update transaction fields (amount, description, type, dateTime)
6. Save updated transaction
7. Apply new transaction balances using `applyBalanceChanges()`
8. Return updated transaction

**Error Handling:**
- Throws `IllegalArgumentException` if transaction not found
- Throws `IllegalArgumentException` if validation fails
- Throws `RuntimeException` if balance adjustment fails (entire operation rolled back)

#### `rollbackBalances(Transaction t, BigDecimal amount)`

**Purpose:** Reverses the balance changes made by the original transaction

**Process:**
- **INCOME:** Subtract amount from account
- **EXPENSE:** Add amount back to account
- **TRANSFER:** Add to source, subtract from destination

#### `applyBalanceChanges(Transaction t)`

**Purpose:** Applies the new balance changes for the updated transaction

**Process:**
- **INCOME:** Add amount to account
- **EXPENSE:** Subtract amount from account
- **TRANSFER:** Subtract from source, add to destination

### TransactionController Endpoint

#### `update(Long id, TransactionDto dto)`

**Responsibilities:**
1. Log the update request
2. Map DTO to entity
3. Call service to update with balance adjustment
4. Map result back to DTO
5. Handle exceptions and return appropriate responses
6. Return 200 OK with updated transaction on success
7. Return 404 if transaction not found
8. Return 400 if validation fails
9. Return 500 for unexpected errors

## Error Scenarios

### Transaction Not Found
```
PUT /api/transactions/999
```
**Response:** 404 Not Found
- Transaction doesn't exist in database

### Invalid Amount
```
PUT /api/transactions/1
{
  "amount": 0,
  ...
}
```
**Response:** 400 Bad Request
- Amount must be greater than zero

### Null Type
```
PUT /api/transactions/1
{
  "type": null,
  ...
}
```
**Response:** 400 Bad Request
- Type cannot be null

### Invalid Type
```
PUT /api/transactions/1
{
  "type": "INVALID",
  ...
}
```
**Response:** 400 Bad Request
- Type must be INCOME, EXPENSE, or TRANSFER

### Account Not Found (for balance update)
```
PUT /api/transactions/1
// But account doesn't exist in database
```
**Response:** 500 Internal Server Error
- Error details logged on server
- Transaction remains in original state

## Logging

### Update Success Log
```
2025-12-28 10:30:15 INFO TransactionController: PUT /api/transactions/1 - Updating transaction: type=EXPENSE, amount=500
2025-12-28 10:30:15 DEBUG TransactionService: Attempting to update transaction with balance adjustment: id=1, new amount=500
2025-12-28 10:30:15 DEBUG TransactionService: Rolling back balances for transaction id=1, type=INCOME, amount=1000
2025-12-28 10:30:15 DEBUG TransactionService: Rolled back INCOME balance: account id=1, new balance=5000
2025-12-28 10:30:15 INFO TransactionService: Transaction updated: id=1, old type=INCOME, new type=EXPENSE, old amount=1000, new amount=500
2025-12-28 10:30:15 DEBUG TransactionService: Applying balance changes for transaction id=1, type=EXPENSE, amount=500
2025-12-28 10:30:15 DEBUG TransactionService: Applied EXPENSE balance: account id=1, new balance=4500
2025-12-28 10:30:15 INFO TransactionController: Transaction updated successfully: id=1
```

### Update Failure Log
```
2025-12-28 10:30:15 ERROR TransactionService: Error updating transaction id=1: Account not found with id: 999
2025-12-28 10:30:15 ERROR TransactionController: Failed to update transaction: Account not found with id: 999
```

## Testing Recommendations

### Test 1: Update Amount Only
1. Create INCOME transaction for 1000 → balance = 6000
2. Update amount to 1500
3. Verify balance = 6500 ✅

### Test 2: Update Type
1. Create INCOME transaction for 1000 → balance = 6000
2. Update type to EXPENSE
3. Verify balance = 4000 ✅

### Test 3: Update Description/Date Only
1. Create EXPENSE transaction for 500 → balance = 4500
2. Update description and date
3. Verify balance unchanged = 4500 ✅

### Test 4: Update Non-existent Transaction
1. Try to update transaction with id=999
2. Verify 404 Not Found response ✅

### Test 5: Invalid Amount
1. Try to update with amount=0 or negative
2. Verify 400 Bad Request response ✅

### Test 6: Update TRANSFER Transaction
1. Create TRANSFER 500 from A to B
2. Update amount to 750
3. Verify A balance decreased by 750 (not 500)
4. Verify B balance increased by 750 (not 500) ✅

### Test 7: Multiple Updates
1. Create transaction, update it, update again
2. Verify all balance adjustments are correct ✅

## Benefits

1. **Complete Control** - Users can update any transaction field
2. **Automatic Adjustments** - Balance changes handled automatically
3. **Atomic Operations** - All or nothing approach
4. **Error Safe** - Validation prevents invalid states
5. **Comprehensive Logging** - Full audit trail of updates
6. **All Types Supported** - Works for all transaction types
7. **Clear Feedback** - Detailed error messages on failure

## Related Features

- **Create Transaction:** `POST /api/transactions` with balance update
- **Delete Transaction:** `DELETE /api/transactions/{id}` with balance rollback
- **Get Transaction:** `GET /api/transactions/{id}` to fetch current state
- **List Transactions:** `GET /api/transactions` to view all transactions

## Future Enhancements

1. Partial updates (only update specified fields)
2. Bulk update transactions
3. Update history/audit log
4. Revert to previous version
5. Update validation rules (e.g., cannot update past transactions)

