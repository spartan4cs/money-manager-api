# Swagger Integration Guide

## Overview
Swagger/OpenAPI has been successfully integrated into the Money Manager API. This document explains how to access and use the Swagger UI for API documentation and testing.

## Accessing the Swagger UI

Once the application is running on `http://localhost:8080`, you can access the API documentation through:

### Swagger UI
- **URL**: `http://localhost:8080/swagger-ui.html`
- Interactive interface to view and test all API endpoints
- Organized by resource tags (Accounts, Transactions)
- Click on any endpoint to see:
  - Request parameters and body schema
  - Response schemas
  - HTTP status codes
  - Example values

### OpenAPI JSON
- **URL**: `http://localhost:8080/api-docs`
- Machine-readable OpenAPI 3.0 specification
- Useful for code generation and integration with other tools

## Key Features

### 1. **API Documentation**
All endpoints are documented with:
- Clear operation summaries
- Detailed descriptions
- Request/response schemas
- HTTP status codes
- Error response examples

### 2. **Organized by Tags**
Endpoints are grouped into logical sections:
- **Accounts**: Account management operations (GET, POST, PUT, DELETE)
- **Transactions**: Transaction management operations

### 3. **Try It Out**
The Swagger UI allows you to test endpoints directly:
1. Click on an endpoint to expand it
2. Click the "Try it out" button
3. Fill in required parameters
4. Click "Execute" to send the request
5. View the response and status code

## Available Endpoints

### Account Endpoints
- `GET /api/accounts` - List all active accounts
- `GET /api/accounts/{id}` - Get account by ID
- `POST /api/accounts/by-name` - Get account by name
- `POST /api/accounts/by-type` - Get accounts by type
- `POST /api/accounts` - Create new account
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Soft-delete account

### Transaction Endpoints
- `GET /api/transactions` - List all transactions
- `GET /api/transactions/{id}` - Get transaction by ID
- `POST /api/transactions` - Create new transaction
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction
- `GET /api/transactions/by-type/{type}` - Get transactions by type
- `GET /api/transactions/account/{accountId}` - Get transactions by account
- `GET /api/transactions/transfers/from/{sourceAccountId}` - Get outgoing transfers
- `GET /api/transactions/transfers/to/{destAccountId}` - Get incoming transfers

## Testing Examples

### 1. Create an Account
```json
POST /api/accounts
Content-Type: application/json

{
  "name": "My Checking Account",
  "type": "BANK",
  "balance": 1000.00,
  "description": "Primary checking account"
}
```

### 2. Create a Transaction
```json
POST /api/transactions
Content-Type: application/json

{
  "type": "EXPENSE",
  "amount": 50.00,
  "date": "2024-01-11T10:30:00",
  "description": "Groceries",
  "accountId": 1
}
```

### 3. Transfer Between Accounts
```json
POST /api/transactions
Content-Type: application/json

{
  "type": "TRANSFER",
  "amount": 500.00,
  "date": "2024-01-11T10:30:00",
  "description": "Transfer to savings",
  "sourceAccountId": 1,
  "destinationAccountId": 2
}
```

## Configuration

The Swagger configuration is located in:
- **Configuration Class**: `SwaggerConfig.java`
- **Properties**: `application.properties`

### Customizing API Metadata
Edit `src/main/java/com/opensource/moneymanager/config/SwaggerConfig.java`:
- API title and version
- API description
- Contact information
- License details

### Springdoc Properties
In `application.properties`:
```properties
# API documentation endpoint
springdoc.api-docs.path=/api-docs

# Swagger UI endpoint
springdoc.swagger-ui.path=/swagger-ui.html

# Enable/disable Swagger UI
springdoc.swagger-ui.enabled=true

# Sort operations by HTTP method
springdoc.swagger-ui.operations-sorter=method

# Sort tags alphabetically
springdoc.swagger-ui.tags-sorter=alpha
```

## Dependencies

The following Maven dependency was added:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

This includes both:
- Springdoc OpenAPI core (generates OpenAPI spec)
- Swagger UI (web interface)

## Integration with External Tools

### 1. Postman
- Import the OpenAPI spec from `http://localhost:8080/api-docs`
- Postman will automatically create collections for all endpoints

### 2. Code Generation
- Use tools like OpenAPI Generator to generate client SDKs
- Pass the OpenAPI JSON: `http://localhost:8080/api-docs`

### 3. API Client Libraries
- Generate TypeScript, Java, Python, Go client libraries
- Use the OpenAPI specification as the source

## Troubleshooting

### Swagger UI not loading
1. Ensure the application is running on port 8080
2. Check that `springdoc-openapi-ui` is in the Maven dependencies
3. Rebuild and restart the application

### Endpoints not showing up
1. Ensure controllers have `@RestController` or `@Controller` annotations
2. Verify `@RequestMapping` is present
3. Check that Swagger annotations are properly added

### CORS Issues
If consuming Swagger UI from different origin, enable CORS in your application or configure Springdoc properties.

## Next Steps

1. Start the application: `./mvnw spring-boot:run`
2. Open browser and navigate to `http://localhost:8080/swagger-ui.html`
3. Explore and test all available endpoints
4. Use the interactive "Try it out" feature to validate your API

## References

- [Springdoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)

