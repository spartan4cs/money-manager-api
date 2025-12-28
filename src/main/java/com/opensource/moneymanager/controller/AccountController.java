package com.opensource.moneymanager.controller;

import com.opensource.moneymanager.dto.AccountDto;
import com.opensource.moneymanager.mapper.AccountStructMapper;
import com.opensource.moneymanager.model.Account;
import com.opensource.moneymanager.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing accounts.
 *
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
     *
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
     *
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
     * GET /api/accounts/by-name/{name}
     *
     * Find an account by its name.
     * Response: 200 OK with AccountDto when found; 404 Not Found otherwise.
     *
     * @param name the account name to search for
     */
    @GetMapping("/by-name/{name}")
    public ResponseEntity<AccountDto> getByName(@PathVariable String name) {
        logger.info("GET /api/accounts/by-name/{} - Fetching account by name", name);
        return service.findByName(name)
            .map(account -> {
                logger.info("Account found by name: id={}, name={}", account.getId(), name);
                return ResponseEntity.ok(mapper.toDto(account));
            })
            .orElseGet(() -> {
                logger.warn("Account not found by name: {}", name);
                return ResponseEntity.notFound().build();
            });
    }

    /**
     * GET /api/accounts/by-type/{type}
     *
     * Returns a list of accounts matching the provided type (e.g. BANK, CASH).
     * Response: 200 OK with an array of AccountDto.
     *
     * @param type the account type filter
     */
    @GetMapping("/by-type/{type}")
    public List<AccountDto> getByType(@PathVariable String type) {
        logger.info("GET /api/accounts/by-type/{} - Fetching accounts by type", type);
        List<AccountDto> accounts = service.findByType(type).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} accounts of type {} to client", accounts.size(), type);
        return accounts;
    }

    /**
     * POST /api/accounts
     *
     * Create a new account from the provided AccountDto. The service is responsible for applying
     * defaults and validations. On success returns 201 Created with Location header.
     *
     * @param dto the account DTO to create
     */
    @PostMapping
    public ResponseEntity<AccountDto> create(@RequestBody AccountDto dto) {
        logger.info("POST /api/accounts - Creating new account: name={}, type={}",
            dto.getName(), dto.getType());

        try {
            Account saved = service.save(mapper.toEntity(dto));
            AccountDto out = mapper.toDto(saved);

            logger.info("Account created successfully with id={}", out.getId());
            return ResponseEntity.created(URI.create("/api/accounts/" + out.getId())).body(out);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create account: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * PUT /api/accounts/{id}
     *
     * Update an existing account identified by id. Only non-null fields in the provided DTO
     * are applied to the existing account. Returns 200 OK with updated AccountDto on success or
     * 404 Not Found if the account does not exist.
     *
     * @param id the id of the account to update
     * @param dto a DTO containing fields to update (non-null fields are applied)
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> update(@PathVariable Long id, @RequestBody AccountDto dto) {
        logger.info("PUT /api/accounts/{} - Updating account", id);

        try {
            return service.findById(id)
                .map(account -> {
                    if (dto.getName() != null) account.setName(dto.getName());
                    if (dto.getDescription() != null) account.setDescription(dto.getDescription());
                    if (dto.getBalance() != null) account.setBalance(dto.getBalance());

                    Account updated = service.save(account);
                    logger.info("Account updated successfully: id={}", id);
                    return ResponseEntity.ok(mapper.toDto(updated));
                })
                .orElseGet(() -> {
                    logger.warn("Account not found for update: id={}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            logger.error("Failed to update account: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * DELETE /api/accounts/{id}
     *
     * Soft-delete an account by marking it inactive. Returns 204 No Content on success or
     * 404 Not Found when the account does not exist.
     *
     * @param id the id of the account to soft-delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/accounts/{} - Deleting account", id);

        try {
            service.deleteById(id);
            logger.info("Account soft deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete account: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
