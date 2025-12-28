package com.opensource.moneymanager.controller;

import com.opensource.moneymanager.dto.AccountDto;
import com.opensource.moneymanager.mapper.AccountMapper;
import com.opensource.moneymanager.model.Account;
import com.opensource.moneymanager.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
        logger.info("AccountController initialized");
    }

    @GetMapping
    public List<AccountDto> list() {
        logger.info("GET /api/accounts - Fetching all accounts");
        List<AccountDto> accounts = service.findAll().stream()
            .map(AccountMapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} accounts to client", accounts.size());
        return accounts;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> get(@PathVariable Long id) {
        logger.info("GET /api/accounts/{} - Fetching account by id", id);
        return service.findById(id)
            .map(account -> {
                logger.info("Account found: id={}, name={}, type={}", id, account.getName(), account.getType());
                return ResponseEntity.ok(AccountMapper.toDto(account));
            })
            .orElseGet(() -> {
                logger.warn("Account not found: id={}", id);
                return ResponseEntity.notFound().build();
            });
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<AccountDto> getByName(@PathVariable String name) {
        logger.info("GET /api/accounts/by-name/{} - Fetching account by name", name);
        return service.findByName(name)
            .map(account -> {
                logger.info("Account found by name: id={}, name={}", account.getId(), name);
                return ResponseEntity.ok(AccountMapper.toDto(account));
            })
            .orElseGet(() -> {
                logger.warn("Account not found by name: {}", name);
                return ResponseEntity.notFound().build();
            });
    }

    @GetMapping("/by-type/{type}")
    public List<AccountDto> getByType(@PathVariable String type) {
        logger.info("GET /api/accounts/by-type/{} - Fetching accounts by type", type);
        List<AccountDto> accounts = service.findByType(type).stream()
            .map(AccountMapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} accounts of type {} to client", accounts.size(), type);
        return accounts;
    }

    @PostMapping
    public ResponseEntity<AccountDto> create(@RequestBody AccountDto dto) {
        logger.info("POST /api/accounts - Creating new account: name={}, type={}",
            dto.getName(), dto.getType());

        try {
            Account saved = service.save(AccountMapper.toEntity(dto));
            AccountDto out = AccountMapper.toDto(saved);

            logger.info("Account created successfully with id={}", out.getId());
            return ResponseEntity.created(URI.create("/api/accounts/" + out.getId())).body(out);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create account: {}", e.getMessage());
            throw e;
        }
    }

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
                    return ResponseEntity.ok(AccountMapper.toDto(updated));
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

