# Account-Transaction Relationship Explanation

## Overview

The Account and Transaction entities have a complex **One-to-Many relationship** (1:N) implemented through three separate foreign-key relationships in the `Transaction` table.

---

## Why Three Relationships?

The Money Manager needs to track transactions in three different contexts:

1. **INCOME/EXPENSE Transactions** — Money flowing in or out of a single account
2. **TRANSFER Transactions** — Money moving FROM one account TO another account

This requires three distinct relationships to handle all scenarios.

---

## Relationship Breakdown

### Relationship #1: INCOME & EXPENSE (Primary Account)

```
Transaction.account ←→ Account
Cardinality: Many Transactions : One Account
```

**Description:**
- Represents the primary account for INCOME or EXPENSE transactions
- Only used when transaction `type = "INCOME"` or `type = "EXPENSE"`
- One account can have many income/expense transactions
- The foreign key column in DB: `account_id`

**Example:**
```
Account: "HDFC Bank" (id=1)
  ├── Transaction: +25000 INCOME (Salary) - account_id = 1
  ├── Transaction: -500 EXPENSE (Groceries) - account_id = 1
  └── Transaction: -200 EXPENSE (Fuel) - account_id = 1
```

**SQL Representation:**
```sql
SELECT * FROM transactions WHERE account_id = 1 AND type IN ('INCOME', 'EXPENSE');
```

---

### Relationship #2: TRANSFER Source Account

```
Transaction.sourceAccount ←→ Account
Cardinality: Many Transactions : One Account
```

**Description:**
- Represents the account money is being transferred FROM
- Only used when transaction `type = "TRANSFER"`
- One account can have many outgoing transfers
- The foreign key column in DB: `source_account_id`

**Example:**
```
Account: "HDFC Bank" (id=1)
  └── Transaction: -5000 TRANSFER (to Google Pay) - source_account_id = 1
```

**SQL Representation:**
```sql
SELECT * FROM transactions WHERE source_account_id = 1 AND type = 'TRANSFER';
```

---

### Relationship #3: TRANSFER Destination Account

```
Transaction.destinationAccount ←→ Account
Cardinality: Many Transactions : One Account
```

**Description:**
- Represents the account money is being transferred TO
- Only used when transaction `type = "TRANSFER"`
- One account can have many incoming transfers
- The foreign key column in DB: `destination_account_id`

**Example:**
```
Account: "Google Pay" (id=2)
  └── Transaction: +5000 TRANSFER (from HDFC Bank) - destination_account_id = 2
```

**SQL Representation:**
```sql
SELECT * FROM transactions WHERE destination_account_id = 2 AND type = 'TRANSFER';
```

---

## Database Schema

### Accounts Table
```sql
CREATE TABLE accounts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE,
  type VARCHAR(50) NOT NULL,
  balance DECIMAL(19,2) NOT NULL,
  account_number VARCHAR(255),
  provider VARCHAR(255),
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT TRUE
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  amount DECIMAL(19,2) NOT NULL,
  description TEXT,
  date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  type VARCHAR(50) NOT NULL,
  account_id BIGINT,                    -- FK to accounts (for INCOME/EXPENSE)
  source_account_id BIGINT,              -- FK to accounts (for TRANSFER source)
  destination_account_id BIGINT,         -- FK to accounts (for TRANSFER destination)
  
  FOREIGN KEY (account_id) REFERENCES accounts(id),
  FOREIGN KEY (source_account_id) REFERENCES accounts(id),
  FOREIGN KEY (destination_account_id) REFERENCES accounts(id)
);
```

---

## Java ORM Mapping (Owning Side)

### Transaction Entity (Owning Side - has the Foreign Keys)

```java
@Entity
@Table(name = "transactions")
public class Transaction {

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;  // INCOME/EXPENSE account

    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;  // TRANSFER source

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;  // TRANSFER destination
}
```

**Annotations Explained:**
- `@ManyToOne` — Multiple transactions can reference one account
- `@JoinColumn(name = "...")` — Specifies the foreign key column name in the DB

---

## Java ORM Mapping (Inverse Side - BIDIRECTIONAL)

### Account Entity (Inverse Side - Optional for queries from Account to Transactions)

```java
@Entity
@Table(name = "accounts")
public class Account {

    // Income/Expense transactions for this account
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();

    // Outgoing transfers FROM this account
    @OneToMany(mappedBy = "sourceAccount", fetch = FetchType.LAZY)
    private Set<Transaction> outgoingTransfers = new HashSet<>();

    // Incoming transfers TO this account
    @OneToMany(mappedBy = "destinationAccount", fetch = FetchType.LAZY)
    private Set<Transaction> incomingTransfers = new HashSet<>();
}
```

**Annotations Explained:**
- `@OneToMany` — One account has many transactions
- `mappedBy = "account"` — Refers to the field name in Transaction entity that owns this relationship
- `fetch = FetchType.LAZY` — Load transactions only when explicitly accessed (improves performance)
- `Set<Transaction>` — Using Set to avoid duplicate relationships

---

## Relationship Rules & Constraints

### Rule 1: Type-Based Field Usage

| Transaction Type | `account` | `sourceAccount` | `destinationAccount` |
|------------------|-----------|-----------------|---------------------|
| **INCOME** | ✅ Required | ❌ NULL | ❌ NULL |
| **EXPENSE** | ✅ Required | ❌ NULL | ❌ NULL |
| **TRANSFER** | ❌ NULL | ✅ Required | ✅ Required |

### Rule 2: Source ≠ Destination

For TRANSFER transactions:
```java
if (sourceAccount.getId().equals(destinationAccount.getId())) {
    throw new IllegalArgumentException("Cannot transfer to same account");
}
```

### Rule 3: Validation in Service

The `TransactionService` validates these rules before saving:

```java
if ("INCOME".equals(t.getType()) || "EXPENSE".equals(t.getType())) {
    if (t.getAccount() == null || t.getAccount().getId() == null) {
        throw new IllegalArgumentException("Account required for " + t.getType());
    }
}

if ("TRANSFER".equals(t.getType())) {
    if (t.getSourceAccount() == null || t.getDestinationAccount() == null) {
        throw new IllegalArgumentException("Source & destination required for TRANSFER");
    }
    if (t.getSourceAccount().getId().equals(t.getDestinationAccount().getId())) {
        throw new IllegalArgumentException("Source and destination cannot be same");
    }
}
```

---

## Real-World Example

### Scenario: Complete Money Flow

**Initial State:**
```
Account 1: HDFC Bank (balance: 50,000)
Account 2: Google Pay (balance: 0)
Account 3: Credit Card (balance: 0)
```

**Transaction Sequence:**

1. **INCOME** — Salary received into HDFC
   ```json
   {
     "type": "INCOME",
     "amount": 100000,
     "accountId": 1
   }
   ```
   - DB: `account_id = 1, source_account_id = NULL, destination_account_id = NULL`
   - Account 1 balance: 50,000 + 100,000 = 150,000

2. **EXPENSE** — Paid groceries from HDFC
   ```json
   {
     "type": "EXPENSE",
     "amount": 5000,
     "accountId": 1
   }
   ```
   - DB: `account_id = 1, source_account_id = NULL, destination_account_id = NULL`
   - Account 1 balance: 150,000 - 5,000 = 145,000

3. **TRANSFER** — Transfer to Google Pay wallet
   ```json
   {
     "type": "TRANSFER",
     "amount": 20000,
     "sourceAccountId": 1,
     "destinationAccountId": 2
   }
   ```
   - DB: `account_id = NULL, source_account_id = 1, destination_account_id = 2`
   - Account 1 balance: 145,000 - 20,000 = 125,000
   - Account 2 balance: 0 + 20,000 = 20,000

---

## Query Examples Using Relationships

### Find All Income/Expense for an Account
```java
// Service method
List<Transaction> findByAccountId(Long accountId) {
    return repository.findByAccountId(accountId);
}

// SQL Generated
SELECT * FROM transactions WHERE account_id = 1 AND type IN ('INCOME', 'EXPENSE');
```

### Find All Outgoing Transfers from an Account
```java
// Service method
List<Transaction> findTransfersFromAccount(Long sourceAccountId) {
    return repository.findBySourceAccountId(sourceAccountId);
}

// SQL Generated
SELECT * FROM transactions WHERE source_account_id = 1 AND type = 'TRANSFER';
```

### Find All Incoming Transfers to an Account
```java
// Service method
List<Transaction> findTransfersToAccount(Long destAccountId) {
    return repository.findByDestinationAccountId(destAccountId);
}

// SQL Generated
SELECT * FROM transactions WHERE destination_account_id = 2 AND type = 'TRANSFER';
```

### From Account Entity (Bidirectional Query)
```java
// After adding @OneToMany mappings to Account:
Account account = accountService.findById(1);

// Get all income/expense transactions
Set<Transaction> incomeExpense = account.getTransactions();

// Get all transfers sent from this account
Set<Transaction> sent = account.getOutgoingTransfers();

// Get all transfers received to this account
Set<Transaction> received = account.getIncomingTransfers();
```

---

## JSON Serialization & Circular References

### Problem
Without proper configuration, serializing an Account with transactions can cause infinite loops:

```
Account
  └── transactions[]
       └── Transaction
            └── account (back to Account) → infinite loop!
```

### Solution: Use DTOs (Recommended)

**AccountDto** - Does NOT include transaction collections:
```json
{
  "id": 1,
  "name": "HDFC Bank",
  "type": "BANK",
  "balance": 125000.00
}
```

**TransactionDto** - Uses IDs instead of full Account objects:
```json
{
  "id": 1,
  "amount": 100000.00,
  "type": "INCOME",
  "accountId": 1,
  "sourceAccountId": null,
  "destinationAccountId": null
}
```

This approach:
- ✅ Prevents circular references
- ✅ Reduces JSON payload size
- ✅ Provides clean API contracts
- ✅ Keeps entities and API independent

---

## Cascade & Delete Behavior

### Current Configuration (RECOMMENDED)

```java
@OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
// NO CascadeType.REMOVE
private Set<Transaction> transactions = new HashSet<>();
```

**Behavior:**
- ✅ Deleting an Account does NOT delete transactions
- ✅ Transactions keep historical records
- ✅ Transactions become orphaned (FK becomes NULL if allowed) or throw error

**Why?** Financial systems must maintain transaction history for auditing.

### Alternative Configuration (NOT RECOMMENDED for finance)

```java
@OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
private Set<Transaction> transactions = new HashSet<>();
```

**Behavior:**
- ❌ Deleting an Account also deletes all its transactions
- ❌ Loses transaction history
- ❌ Breaks audit trails

---

## Summary Table

| Aspect | INCOME/EXPENSE | TRANSFER |
|--------|----------------|----------|
| **Fields Used** | `account` only | `sourceAccount` + `destinationAccount` |
| **FK Columns** | `account_id` | `source_account_id`, `destination_account_id` |
| **Balance Impact** | Single account updated | Both accounts updated |
| **Query Method** | `findByAccountId()` | `findBySourceAccountId()`, `findByDestinationAccountId()` |
| **Validation** | Account must exist | Both accounts must exist & differ |
| **Example** | Salary, Groceries | Bank to Wallet, Card to Bank |

---

## Best Practices

1. ✅ **Always validate transaction type** before accessing account fields
2. ✅ **Use DTOs** for API responses to prevent circular references
3. ✅ **Use Lazy Loading** to avoid loading unnecessary transaction lists
4. ✅ **Use Sets** instead of Lists for @OneToMany to prevent duplicates
5. ✅ **Never cascade delete** transactions for audit trail preservation
6. ✅ **Log all relationship operations** for debugging
7. ✅ **Use @Transactional** when updating balances atomically
8. ✅ **Keep entity relationships** on the inverse side as optional (queries from Transaction side)

---

## Visual Diagram

```
┌─────────────────┐
│    ACCOUNT 1    │
│  HDFC Bank      │
│  Balance: 125K  │
└────────┬────────┘
         │
         ├──> @OneToMany(mappedBy="account")
         │    └─> Set<Transaction> transactions
         │        ├─ INCOME: +100K
         │        ├─ EXPENSE: -5K
         │        └─ EXPENSE: -2K
         │
         ├──> @OneToMany(mappedBy="sourceAccount")
         │    └─> Set<Transaction> outgoingTransfers
         │        └─ TRANSFER: -20K (to Account 2)
         │
         └──> @OneToMany(mappedBy="destinationAccount")
              └─> Set<Transaction> incomingTransfers
                  (None)

┌─────────────────┐
│    ACCOUNT 2    │
│  Google Pay     │
│  Balance: 20K   │
└────────┬────────┘
         │
         ├──> @OneToMany(mappedBy="account")
         │    └─> Set<Transaction> transactions
         │        (None)
         │
         ├──> @OneToMany(mappedBy="sourceAccount")
         │    └─> Set<Transaction> outgoingTransfers
         │        (None)
         │
         └──> @OneToMany(mappedBy="destinationAccount")
              └─> Set<Transaction> incomingTransfers
                  └─ TRANSFER: +20K (from Account 1)
```

---

## Key Takeaways

1. **Three Foreign Keys** handle three distinct transaction scenarios
2. **Bidirectional Mapping** (optional inverse side) enables convenient querying from Account
3. **DTOs prevent serialization issues** and keep API clean
4. **Validation is critical** to enforce business rules
5. **No cascade delete** preserves audit trail
6. **Lazy loading** with Sets improves performance
7. **Type-based field usage** keeps data integrity high

---

Last Updated: December 28, 2025

