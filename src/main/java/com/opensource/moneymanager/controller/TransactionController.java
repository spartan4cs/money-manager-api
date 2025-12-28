package com.opensource.moneymanager.controller;

import com.opensource.moneymanager.dto.TransactionDto;
import com.opensource.moneymanager.dto.ErrorResponse;
import com.opensource.moneymanager.mapper.TransactionStructMapper;
import com.opensource.moneymanager.model.Transaction;
import com.opensource.moneymanager.service.TransactionService;
import com.opensource.moneymanager.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing transactions.
 *
 * Base path: /api/transactions
 * Responsibilities:
 * - Expose endpoints to create, read, and delete transactions (DTO representation).
 * - Keep controllers thin: delegate mapping and business rules to services.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService service;
    private final AccountService accountService;
    private final TransactionStructMapper mapper;

    public TransactionController(TransactionService service, AccountService accountService, TransactionStructMapper mapper) {
        this.service = service;
        this.accountService = accountService;
        this.mapper = mapper;
        logger.info("TransactionController initialized");
    }

    /**
     * GET /api/transactions
     *
     * Returns all transactions as TransactionDto list.
     * Response: 200 OK with an array of TransactionDto.
     */
    @GetMapping
    public List<TransactionDto> list() {
        logger.info("GET /api/transactions - Fetching all transactions");
        List<TransactionDto> transactions = service.findAll().stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transactions to client", transactions.size());
        return transactions;
    }

    /**
     * GET /api/transactions/{id}
     *
     * Fetch a transaction by id. Returns 200 OK with TransactionDto when found, or 404 when not.
     *
     * @param id the transaction id
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> get(@PathVariable Long id) {
        logger.info("GET /api/transactions/{} - Fetching transaction by id", id);
        return service.findById(id)
            .map(transaction -> {
                logger.info("Transaction found: id={}, type={}", id, transaction.getType());
                return ResponseEntity.ok(mapper.toDto(transaction));
            })
            .orElseGet(() -> {
                logger.warn("Transaction not found: id={}", id);
                return ResponseEntity.notFound().build();
            });
    }

    /**
     * POST /api/transactions
     *
     * Create a new transaction from the provided TransactionDto. The controller will map DTO->entity,
     * attach account relationships using the AccountService, and then delegate to the transaction service
     * which will persist and update account balances accordingly.
     *
     * Response: 201 Created with Location header and created TransactionDto body on success.
     * Response: 400 Bad Request with error details on validation failure.
     *
     * @param dto the transaction DTO containing amount, type and account id references
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TransactionDto dto) {
        logger.info("POST /api/transactions - Creating new transaction: type={}, amount={}",
            dto.getType(), dto.getAmount());

        try {
            Transaction t = mapper.toEntity(dto);

            // Load account relationships based on transaction type
            if ("INCOME".equals(dto.getType()) || "EXPENSE".equals(dto.getType())) {
                if (dto.getAccountId() != null) {
                    accountService.findById(dto.getAccountId()).ifPresent(t::setAccount);
                }
            } else if ("TRANSFER".equals(dto.getType())) {
                if (dto.getSourceAccountId() != null) {
                    accountService.findById(dto.getSourceAccountId()).ifPresent(t::setSourceAccount);
                }
                if (dto.getDestinationAccountId() != null) {
                    accountService.findById(dto.getDestinationAccountId()).ifPresent(t::setDestinationAccount);
                }
            }

            Transaction saved = service.saveWithBalanceUpdate(t);
            TransactionDto out = mapper.toDto(saved);

            logger.info("Transaction created successfully with id={}", out.getId());
            return ResponseEntity.created(URI.create("/api/transactions/" + out.getId())).body(out);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create transaction: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    e.getMessage(),
                    "Validation Error",
                    "/api/transactions"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * DELETE /api/transactions/{id}
     *
     * Delete (hard delete) a transaction by id.
     * Response: 204 No Content on success or 404 Not Found when the transaction does not exist.
     *
     * @param id the id of the transaction to delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        logger.info("DELETE /api/transactions/{} - Deleting transaction", id);

        try {
            service.deleteById(id);
            logger.info("Transaction deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete transaction: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage(),
                    "Not Found",
                    "/api/transactions/" + id
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * PUT /api/transactions/{id}
     *
     * Update an existing transaction identified by id. Supports updating amount, date, description, and type.
     * When amount or type changes, account balances are automatically adjusted (old balances rolled back,
     * new balances applied).
     *
     * Response: 200 OK with updated TransactionDto on success,
     * 404 Not Found if the transaction does not exist, or
     * 400 Bad Request on validation failure.
     *
     * @param id the id of the transaction to update
     * @param dto a DTO containing fields to update
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TransactionDto dto) {
        logger.info("PUT /api/transactions/{} - Updating transaction: type={}, amount={}",
            id, dto.getType(), dto.getAmount());

        try {
            Transaction updatedTransaction = mapper.toEntity(dto);

            // Load account relationships based on transaction type
            if ("INCOME".equals(dto.getType()) || "EXPENSE".equals(dto.getType())) {
                if (dto.getAccountId() != null) {
                    accountService.findById(dto.getAccountId()).ifPresent(updatedTransaction::setAccount);
                }
            } else if ("TRANSFER".equals(dto.getType())) {
                if (dto.getSourceAccountId() != null) {
                    accountService.findById(dto.getSourceAccountId()).ifPresent(updatedTransaction::setSourceAccount);
                }
                if (dto.getDestinationAccountId() != null) {
                    accountService.findById(dto.getDestinationAccountId()).ifPresent(updatedTransaction::setDestinationAccount);
                }
            }

            Transaction saved = service.updateWithBalanceAdjustment(id, updatedTransaction);
            TransactionDto out = mapper.toDto(saved);

            logger.info("Transaction updated successfully: id={}", id);
            return ResponseEntity.ok(out);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update transaction: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    e.getMessage(),
                    "Validation Error",
                    "/api/transactions/" + id
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error during transaction update: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred while updating the transaction",
                    e.getClass().getSimpleName(),
                    "/api/transactions/" + id
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/transactions/by-type/{type}
     *
     * Returns transactions filtered by type (INCOME, EXPENSE, TRANSFER).
     * Response: 200 OK with an array of TransactionDto.
     *
     * @param type the transaction type filter
     */
    @GetMapping("/by-type/{type}")
    public List<TransactionDto> getByType(@PathVariable String type) {
        logger.info("GET /api/transactions/by-type/{} - Fetching transactions by type", type);
        List<TransactionDto> transactions = service.findByType(type).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transactions of type {} to client", transactions.size(), type);
        return transactions;
    }

    /**
     * GET /api/transactions/account/{accountId}
     *
     * Returns transactions for a specific account id.
     * Response: 200 OK with an array of TransactionDto.
     *
     * @param accountId the account id to fetch transactions for
     */
    @GetMapping("/account/{accountId}")
    public List<TransactionDto> getByAccount(@PathVariable Long accountId) {
        logger.info("GET /api/transactions/account/{} - Fetching transactions for account", accountId);
        List<TransactionDto> transactions = service.findByAccountId(accountId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transactions for account {} to client", transactions.size(), accountId);
        return transactions;
    }

    /**
     * GET /api/transactions/transfers/from/{sourceAccountId}
     *
     * Returns transfer transactions outgoing from a source account.
     * Response: 200 OK with an array of TransactionDto.
     *
     * @param sourceAccountId id of the source account
     */
    @GetMapping("/transfers/from/{sourceAccountId}")
    public List<TransactionDto> getTransfersFrom(@PathVariable Long sourceAccountId) {
        logger.info("GET /api/transactions/transfers/from/{} - Fetching transfers from account", sourceAccountId);
        List<TransactionDto> transfers = service.findTransfersFromAccount(sourceAccountId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transfers from account {} to client", transfers.size(), sourceAccountId);
        return transfers;
    }

    /**
     * GET /api/transactions/transfers/to/{destAccountId}
     *
     * Returns transfer transactions incoming to a destination account.
     * Response: 200 OK with an array of TransactionDto.
     *
     * @param destAccountId id of the destination account
     */
    @GetMapping("/transfers/to/{destAccountId}")
    public List<TransactionDto> getTransfersTo(@PathVariable Long destAccountId) {
        logger.info("GET /api/transactions/transfers/to/{} - Fetching transfers to account", destAccountId);
        List<TransactionDto> transfers = service.findTransfersToAccount(destAccountId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transfers to account {} to client", transfers.size(), destAccountId);
        return transfers;
    }
}
