package com.opensource.moneymanager.service;

import com.opensource.moneymanager.model.Account;
import com.opensource.moneymanager.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
        logger.info("AccountService initialized with repository");
    }

    public Account save(Account a) {
        logger.debug("Attempting to save account: name={}, type={}, balance={}",
            a.getName(), a.getType(), a.getBalance());

        if (a.getName() == null || a.getName().trim().isEmpty()) {
            logger.warn("Account name is null or empty");
            throw new IllegalArgumentException("Account name cannot be null or empty");
        }

        if (a.getType() == null) {
            logger.warn("Account type is null");
            throw new IllegalArgumentException("Account type cannot be null");
        }

        // Check for duplicate account name (excluding current record if updating)
        Optional<Account> existingAccount = repository.findByName(a.getName());
        if (existingAccount.isPresent()) {
            // If updating, allow same name for the same account; if creating, reject duplicate
            if (a.getId() == null || !existingAccount.get().getId().equals(a.getId())) {
                logger.warn("Account with name '{}' already exists", a.getName());
                throw new IllegalArgumentException("An account with name '" + a.getName() + "' already exists");
            }
        }

        // Rely on entity lifecycle callbacks (@PrePersist/@PreUpdate) to populate defaults

        Account saved = repository.save(a);
        logger.info("Account saved successfully with id={}, name={}, type={}",
            saved.getId(), saved.getName(), saved.getType());
        return saved;
    }

    public List<Account> findAll() {
        logger.debug("Fetching all active accounts");
        List<Account> accounts = repository.findByIsActiveTrue();
        logger.info("Retrieved {} active accounts from database", accounts.size());
        return accounts;
    }

    public Optional<Account> findById(Long id) {
        logger.debug("Fetching account with id={}", id);
        Optional<Account> account = repository.findByIdAndIsActiveTrue(id);

        if (account.isPresent()) {
            logger.info("Account found: id={}, name={}, type={}",
                id, account.get().getName(), account.get().getType());
        } else {
            logger.warn("Account not found: id={}", id);
        }

        return account;
    }

    public Optional<Account> findByName(String name) {
        logger.debug("Fetching account by name={}", name);
        Optional<Account> account = repository.findByNameAndIsActiveTrue(name);

        if (account.isPresent()) {
            logger.info("Account found by name: id={}, name={}", account.get().getId(), name);
        } else {
            logger.warn("Account not found by name: {}", name);
        }

        return account;
    }

    public List<Account> findByType(String type) {
        logger.debug("Fetching accounts by type={}", type);
        List<Account> accounts = repository.findByTypeAndIsActiveTrue(type);
        logger.info("Found {} accounts of type={}", accounts.size(), type);
        return accounts;
    }

    public void deleteById(Long id) {
        logger.debug("Attempting to soft delete account with id={}", id);

        Optional<Account> account = repository.findById(id);
        if (account.isPresent()) {
            if (Boolean.FALSE.equals(account.get().getIsActive())) {
                logger.info("Account already inactive: id={}", id);
                return; // already deleted
            }
            account.get().setIsActive(false);
            repository.save(account.get());
            logger.info("Account soft deleted successfully: id={}, name={}",
                id, account.get().getName());
        } else {
            logger.warn("Cannot delete account: id={} not found", id);
            throw new IllegalArgumentException("Account not found with id: " + id);
        }
    }

    public Account updateBalance(Long accountId, BigDecimal amount) {
        logger.debug("Updating balance for account id={}, amount={}", accountId, amount);

        Optional<Account> account = repository.findByIdAndIsActiveTrue(accountId);
        if (account.isPresent()) {
            BigDecimal newBalance = account.get().getBalance().add(amount);
            account.get().setBalance(newBalance);
            Account updated = repository.save(account.get());
            logger.info("Account balance updated: id={}, newBalance={}", accountId, newBalance);
            return updated;
        } else {
            logger.warn("Cannot update balance for non-existent or inactive account: id={}", accountId);
            throw new IllegalArgumentException("Active account not found with id: " + accountId);
        }
    }

    public Boolean hasNegativeBalance(Long accountId) {
        logger.debug("Checking balance for account id={}", accountId);
        Optional<Account> account = repository.findByIdAndIsActiveTrue(accountId);
        if (account.isPresent()) {
            boolean isNegative = account.get().getBalance().signum() < 0;
            logger.debug("Account {} balance is negative: {}", accountId, isNegative);
            return isNegative;
        }
        logger.warn("Account not found or inactive when checking negative balance: id={}", accountId);
        return false;
    }
}
