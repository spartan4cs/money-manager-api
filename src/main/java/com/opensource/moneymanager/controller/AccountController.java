package com.opensource.moneymanager.controller;

import com.opensource.moneymanager.dto.AccountDto;
import com.opensource.moneymanager.dto.ErrorResponse;
import com.opensource.moneymanager.mapper.AccountStructMapper;
import com.opensource.moneymanager.model.Account;
import com.opensource.moneymanager.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
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
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> get(@PathVariable Long id) {
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
    @PostMapping(value = "/by-name", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByNameBody(@RequestBody Map<String, String> body) {
        String name = body != null ? body.get("name") : null;
        if (name == null || name.trim().isEmpty()) {
            logger.warn("POST /api/accounts/by-name - missing 'name' in request body");
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Account name is required",
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
    @PostMapping(value = "/by-type", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByTypeBody(@RequestBody Map<String, String> body) {
        String type = body != null ? body.get("type") : null;
        if (type == null || type.trim().isEmpty()) {
            logger.warn("POST /api/accounts/by-type - missing 'type' in request body");
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Account type is required",
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
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AccountDto dto) {
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
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AccountDto dto) {
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
                            return (ResponseEntity<?>) ResponseEntity.ok(mapper.toDto(updated));
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
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


}
