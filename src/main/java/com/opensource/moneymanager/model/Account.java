package com.opensource.moneymanager.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
public class Account {

    private static final Logger logger = LoggerFactory.getLogger(Account.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "HDFC Bank", "ICICI Credit Card", "Google Pay"

    @Column(nullable = false)
    private String type; // BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance; // Current balance in the account

    private String accountNumber; // Optional account number

    private String provider; // Bank/service provider name (optional)

    // Ensure createdAt is never null on persist
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Ensure updatedAt is never null and will be updated on change
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String description; // Optional notes

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive; // Soft delete flag

    // ===== BIDIRECTIONAL RELATIONSHIPS (Inverse Side) =====
    // Income/Expense transactions for this account (mapped to Transaction.account)
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();

    // Outgoing transfers FROM this account (mapped to Transaction.sourceAccount)
    @OneToMany(mappedBy = "sourceAccount", fetch = FetchType.LAZY)
    private Set<Transaction> outgoingTransfers = new HashSet<>();

    // Incoming transfers TO this account (mapped to Transaction.destinationAccount)
    @OneToMany(mappedBy = "destinationAccount", fetch = FetchType.LAZY)
    private Set<Transaction> incomingTransfers = new HashSet<>();

    public Account() {
        logger.debug("Creating new Account instance");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.balance = BigDecimal.ZERO;
        this.isActive = true;
    }

    public Account(String name, String type, BigDecimal balance) {
        logger.debug("Creating Account with name={}, type={}, balance={}", name, type, balance);
        this.name = name;
        this.type = type;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // JPA lifecycle callbacks to make sure timestamps are set even when entity is created without using constructors
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
            logger.debug("Setting createdAt for new Account: {}", this.createdAt);
        }
        if (this.isActive == null) {
            this.isActive = true;
            logger.debug("Setting default isActive for new Account: {}", this.isActive);
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null && !type.matches("^(BANK|CREDIT_CARD|DEBIT_CARD|E_WALLET|CASH|SAVINGS|INVESTMENT)$")) {
            logger.error("Invalid account type attempted: {}", type);
            throw new IllegalArgumentException("Type must be one of: BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT");
        }
        logger.debug("Setting account type: {}", type);
        this.type = type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // ===== RELATIONSHIP GETTERS (for querying from Account side) =====

    public Set<Transaction> getTransactions() {
        logger.debug("Retrieving transactions for account id={}, count={}", id, transactions.size());
        return transactions;
    }

    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Set<Transaction> getOutgoingTransfers() {
        logger.debug("Retrieving outgoing transfers for account id={}, count={}", id, outgoingTransfers.size());
        return outgoingTransfers;
    }

    public void setOutgoingTransfers(Set<Transaction> outgoingTransfers) {
        this.outgoingTransfers = outgoingTransfers;
    }

    public Set<Transaction> getIncomingTransfers() {
        logger.debug("Retrieving incoming transfers for account id={}, count={}", id, incomingTransfers.size());
        return incomingTransfers;
    }

    public void setIncomingTransfers(Set<Transaction> incomingTransfers) {
        this.incomingTransfers = incomingTransfers;
    }

    // ===== HELPER METHODS =====

    public void addTransaction(Transaction t) {
        logger.debug("Adding transaction to account id={}, transaction id={}", id, t.getId());
        if (t != null) {
            transactions.add(t);
            // Note: Do NOT set t.setAccount(this) here to avoid circular updates
            // Let the service layer manage this
        }
    }

    public void addOutgoingTransfer(Transaction t) {
        logger.debug("Adding outgoing transfer from account id={}, transaction id={}", id, t.getId());
        if (t != null) {
            outgoingTransfers.add(t);
        }
    }

    public void addIncomingTransfer(Transaction t) {
        logger.debug("Adding incoming transfer to account id={}, transaction id={}", id, t.getId());
        if (t != null) {
            incomingTransfers.add(t);
        }
    }

    // Get total number of all transactions (income + expense + transfers)
    public int getTotalTransactionCount() {
        int total = transactions.size() + outgoingTransfers.size() + incomingTransfers.size();
        logger.debug("Total transaction count for account id={}: {}", id, total);
        return total;
    }
}
