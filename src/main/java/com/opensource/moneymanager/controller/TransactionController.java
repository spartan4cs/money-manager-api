package com.opensource.moneymanager.controller;

import com.opensource.moneymanager.dto.TransactionDto;
import com.opensource.moneymanager.mapper.TransactionMapper;
import com.opensource.moneymanager.model.Transaction;
import com.opensource.moneymanager.service.TransactionService;
import com.opensource.moneymanager.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService service;
    private final AccountService accountService;

    public TransactionController(TransactionService service, AccountService accountService) {
        this.service = service;
        this.accountService = accountService;
        logger.info("TransactionController initialized");
    }

    @GetMapping
    public List<TransactionDto> list() {
        logger.info("GET /api/transactions - Fetching all transactions");
        List<TransactionDto> transactions = service.findAll().stream()
            .map(TransactionMapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transactions to client", transactions.size());
        return transactions;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> get(@PathVariable Long id) {
        logger.info("GET /api/transactions/{} - Fetching transaction by id", id);
        return service.findById(id)
            .map(transaction -> {
                logger.info("Transaction found: id={}, type={}", id, transaction.getType());
                return ResponseEntity.ok(TransactionMapper.toDto(transaction));
            })
            .orElseGet(() -> {
                logger.warn("Transaction not found: id={}", id);
                return ResponseEntity.notFound().build();
            });
    }

    @PostMapping
    public ResponseEntity<TransactionDto> create(@RequestBody TransactionDto dto) {
        logger.info("POST /api/transactions - Creating new transaction: type={}, amount={}",
            dto.getType(), dto.getAmount());

        try {
            Transaction t = TransactionMapper.toEntity(dto);

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
            TransactionDto out = TransactionMapper.toDto(saved);

            logger.info("Transaction created successfully with id={}", out.getId());
            return ResponseEntity.created(URI.create("/api/transactions/" + out.getId())).body(out);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create transaction: {}", e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/transactions/{} - Deleting transaction", id);

        try {
            service.deleteById(id);
            logger.info("Transaction deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete transaction: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-type/{type}")
    public List<TransactionDto> getByType(@PathVariable String type) {
        logger.info("GET /api/transactions/by-type/{} - Fetching transactions by type", type);
        List<TransactionDto> transactions = service.findByType(type).stream()
            .map(TransactionMapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transactions of type {} to client", transactions.size(), type);
        return transactions;
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionDto> getByAccount(@PathVariable Long accountId) {
        logger.info("GET /api/transactions/account/{} - Fetching transactions for account", accountId);
        List<TransactionDto> transactions = service.findByAccountId(accountId).stream()
            .map(TransactionMapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transactions for account {} to client", transactions.size(), accountId);
        return transactions;
    }

    @GetMapping("/transfers/from/{sourceAccountId}")
    public List<TransactionDto> getTransfersFrom(@PathVariable Long sourceAccountId) {
        logger.info("GET /api/transactions/transfers/from/{} - Fetching transfers from account", sourceAccountId);
        List<TransactionDto> transfers = service.findTransfersFromAccount(sourceAccountId).stream()
            .map(TransactionMapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transfers from account {} to client", transfers.size(), sourceAccountId);
        return transfers;
    }

    @GetMapping("/transfers/to/{destAccountId}")
    public List<TransactionDto> getTransfersTo(@PathVariable Long destAccountId) {
        logger.info("GET /api/transactions/transfers/to/{} - Fetching transfers to account", destAccountId);
        List<TransactionDto> transfers = service.findTransfersToAccount(destAccountId).stream()
            .map(TransactionMapper::toDto)
            .collect(Collectors.toList());
        logger.info("Returning {} transfers to account {} to client", transfers.size(), destAccountId);
        return transfers;
    }
}
