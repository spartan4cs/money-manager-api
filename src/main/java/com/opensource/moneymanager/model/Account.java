package com.opensource.moneymanager.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    private String description; // Optional notes

    @Column(nullable = false)
    private Boolean isActive = true; // Soft delete flag

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
}

