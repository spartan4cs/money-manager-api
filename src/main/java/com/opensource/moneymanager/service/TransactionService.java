package com.opensource.moneymanager.service;

import com.opensource.moneymanager.model.Transaction;
import com.opensource.moneymanager.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
        logger.info("TransactionService initialized with repository");
    }

    public Transaction save(Transaction t) {
        logger.debug("Attempting to save transaction: amount={}, type={}, description={}",
            t.getAmount(), t.getType(), t.getDescription());

        if (t.getAmount() == null || t.getAmount().signum() <= 0) {
            logger.warn("Invalid transaction amount: {}", t.getAmount());
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        if (t.getType() == null) {
            logger.warn("Transaction type is null");
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        Transaction saved = repository.save(t);
        logger.info("Transaction saved successfully with id={}, type={}, amount={}",
            saved.getId(), saved.getType(), saved.getAmount());
        return saved;
    }

    public List<Transaction> findAll() {
        logger.debug("Fetching all transactions");
        List<Transaction> transactions = repository.findAll();
        logger.info("Retrieved {} transactions from database", transactions.size());
        return transactions;
    }

    public Optional<Transaction> findById(Long id) {
        logger.debug("Fetching transaction with id={}", id);
        Optional<Transaction> transaction = repository.findById(id);

        if (transaction.isPresent()) {
            logger.info("Transaction found: id={}, type={}, amount={}",
                id, transaction.get().getType(), transaction.get().getAmount());
        } else {
            logger.warn("Transaction not found: id={}", id);
        }

        return transaction;
    }

    public void deleteById(Long id) {
        logger.debug("Attempting to delete transaction with id={}", id);

        Optional<Transaction> transaction = repository.findById(id);
        if (transaction.isPresent()) {
            repository.deleteById(id);
            logger.info("Transaction deleted successfully: id={}, type={}",
                id, transaction.get().getType());
        } else {
            logger.warn("Cannot delete transaction: id={} not found", id);
            throw new IllegalArgumentException("Transaction not found with id: " + id);
        }
    }

    public List<Transaction> findByType(String type) {
        logger.debug("Fetching transactions by type={}", type);
        // This method can be added to repository for more complex queries
        List<Transaction> transactions = repository.findAll().stream()
            .filter(t -> t.getType().equals(type))
            .toList();
        logger.info("Found {} transactions of type={}", transactions.size(), type);
        return transactions;
    }
}
