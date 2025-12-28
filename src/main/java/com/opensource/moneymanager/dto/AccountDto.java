package com.opensource.moneymanager.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDto {

    private static final Logger logger = LoggerFactory.getLogger(AccountDto.class);

    private Long id;
    private String name;
    private String type; // BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT
    private BigDecimal balance;
    private String accountNumber;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private Boolean isActive;

    public AccountDto() {
        logger.debug("Creating new AccountDto instance");
    }

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
            logger.error("Invalid account type attempted in DTO: {}", type);
            throw new IllegalArgumentException("Type must be one of: BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT");
        }
        logger.debug("Setting DTO type: {}", type);
        this.type = type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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

