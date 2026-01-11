# What to Expect After Integration

## 🎨 Swagger UI Interface

When you visit `http://localhost:8080/swagger-ui.html`, you'll see:

### Top Section
```
Money Manager API (v1.0.0)
REST API for managing accounts and transactions. Provides comprehensive 
endpoints for account management, transaction tracking, and financial analytics.

Contact: Money Manager Team (support@moneymanager.com)
License: Apache 2.0
```

### Two Main Sections (Expandable)

#### 1. Accounts
```
GET     /api/accounts                    List all accounts
GET     /api/accounts/{id}               Get account by ID
POST    /api/accounts/by-name            Get account by name
POST    /api/accounts/by-type            Get accounts by type
POST    /api/accounts                    Create a new account
PUT     /api/accounts/{id}               Update an account
DELETE  /api/accounts/{id}               Delete an account
```

#### 2. Transactions
```
GET     /api/transactions                List all transactions
GET     /api/transactions/{id}           Get transaction by ID
POST    /api/transactions                Create a new transaction
PUT     /api/transactions/{id}           Update a transaction
DELETE  /api/transactions/{id}           Delete a transaction
GET     /api/transactions/by-type/{type}       Get transactions by type
GET     /api/transactions/account/{accountId}  Get transactions by account
GET     /api/transactions/transfers/from/{sourceAccountId}  Get outgoing transfers
GET     /api/transactions/transfers/to/{destAccountId}      Get incoming transfers
```

---

## 🔍 When You Click an Endpoint

### Example: POST /api/accounts (Create Account)

You'll see:
- **Method**: POST
- **Path**: /api/accounts
- **Summary**: "Create a new account"
- **Description**: "Creates a new account with the provided details"
- **Request Body**: 
  ```json
  {
    "name": "string (required)",
    "type": "string (required) - BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT",
    "balance": "number",
    "description": "string"
  }
  ```
- **Responses**:
  - 201 Created - AccountDto
  - 400 Bad Request - ErrorResponse

- **Try it out** button at the bottom

---

## 🧪 How to Test an Endpoint

### Step-by-Step Example: Create an Account

1. Find "POST /api/accounts" endpoint
2. Click on it to expand
3. Click blue **"Try it out"** button
4. Edit the request body JSON:
   ```json
   {
     "name": "My Savings",
     "type": "SAVINGS",
     "balance": 5000,
     "description": "Emergency fund"
   }
   ```
5. Click **"Execute"** button
6. See the response:
   ```json
   {
     "id": 1,
     "name": "My Savings",
     "type": "SAVINGS",
     "balance": 5000.00,
     "description": "Emergency fund",
     "isActive": true
   }
   ```
7. Response code: **201 Created**

---

## 📋 Response Details You'll See

Each endpoint shows:

### For Success Responses
```
Response Code: 200, 201, 204
Response Body: 
- Full schema
- Example values
- Data types
- Required/optional fields
```

### For Error Responses
```
Response Code: 400, 404, 500
Error Format:
{
  "status": 400,
  "message": "Validation Error",
  "errorType": "Validation Error",
  "path": "/api/accounts"
}
```

---

## 🔗 Parameter Documentation

### When you click on an endpoint with parameters:

Example: GET /api/accounts/{id}

You'll see:
```
Parameters:
- id (path)
  Description: Account ID
  Type: integer (int64)
  Required: yes
  
Example: 1
```

---

## 💾 Useful Features in Swagger UI

### 1. **Models Section** (at bottom)
Shows all data models:
- AccountDto schema
- TransactionDto schema
- ErrorResponse schema
- Enum types

### 2. **Authorization** (if needed)
Would show OAuth2, API Key, etc.

### 3. **Download Spec**
- Download as JSON/YAML
- For use with code generators
- For sharing with team

### 4. **API Calls History**
- Previous requests stored
- Quick re-execute
- Useful for testing sequences

---

## 🎯 Common Testing Workflows

### Workflow 1: Account Management
```
1. POST /api/accounts          → Create account (get ID)
2. GET /api/accounts/{id}      → Verify creation
3. PUT /api/accounts/{id}      → Update account
4. GET /api/accounts           → List all
5. DELETE /api/accounts/{id}   → Delete account
```

### Workflow 2: Transactions with Transfer
```
1. POST /api/accounts          → Create account 1
2. POST /api/accounts          → Create account 2
3. POST /api/transactions      → Create TRANSFER (between accounts)
4. GET /api/transactions/{id}  → View transfer details
5. GET /api/transactions/transfers/from/{id}  → View outgoing
6. GET /api/transactions/transfers/to/{id}    → View incoming
```

### Workflow 3: Account Operations
```
1. POST /api/accounts          → Create account
2. POST /api/accounts/by-name  → Find by name
3. POST /api/accounts/by-type  → Find by type
4. GET /api/transactions/account/{id}  → Get transactions
```

---

## 🌐 Other Useful URLs

Once application is running:

| URL | Purpose |
|-----|---------|
| http://localhost:8080 | Application home (may redirect to Swagger) |
| http://localhost:8080/swagger-ui.html | Interactive API docs |
| http://localhost:8080/api-docs | OpenAPI JSON spec |
| http://localhost:8080/h2-console | H2 database console |

---

## 📱 Mobile Access

Swagger UI is responsive:
- Works on desktop
- Works on tablets
- Works on mobile phones
- Touch-friendly interface

---

## 🔐 Security Notes

In Swagger UI you can:
- View all endpoints publicly
- Test without authentication (for now)
- See all parameter requirements
- See all response formats

If you add authentication later:
- Add "Authorize" button in Swagger config
- Tokens can be provided in UI
- Tests will include auth headers

---

## ❌ Troubleshooting Display Issues

### If endpoints don't show:
1. Restart the application
2. Hard refresh browser (Cmd+Shift+R on Mac)
3. Clear browser cache
4. Try incognito/private mode

### If responses show as errors:
1. Check server logs
2. Verify database is running (H2 is in-memory, auto-created)
3. Try creating resource first
4. Check request body format

### If Swagger UI is blank:
1. Check browser console for errors
2. Verify port 8080 is accessible
3. Check application startup logs
4. Verify all dependencies loaded

---

## 🎓 Learning Resources

From Swagger UI, you can learn:
- Exact endpoint paths
- Required vs optional parameters
- Data types and formats
- Response structures
- Error handling
- Pagination (if implemented)
- Filtering options

---

## ✅ You're Ready!

Everything is set up. Start the app and explore the API through the beautiful Swagger UI interface. Happy testing!

