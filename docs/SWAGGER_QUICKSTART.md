# Swagger Quick Start

## 🚀 Quick Access Links

Once the application is running on `localhost:8080`:

- **Swagger UI (Interactive)**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec (JSON)**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console

---

## ⚡ Start the Application

```bash
cd /Users/akash/Documents/Akash/dev/money-manager-api
./mvnw spring-boot:run
```

---

## 📖 What You'll Find

### In Swagger UI (http://localhost:8080/swagger-ui.html)

Two main sections:
1. **Accounts** - Account management endpoints
2. **Transactions** - Transaction management endpoints

### Key Features
✅ View all endpoints with descriptions  
✅ See request/response schemas  
✅ Try endpoints with "Try it out" button  
✅ Test API directly in the browser  
✅ View response status codes and examples  

---

## 🧪 Example Tests

### Create Account
1. Go to POST `/api/accounts`
2. Click "Try it out"
3. Paste this JSON:
```json
{
  "name": "Checking",
  "type": "BANK",
  "balance": 1000,
  "description": "Main checking"
}
```
4. Click Execute

### Create Expense Transaction
1. Go to POST `/api/transactions`
2. Click "Try it out"
3. Paste this JSON (use account ID from previous step):
```json
{
  "type": "EXPENSE",
  "amount": 50,
  "date": "2024-01-11T10:30:00",
  "description": "Groceries",
  "accountId": 1
}
```
4. Click Execute

---

## 📋 All Available Endpoints

### Accounts API
```
GET    /api/accounts                    - List all
GET    /api/accounts/{id}               - Get by ID
POST   /api/accounts/by-name            - Get by name
POST   /api/accounts/by-type            - Get by type
POST   /api/accounts                    - Create
PUT    /api/accounts/{id}               - Update
DELETE /api/accounts/{id}               - Delete
```

### Transactions API
```
GET    /api/transactions                           - List all
GET    /api/transactions/{id}                      - Get by ID
POST   /api/transactions                           - Create
PUT    /api/transactions/{id}                      - Update
DELETE /api/transactions/{id}                      - Delete
GET    /api/transactions/by-type/{type}            - Get by type
GET    /api/transactions/account/{accountId}       - Get by account
GET    /api/transactions/transfers/from/{id}       - Outgoing transfers
GET    /api/transactions/transfers/to/{id}         - Incoming transfers
```

---

## 🔧 Configuration Files

- **Swagger Config**: `src/main/java/com/opensource/moneymanager/config/SwaggerConfig.java`
- **Properties**: `src/main/resources/application.properties`
- **Full Guide**: `docs/SWAGGER_INTEGRATION.md`

---

## ✅ You're All Set!

Swagger is now integrated and ready to use. Start the application and visit the Swagger UI to explore your API!

