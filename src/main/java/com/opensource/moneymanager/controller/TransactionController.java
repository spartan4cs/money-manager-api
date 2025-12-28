package com.opensource.moneymanager.controller;

import com.opensource.moneymanager.dto.TransactionDto;
import com.opensource.moneymanager.mapper.TransactionMapper;
import com.opensource.moneymanager.model.Transaction;
import com.opensource.moneymanager.service.TransactionService;
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

    public TransactionController(TransactionService service) {
        this.service = service;
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
            // Delegate to service for validation and saving
            Transaction saved = service.save(TransactionMapper.toEntity(dto));
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
}
