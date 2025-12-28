# Error Handling Architecture

## Overview
The error handling system follows a layered architecture to ensure consistent error responses across the entire API.

```
┌─────────────────────────────────────────────────────────────┐
│                     HTTP Client Request                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
        ┌────────────────────────────────┐
        │    AccountController / etc      │ (REST Endpoints)
        │                                │
        │  • Validates input             │
        │  • Calls service methods       │
        │  • Handles responses           │
        └────────┬───────────────────────┘
                 │
        ┌────────▼─────────────┐
        │  AccountService      │ (Business Logic)
        │                      │
        │  • Validates rules   │
        │  • Checks uniqueness │
        │  • Throws exceptions │
        └────────┬─────────────┘
                 │
    ┌────────────▼────────────────┐
    │  IllegalArgumentException   │ (Validation Error)
    │  or Other Exceptions        │
    └────────────┬────────────────┘
                 │
                 ▼
    ┌─────────────────────────────────┐
    │ GlobalExceptionHandler          │ (Centralized)
    │ @RestControllerAdvice           │
    │                                 │
    │ • Catches all exceptions        │
    │ • Formats as ErrorResponse      │
    │ • Returns JSON with status code │
    └────────────┬────────────────────┘
                 │
                 ▼
        ┌────────────────────────┐
        │   ErrorResponse DTO    │
        │                        │
        │  • status (int)        │
        │  • message (String)    │
        │  • error (String)      │
        │  • timestamp           │
        │  • path (String)       │
        └────────────┬───────────┘
                     │
                     ▼
        ┌────────────────────────┐
        │   HTTP Response JSON   │
        │   with status code     │
        │   (400/404/500/etc)    │
        └────────────────────────┘
```

---

## Layer Responsibilities

### 1. Controller Layer
**Files:** `AccountController.java`, `TransactionController.java`

**Responsibilities:**
- Accept HTTP requests
- Validate request parameters
- Call service layer methods
- Catch `IllegalArgumentException` and return 400 Bad Request
- Convert DTOs to entities and entities to DTOs
- Return HTTP responses with appropriate status codes

**Error Handling:**
```java
try {
    Account saved = service.save(mapper.toEntity(dto));
    return ResponseEntity.created(...).body(out);
} catch (IllegalArgumentException e) {
    logger.error("Failed to create account: {}", e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        e.getMessage(),
        "Validation Error",
        "/api/accounts"
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
}
```

---

### 2. Service Layer
**Files:** `AccountService.java`, `TransactionService.java`

**Responsibilities:**
- Implement business logic
- Validate entities according to business rules
- Check unique constraints (e.g., duplicate account names)
- Update related entities (e.g., account balances for transactions)
- Throw `IllegalArgumentException` with descriptive messages for validation failures

**Error Handling:**
```java
public Account save(Account a) {
    // Validate name exists
    if (a.getName() == null || a.getName().trim().isEmpty()) {
        throw new IllegalArgumentException("Account name cannot be null or empty");
    }
    
    // Check for duplicate (excluding current record if updating)
    Optional<Account> existingAccount = repository.findByName(a.getName());
    if (existingAccount.isPresent()) {
        if (a.getId() == null || !existingAccount.get().getId().equals(a.getId())) {
            throw new IllegalArgumentException(
                "An account with name '" + a.getName() + "' already exists"
            );
        }
    }
    
    return repository.save(a);
}
```

---

### 3. Global Exception Handler
**File:** `GlobalExceptionHandler.java`

**Responsibilities:**
- Catch all exceptions not handled at controller level
- Format exceptions as standard `ErrorResponse` DTOs
- Return appropriate HTTP status codes
- Log errors for debugging
- Prevent exception stack traces from leaking to clients

**Exception Handling:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            "Validation Error",
            getPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.",
            ex.getClass().getSimpleName(),
            getPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

---

### 4. Error Response DTO
**File:** `ErrorResponse.java`

**Structure:**
```java
public class ErrorResponse {
    private int status;              // HTTP status code
    private String message;          // User-friendly error message
    private String error;            // Error type (Validation Error, Not Found, etc.)
    private LocalDateTime timestamp; // When the error occurred
    private String path;             // Request path that caused the error
}
```

**Example Response:**
```json
{
  "status": 400,
  "message": "An account with name 'HDFC Bank' already exists",
  "error": "Validation Error",
  "timestamp": "2025-12-28T10:30:15",
  "path": "/api/accounts"
}
```

---

## Error Flow Diagram

### Scenario 1: Validation Error in Service
```
POST /api/accounts
    ↓
[Create Account Request]
    ↓
AccountController.create()
    ├─ mapper.toEntity(dto)
    ├─ service.save(account)
    │   ├─ Check if name is null/empty → ✗
    │   └─ throw IllegalArgumentException("Account name cannot be null or empty")
    │
    ├─ Catch IllegalArgumentException
    └─ Return 400 with ErrorResponse
        {
          "status": 400,
          "message": "Account name cannot be null or empty",
          "error": "Validation Error",
          "timestamp": "...",
          "path": "/api/accounts"
        }
```

### Scenario 2: Duplicate Name Error
```
POST /api/accounts (name = "HDFC Bank")
    ↓
[Create Account Request]
    ↓
AccountController.create()
    ├─ mapper.toEntity(dto)
    ├─ service.save(account)
    │   ├─ Check if name is empty → ✓
    │   ├─ Check if account with name exists
    │   │   └─ repository.findByName("HDFC Bank") → Found!
    │   └─ throw IllegalArgumentException("An account with name 'HDFC Bank' already exists")
    │
    ├─ Catch IllegalArgumentException
    └─ Return 400 with ErrorResponse
        {
          "status": 400,
          "message": "An account with name 'HDFC Bank' already exists",
          "error": "Validation Error",
          "timestamp": "...",
          "path": "/api/accounts"
        }
```

### Scenario 3: Resource Not Found
```
GET /api/accounts/999
    ↓
[Fetch Account Request]
    ↓
AccountController.get(999)
    ├─ service.findById(999)
    │   └─ Optional.empty()
    │
    └─ orElseGet(() -> ResponseEntity.notFound().build())
        → 404 Not Found (no body)
```

### Scenario 4: Unexpected Exception
```
[Any Request]
    ↓
[Exception occurs in service/repository - not caught]
    ↓
GlobalExceptionHandler.handleGenericException()
    ├─ Log error with stack trace
    └─ Return 500 with ErrorResponse
        {
          "status": 500,
          "message": "An unexpected error occurred. Please try again later.",
          "error": "DatabaseException",
          "timestamp": "...",
          "path": "/api/accounts"
        }
```

---

## Benefits of This Architecture

### 1. **Separation of Concerns**
- Controllers handle HTTP
- Services handle business logic
- Exception handler handles error formatting

### 2. **Consistency**
- All errors follow the same JSON format
- All errors include status code, message, type, timestamp, path
- Clients can rely on consistent error structure

### 3. **Client-Friendly**
- Clear, actionable error messages
- No technical stack traces exposed
- Specific error types help clients handle errors appropriately

### 4. **Debugging**
- Full stack traces logged on server side
- Request path recorded for tracking issues
- Timestamp helps correlate with logs

### 5. **Maintainability**
- New exceptions types can be added to GlobalExceptionHandler
- Changes to error format only need updates in ErrorResponse and GlobalExceptionHandler
- Service layer focuses on business rules, not error formatting

---

## Adding New Error Types

To handle a new exception type:

1. **Add handler to GlobalExceptionHandler:**
```java
@ExceptionHandler(YourCustomException.class)
public ResponseEntity<ErrorResponse> handleYourCustomException(
        YourCustomException ex, WebRequest request) {
    logger.warn("Custom error: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.UNPROCESSABLE_ENTITY.value(),  // 422
        ex.getMessage(),
        "Custom Error Type",
        getPath(request)
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
}
```

2. **Throw from service layer:**
```java
if (someCondition) {
    throw new YourCustomException("Descriptive message");
}
```

3. **Optional: Catch in controller for specific handling:**
```java
catch (YourCustomException e) {
    // Handle specifically if needed
}
```

---

## Testing the Error Handling

See `ERROR_HANDLING_TEST_GUIDE.md` for comprehensive testing scenarios and expected responses.

