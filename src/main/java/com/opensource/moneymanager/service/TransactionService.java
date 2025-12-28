package com.opensource.moneymanager.service;

import com.opensource.moneymanager.model.Transaction;
import com.opensource.moneymanager.repository.TransactionRepository;
import com.opensource.moneymanager.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository repository, AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
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

        // Validate account exists for INCOME/EXPENSE
        if ("INCOME".equals(t.getType()) || "EXPENSE".equals(t.getType())) {
            if (t.getAccount() == null || t.getAccount().getId() == null) {
                logger.warn("Account not provided for {} transaction", t.getType());
                throw new IllegalArgumentException("Account is required for " + t.getType() + " transactions");
            }
        }

        // Validate source and destination accounts for TRANSFER
        if ("TRANSFER".equals(t.getType())) {
            if (t.getSourceAccount() == null || t.getSourceAccount().getId() == null) {
                logger.warn("Source account not provided for TRANSFER transaction");
                throw new IllegalArgumentException("Source account is required for TRANSFER transactions");
            }
            if (t.getDestinationAccount() == null || t.getDestinationAccount().getId() == null) {
                logger.warn("Destination account not provided for TRANSFER transaction");
                throw new IllegalArgumentException("Destination account is required for TRANSFER transactions");
            }
            if (t.getSourceAccount().getId().equals(t.getDestinationAccount().getId())) {
                logger.warn("Source and destination accounts are the same");
                throw new IllegalArgumentException("Source and destination accounts cannot be the same");
            }
        }

        Transaction saved = repository.save(t);
        logger.info("Transaction saved successfully with id={}, type={}, amount={}",
            saved.getId(), saved.getType(), saved.getAmount());
        return saved;
    }

    @Transactional
    public Transaction saveWithBalanceUpdate(Transaction t) {
        logger.debug("Attempting to save transaction with balance update: type={}, amount={}",
            t.getType(), t.getAmount());

        // Save transaction first
        Transaction saved = save(t);

        // Update account balances based on transaction type
        try {
            if ("INCOME".equals(t.getType())) {
                logger.info("Processing INCOME transaction: account id={}, amount={}",
                    t.getAccount().getId(), t.getAmount());
                accountRepository.findById(t.getAccount().getId()).ifPresent(account -> {
                    account.setBalance(account.getBalance().add(t.getAmount()));
                    accountRepository.save(account);
                    logger.info("Account balance updated (INCOME): account id={}, new balance={}",
                        account.getId(), account.getBalance());
                });
            } else if ("EXPENSE".equals(t.getType())) {
                logger.info("Processing EXPENSE transaction: account id={}, amount={}",
                    t.getAccount().getId(), t.getAmount());
                accountRepository.findById(t.getAccount().getId()).ifPresent(account -> {
                    account.setBalance(account.getBalance().subtract(t.getAmount()));
                    accountRepository.save(account);
                    logger.info("Account balance updated (EXPENSE): account id={}, new balance={}",
                        account.getId(), account.getBalance());
                });
            } else if ("TRANSFER".equals(t.getType())) {
                logger.info("Processing TRANSFER transaction: source id={}, destination id={}, amount={}",
                    t.getSourceAccount().getId(), t.getDestinationAccount().getId(), t.getAmount());

                // Deduct from source account
                accountRepository.findById(t.getSourceAccount().getId()).ifPresent(source -> {
                    source.setBalance(source.getBalance().subtract(t.getAmount()));
                    accountRepository.save(source);
                    logger.info("Source account balance updated: account id={}, new balance={}",
                        source.getId(), source.getBalance());
                });

                // Add to destination account
                accountRepository.findById(t.getDestinationAccount().getId()).ifPresent(dest -> {
                    dest.setBalance(dest.getBalance().add(t.getAmount()));
                    accountRepository.save(dest);
                    logger.info("Destination account balance updated: account id={}, new balance={}",
                        dest.getId(), dest.getBalance());
                });
            }
        } catch (Exception e) {
            logger.error("Error updating account balances for transaction id={}: {}", saved.getId(), e.getMessage());
            throw new RuntimeException("Failed to update account balances: " + e.getMessage());
        }

        return saved;
    }

    @Transactional
    public Transaction updateWithBalanceAdjustment(Long id, Transaction updatedTransaction) {
        logger.debug("Attempting to update transaction with balance adjustment: id={}, new amount={}",
            id, updatedTransaction.getAmount());

        Optional<Transaction> existingOpt = repository.findById(id);
        if (!existingOpt.isPresent()) {
            logger.warn("Cannot update transaction: id={} not found", id);
            throw new IllegalArgumentException("Transaction not found with id: " + id);
        }

        Transaction existing = existingOpt.get();
        BigDecimal oldAmount = existing.getAmount();
        String oldType = existing.getType();

        // Validate the updated transaction
        if (updatedTransaction.getAmount() == null || updatedTransaction.getAmount().signum() <= 0) {
            logger.warn("Invalid transaction amount: {}", updatedTransaction.getAmount());
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        if (updatedTransaction.getType() == null) {
            logger.warn("Transaction type is null");
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        try {
            // Rollback old transaction balances first
            rollbackBalances(existing, oldAmount);

            // Update transaction fields
            existing.setAmount(updatedTransaction.getAmount());
            existing.setDescription(updatedTransaction.getDescription());
            existing.setType(updatedTransaction.getType());
            existing.setDateTime(updatedTransaction.getDateTime());

            // Save updated transaction
            Transaction saved = repository.save(existing);
            logger.info("Transaction updated: id={}, old type={}, new type={}, old amount={}, new amount={}",
                id, oldType, updatedTransaction.getType(), oldAmount, updatedTransaction.getAmount());

            // Apply new transaction balances
            applyBalanceChanges(saved);

            return saved;
        } catch (Exception e) {
            logger.error("Error updating transaction id={}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update transaction: " + e.getMessage());
        }
    }

    /**
     * Rollback account balances for a transaction
     */
    private void rollbackBalances(Transaction t, BigDecimal amount) {
        logger.debug("Rolling back balances for transaction id={}, type={}, amount={}",
            t.getId(), t.getType(), amount);

        if ("INCOME".equals(t.getType())) {
            accountRepository.findById(t.getAccount().getId()).ifPresent(account -> {
                account.setBalance(account.getBalance().subtract(amount));
                accountRepository.save(account);
                logger.debug("Rolled back INCOME balance: account id={}, new balance={}",
                    account.getId(), account.getBalance());
            });
        } else if ("EXPENSE".equals(t.getType())) {
            accountRepository.findById(t.getAccount().getId()).ifPresent(account -> {
                account.setBalance(account.getBalance().add(amount));
                accountRepository.save(account);
                logger.debug("Rolled back EXPENSE balance: account id={}, new balance={}",
                    account.getId(), account.getBalance());
            });
        } else if ("TRANSFER".equals(t.getType())) {
            accountRepository.findById(t.getSourceAccount().getId()).ifPresent(source -> {
                source.setBalance(source.getBalance().add(amount));
                accountRepository.save(source);
                logger.debug("Rolled back TRANSFER source balance: account id={}, new balance={}",
                    source.getId(), source.getBalance());
            });

            accountRepository.findById(t.getDestinationAccount().getId()).ifPresent(dest -> {
                dest.setBalance(dest.getBalance().subtract(amount));
                accountRepository.save(dest);
                logger.debug("Rolled back TRANSFER destination balance: account id={}, new balance={}",
                    dest.getId(), dest.getBalance());
            });
        }
    }

    /**
     * Apply account balance changes for a transaction
     */
    private void applyBalanceChanges(Transaction t) {
        logger.debug("Applying balance changes for transaction id={}, type={}, amount={}",
            t.getId(), t.getType(), t.getAmount());

        if ("INCOME".equals(t.getType())) {
            accountRepository.findById(t.getAccount().getId()).ifPresent(account -> {
                account.setBalance(account.getBalance().add(t.getAmount()));
                accountRepository.save(account);
                logger.debug("Applied INCOME balance: account id={}, new balance={}",
                    account.getId(), account.getBalance());
            });
        } else if ("EXPENSE".equals(t.getType())) {
            accountRepository.findById(t.getAccount().getId()).ifPresent(account -> {
                account.setBalance(account.getBalance().subtract(t.getAmount()));
                accountRepository.save(account);
                logger.debug("Applied EXPENSE balance: account id={}, new balance={}",
                    account.getId(), account.getBalance());
            });
        } else if ("TRANSFER".equals(t.getType())) {
            accountRepository.findById(t.getSourceAccount().getId()).ifPresent(source -> {
                source.setBalance(source.getBalance().subtract(t.getAmount()));
                accountRepository.save(source);
                logger.debug("Applied TRANSFER source balance: account id={}, new balance={}",
                    source.getId(), source.getBalance());
            });

            accountRepository.findById(t.getDestinationAccount().getId()).ifPresent(dest -> {
                dest.setBalance(dest.getBalance().add(t.getAmount()));
                accountRepository.save(dest);
                logger.debug("Applied TRANSFER destination balance: account id={}, new balance={}",
                    dest.getId(), dest.getBalance());
            });
        }
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

    @Transactional
    public void deleteById(Long id) {
        logger.debug("Attempting to delete transaction with id={}", id);

        Optional<Transaction> transaction = repository.findById(id);
        if (transaction.isPresent()) {
            Transaction t = transaction.get();

            // Rollback account balance changes before deleting the transaction
            try {
                if ("INCOME".equals(t.getType())) {
                    logger.info("Rolling back INCOME transaction: account id={}, amount={}",
                        t.getAccount().getId(), t.getAmount());
                    accountRepository.findById(t.getAccount().getId()).ifPresent(account -> {
                        account.setBalance(account.getBalance().subtract(t.getAmount()));
                        accountRepository.save(account);
                        logger.info("Account balance rolled back (INCOME): account id={}, new balance={}",
                            account.getId(), account.getBalance());
                    });
                } else if ("EXPENSE".equals(t.getType())) {
                    logger.info("Rolling back EXPENSE transaction: account id={}, amount={}",
                        t.getAccount().getId(), t.getAmount());
                    accountRepository.findById(t.getAccount().getId()).ifPresent(account -> {
                        account.setBalance(account.getBalance().add(t.getAmount()));
                        accountRepository.save(account);
                        logger.info("Account balance rolled back (EXPENSE): account id={}, new balance={}",
                            account.getId(), account.getBalance());
                    });
                } else if ("TRANSFER".equals(t.getType())) {
                    logger.info("Rolling back TRANSFER transaction: source id={}, destination id={}, amount={}",
                        t.getSourceAccount().getId(), t.getDestinationAccount().getId(), t.getAmount());

                    // Add back to source account (reverse subtract)
                    accountRepository.findById(t.getSourceAccount().getId()).ifPresent(source -> {
                        source.setBalance(source.getBalance().add(t.getAmount()));
                        accountRepository.save(source);
                        logger.info("Source account balance rolled back: account id={}, new balance={}",
                            source.getId(), source.getBalance());
                    });

                    // Subtract from destination account (reverse add)
                    accountRepository.findById(t.getDestinationAccount().getId()).ifPresent(dest -> {
                        dest.setBalance(dest.getBalance().subtract(t.getAmount()));
                        accountRepository.save(dest);
                        logger.info("Destination account balance rolled back: account id={}, new balance={}",
                            dest.getId(), dest.getBalance());
                    });
                }
            } catch (Exception e) {
                logger.error("Error rolling back account balances for transaction id={}: {}", id, e.getMessage());
                throw new RuntimeException("Failed to rollback account balances: " + e.getMessage());
            }

            repository.deleteById(id);
            logger.info("Transaction deleted successfully with balance rollback: id={}, type={}",
                id, t.getType());
        } else {
            logger.warn("Cannot delete transaction: id={} not found", id);
            throw new IllegalArgumentException("Transaction not found with id: " + id);
        }
    }

    public List<Transaction> findByType(String type) {
        logger.debug("Fetching transactions by type={}", type);
        List<Transaction> transactions = repository.findByType(type);
        logger.info("Found {} transactions of type={}", transactions.size(), type);
        return transactions;
    }

    public List<Transaction> findByAccountId(Long accountId) {
        logger.debug("Fetching transactions for account id={}", accountId);
        List<Transaction> transactions = repository.findByAccountId(accountId);
        logger.info("Found {} transactions for account id={}", transactions.size(), accountId);
        return transactions;
    }

    public List<Transaction> findTransfersFromAccount(Long sourceAccountId) {
        logger.debug("Fetching transfers from account id={}", sourceAccountId);
        List<Transaction> transfers = repository.findBySourceAccountId(sourceAccountId);
        logger.info("Found {} transfers from account id={}", transfers.size(), sourceAccountId);
        return transfers;
    }

    public List<Transaction> findTransfersToAccount(Long destAccountId) {
        logger.debug("Fetching transfers to account id={}", destAccountId);
        List<Transaction> transfers = repository.findByDestinationAccountId(destAccountId);
        logger.info("Found {} transfers to account id={}", transfers.size(), destAccountId);
        return transfers;
    }
}
