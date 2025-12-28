# Quick Reference - Error Handling

## Files Changed

| File | Changes |
|------|---------|
| `AccountController.java` | ✅ Added error handling to all endpoints |
| `TransactionController.java` | ✅ Added error handling to POST and DELETE |
| `AccountService.java` | ✅ Added unique name validation |
| `ErrorResponse.java` | ✨ NEW - Error response DTO |
| `GlobalExceptionHandler.java` | ✨ NEW - Global exception handler |

## Error Response Format

```json
{
  "status": <HTTP_CODE>,
  "message": "<Human-readable message>",
  "error": "<Error type>",
  "timestamp": "<ISO-8601 date>",
  "path": "<Request path>"
}
```

## Common Error Responses

### 400 Bad Request - Duplicate Name
```json
{
  "status": 400,
  "message": "An account with name 'HDFC Bank' already exists",
  "error": "Validation Error",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/accounts"
}
```

### 400 Bad Request - Missing Field
```json
{
  "status": 400,
  "message": "Account name is required",
  "error": "Validation Error",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/accounts/by-name"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "Account with id 999 not found",
  "error": "Not Found",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/accounts/999"
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "An unexpected error occurred. Please try again later.",
  "error": "DatabaseException",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/accounts"
}
```

## API Endpoints - Error Handling

### Accounts
| Method | Endpoint | Success | Error |
|--------|----------|---------|-------|
| POST | /api/accounts | 201 Created | 400 Bad Request |
| GET | /api/accounts/{id} | 200 OK | 404 Not Found |
| PUT | /api/accounts/{id} | 200 OK | 400/404 |
| DELETE | /api/accounts/{id} | 204 No Content | 404 Not Found |
| POST | /api/accounts/by-name | 200 OK | 400/404 |
| POST | /api/accounts/by-type | 200 OK | 400 |

### Transactions
| Method | Endpoint | Success | Error |
|--------|----------|---------|-------|
| POST | /api/transactions | 201 Created | 400 Bad Request |
| GET | /api/transactions/{id} | 200 OK | 404 Not Found |
| DELETE | /api/transactions/{id} | 204 No Content | 404 Not Found |
| GET | /api/transactions/by-type/{type} | 200 OK | - |
| GET | /api/transactions/account/{accountId} | 200 OK | - |
| GET | /api/transactions/transfers/from/{sourceAccountId} | 200 OK | - |
| GET | /api/transactions/transfers/to/{destAccountId} | 200 OK | - |

## Validation Rules

### Account Creation (POST /api/accounts)
✅ Must have: name (non-empty), type
❌ Cannot have: duplicate name
❌ Cannot be: null name/type

### Account Update (PUT /api/accounts/{id})
✅ Can update: name, description, balance
❌ Cannot change: type to duplicate name
✅ Can keep: same name (allows updating other fields)

### Account Lookup
✅ By name: POST with {"name": "..."}
❌ By name: empty string returns 400
❌ By name: non-existent returns 404
✅ By type: POST with {"type": "..."}
❌ By type: empty string returns 400

### Transaction Creation (POST /api/transactions)
✅ Must have: type (INCOME/EXPENSE/TRANSFER), amount
✅ INCOME/EXPENSE: must have accountId
✅ TRANSFER: must have sourceAccountId and destinationAccountId
❌ Cannot have: null amount
❌ Cannot have: non-existent accounts

## Testing with cURL

### Create Account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"name":"My Account","type":"BANK","balance":1000}'
```

### Try Duplicate
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"name":"My Account","type":"BANK","balance":2000}'
# Returns 400 Bad Request with "already exists" message
```

### Update Account
```bash
curl -X PUT http://localhost:8080/api/accounts/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"My Account","description":"Main account"}'
```

### Find by Name
```bash
curl -X POST http://localhost:8080/api/accounts/by-name \
  -H "Content-Type: application/json" \
  -d '{"name":"My Account"}'
```

### Delete Account
```bash
curl -X DELETE http://localhost:8080/api/accounts/1
# Returns 204 No Content on success
# Returns 404 with error message if not found
```

## Logging

All errors are logged with context:
- ✅ Logged in controllers with request details
- ✅ Logged in services with business logic details
- ✅ Logged in GlobalExceptionHandler with full stack trace
- ✅ All logs include timestamps for debugging

## Migration from Old Behavior

### Old Behavior
```
POST /api/accounts (duplicate name)
→ Uncaught exception
→ 500 Internal Server Error
→ No response body
```

### New Behavior
```
POST /api/accounts (duplicate name)
→ Service throws IllegalArgumentException
→ Controller catches and formats
→ 400 Bad Request
→ Response body with clear message:
  "An account with name 'X' already exists"
```

## Benefits Summary

| Aspect | Before | After |
|--------|--------|-------|
| Error Format | Inconsistent | Standardized JSON |
| Status Codes | Wrong | Correct (400/404/500) |
| Messages | Generic | Specific & actionable |
| Debugging | Stack traces exposed | Full logs on server |
| Client Experience | Confusing | Clear & professional |
| Maintainability | Scattered | Centralized |
| Validation | Missing | Comprehensive |
| Unique Names | No validation | Enforced |

## Need More Info?

- 📋 See `ERROR_HANDLING_IMPROVEMENTS.md` for detailed changes
- 🏗️ See `ERROR_HANDLING_ARCHITECTURE.md` for system design
- 🧪 See `ERROR_HANDLING_TEST_GUIDE.md` for testing examples
- 📝 See `CHANGES_SUMMARY.md` for overview

