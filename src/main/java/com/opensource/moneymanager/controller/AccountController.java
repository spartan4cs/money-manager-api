package com.opensource.moneymanager.controller;

import com.opensource.moneymanager.dto.AccountDto;
import com.opensource.moneymanager.dto.BalanceDto;
import com.opensource.moneymanager.dto.ErrorResponse;
import com.opensource.moneymanager.mapper.AccountStructMapper;
import com.opensource.moneymanager.model.Account;
import com.opensource.moneymanager.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for managing accounts.
 * <p>
 * Base path: /api/accounts
 * Responsibilities:
 * - Expose CRUD endpoints for Account resources (DTO representation).
 * - Keep controller logic thin: delegate business rules, mapping and persistence to service layer.
 * - Delete is a soft-delete and will mark account as inactive (isActive=false).
 */
@Tag(name = "Accounts", description = "Account management endpoints")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService service;
    private final AccountStructMapper mapper;

    public AccountController(AccountService service, AccountStructMapper mapper) {
        this.service = service;
        this.mapper = mapper;
        logger.info("AccountController initialized");
    }

    /**
     * GET /api/accounts
     * <p>
     * Returns a list of all active accounts (AccountDto).
     * Response: 200 OK with an array of AccountDto.
     */
    @Operation(summary = "List all accounts", description = "Retrieves a list of all active accounts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
            content = @Content(schema = @Schema(implementation = AccountDto.class)))
    @GetMapping
    public List<AccountDto> list() {
        logger.info("GET /api/accounts - Fetching all accounts");
        List<AccountDto> accounts = service.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        logger.info("Returning {} accounts to client", accounts.size());
        return accounts;
    }

    /**
     * GET /api/accounts/{id}
     * <p>
     * Fetch a single account by id (only active accounts are returned).
     * Response: 200 OK with AccountDto when found; 404 Not Found when not present.
     *
     * @param id the account id
     */
    @Operation(summary = "Get account by ID", description = "Retrieves a single account by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> get(@Parameter(description = "Account ID") @PathVariable Long id) {
        logger.info("GET /api/accounts/{} - Fetching account by id", id);
        return service.findById(id)
                .map(account -> {
                    logger.info("Account found: id={}, name={}, type={}", id, account.getName(), account.getType());
                    return ResponseEntity.ok(mapper.toDto(account));
                })
                .orElseGet(() -> {
                    logger.warn("Account not found: id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * GET /api/balance/{accountId}
     * <p>
     * Fetch the balance of an account by its ID. Returns 200 OK with BalanceDto when found,
     * or 404 when not found.
     *
     * @param accountId the account id
     */
    @Operation(summary = "Get account balance", description = "Retrieves the balance information for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BalanceDto.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/balance/{accountId}")
    public ResponseEntity<?> getBalance(@Parameter(description = "Account ID") @PathVariable Long accountId) {
        logger.info("GET /api/balance/{} - Fetching account balance", accountId);
        return service.findById(accountId)
                .map(account -> {
                    logger.info("Account found for balance: id={}, name={}, balance={}",
                        accountId, account.getName(), account.getBalance());
                    BalanceDto balanceDto = new BalanceDto(
                            String.valueOf(account.getId()),
                            account.getBalance(),
                            "USD" // Default currency
                    );
                    return ResponseEntity.ok((Object) balanceDto);
                })
                .orElseGet(() -> {
                    logger.warn("Account not found for balance: id={}", accountId);
                    ErrorResponse errorResponse = new ErrorResponse(
                            HttpStatus.NOT_FOUND.value(),
                            "Account with id " + accountId + " not found",
                            "Not Found",
                            "/api/balance/" + accountId
                    );
                    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
                });
    }

    /**
     * POST /api/accounts/by-name (body)
     * <p>
     * Accepts a JSON body {"name": "..."} to find an account by name. This is provided as a
     * safe alternative to query/path-based lookups when client code cannot easily URL-encode
     * the account name (for example when names contain spaces or other reserved characters).
     * <p>
     * Response: 200 OK with AccountDto when found; 400 Bad Request when name missing; 404 Not Found otherwise.
     *
     * @param body JSON object with a `name` property
     */
    @Operation(summary = "Get account by name", description = "Retrieves an account by its name using request body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - name is missing or empty",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/by-name", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByNameBody(@RequestBody Map<String, String> body) {
        if (body == null) {
            logger.warn("POST /api/accounts/by-name - request body is null");
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Request body cannot be null",
                    "Validation Error",
                    "/api/accounts/by-name"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            logger.warn("POST /api/accounts/by-name - missing or empty 'name' in request body");
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Account name is required and cannot be empty",
                    "Validation Error",
                    "/api/accounts/by-name"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        if (name.length() > 255) {
            logger.warn("POST /api/accounts/by-name - name exceeds maximum length: {}", name.length());
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Account name must not exceed 255 characters",
                    "Validation Error",
                    "/api/accounts/by-name"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        logger.info("POST /api/accounts/by-name - Fetching account by name (body): {}", name);
        return service.findByName(name)
                .map(account -> ResponseEntity.ok((Object) mapper.toDto(account)))
                .orElseGet(() -> {
                    ErrorResponse errorResponse = new ErrorResponse(
                            HttpStatus.NOT_FOUND.value(),
                            "Account with name '" + name + "' not found",
                            "Not Found",
                            "/api/accounts/by-name"
                    );
                    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
                });
    }

    /**
     * POST /api/accounts/by-type (body)
     * <p>
     * Accepts a JSON body {"type": "..."} to find accounts by type. This is provided as a
     * safe alternative to query/path-based lookups when client code cannot easily URL-encode
     * the account type (for example when type contains special characters).
     * <p>
     * Response: 200 OK with an array of AccountDto; 400 Bad Request when type missing.
     *
     * @param body JSON object with a `type` property
     */
    @Operation(summary = "Get accounts by type", description = "Retrieves all accounts of a specific type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - type is missing, empty, or invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/by-type", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByTypeBody(@RequestBody Map<String, String> body) {
        if (body == null) {
            logger.warn("POST /api/accounts/by-type - request body is null");
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Request body cannot be null",
                    "Validation Error",
                    "/api/accounts/by-type"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        String type = body.get("type");
        if (type == null || type.trim().isEmpty()) {
            logger.warn("POST /api/accounts/by-type - missing or empty 'type' in request body");
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Account type is required and cannot be empty",
                    "Validation Error",
                    "/api/accounts/by-type"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        if (!type.matches("^(BANK|CREDIT_CARD|DEBIT_CARD|E_WALLET|CASH|SAVINGS|INVESTMENT)$")) {
            logger.warn("POST /api/accounts/by-type - invalid type: {}", type);
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Type must be one of: BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT",
                    "Validation Error",
                    "/api/accounts/by-type"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        logger.info("POST /api/accounts/by-type - Fetching accounts by type (body): {}", type);
        List<AccountDto> accounts = service.findByType(type).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    /**
     * POST /api/accounts
     * <p>
     * Create a new account from the provided AccountDto. The service is responsible for applying
     * defaults and validations. On success returns 201 Created with Location header.
     * Returns 400 Bad Request with error details on validation failure.
     *
     * @param dto the account DTO to create
     */
    @Operation(summary = "Create a new account", description = "Creates a new account with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AccountDto dto) {
        logger.info("POST /api/accounts - Creating new account: name={}, type={}",
                dto.getName(), dto.getType());

        try {
            Account saved = service.save(mapper.toEntity(dto));
            AccountDto out = mapper.toDto(saved);

            logger.info("Account created successfully with id={}", out.getId());
            return ResponseEntity.created(URI.create("/api/accounts/" + out.getId())).body(out);
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
    }

    /**
     * PUT /api/accounts/{id}
     * <p>
     * Update an existing account identified by id. Only non-null fields in the provided DTO
     * are applied to the existing account. Returns 200 OK with updated AccountDto on success,
     * 404 Not Found if the account does not exist, or 400 Bad Request on validation failure.
     *
     * @param id  the id of the account to update
     * @param dto a DTO containing fields to update (non-null fields are applied)
     */
    @Operation(summary = "Update an account", description = "Updates an existing account with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Parameter(description = "Account ID") @PathVariable Long id,
                                    @Valid @RequestBody AccountDto dto) {
        logger.info("PUT /api/accounts/{} - Updating account", id);

        try {
            return service.findById(id)
                    .map(account -> {
                        if (dto.getName() != null) account.setName(dto.getName());
                        if (dto.getDescription() != null) account.setDescription(dto.getDescription());
                        if (dto.getBalance() != null) account.setBalance(dto.getBalance());

                        try {
                            Account updated = service.save(account);
                            logger.info("Account updated successfully: id={}", id);
                            return ResponseEntity.ok(mapper.toDto(updated));
                        } catch (IllegalArgumentException e) {
                            logger.error("Failed to update account: {}", e.getMessage());
                            ErrorResponse errorResponse = new ErrorResponse(
                                    HttpStatus.BAD_REQUEST.value(),
                                    e.getMessage(),
                                    "Validation Error",
                                    "/api/accounts/" + id
                            );
                            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                        }
                    })
                    .orElseGet(() -> {
                        logger.warn("Account not found for update: id={}", id);
                        ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Account with id " + id + " not found",
                                "Not Found",
                                "/api/accounts/" + id
                        );
                        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
                    });
        } catch (Exception e) {
            logger.error("Unexpected error during update: {}", e.getMessage(), e);
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred while updating the account",
                    e.getClass().getSimpleName(),
                    "/api/accounts/" + id
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DELETE /api/accounts/{id}
     * <p>
     * Soft-delete an account by marking it inactive. Returns 204 No Content on success,
     * 404 Not Found when the account does not exist, or 500 on unexpected errors.
     *
     * @param id the id of the account to soft-delete
     */
    @Operation(summary = "Delete an account", description = "Soft-deletes an account by marking it as inactive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "Account ID") @PathVariable Long id) {
        logger.info("DELETE /api/accounts/{} - Deleting account", id);

        try {
            service.deleteById(id);
            logger.info("Account soft deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete account: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage(),
                    "Not Found",
                    "/api/accounts/" + id
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error during delete: {}", e.getMessage(), e);
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred while deleting the account",
                    e.getClass().getSimpleName(),
                    "/api/accounts/" + id
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/accounts/types/available
     * <p>
     * Returns a list of all available account types for UI dropdowns/selection.
     * Response: 200 OK with an array of account type strings.
     */
    @Operation(summary = "Get available account types", description = "Retrieves all available account types for UI selection")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved account types",
            content = @Content(schema = @Schema(implementation = String.class)))
    @GetMapping("/types/available")
    public ResponseEntity<List<String>> getAvailableTypes() {
        logger.info("GET /api/accounts/types/available - Fetching available account types");
        List<String> types = List.of("BANK", "CREDIT_CARD", "DEBIT_CARD", "E_WALLET", "CASH", "SAVINGS", "INVESTMENT");
        logger.info("Returning {} account types", types.size());
        return ResponseEntity.ok(types);
    }



}
