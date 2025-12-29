package com.opensource.moneymanager.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDto {

    private static final Logger logger = LoggerFactory.getLogger(AccountDto.class);

    private Long id;

    @NotBlank(message = "Account name is required")
    @Size(min = 1, max = 255, message = "Account name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(BANK|CREDIT_CARD|DEBIT_CARD|E_WALLET|CASH|SAVINGS|INVESTMENT)$", message = "Type must be one of: BANK, CREDIT_CARD, DEBIT_CARD, E_WALLET, CASH, SAVINGS, INVESTMENT")
    private String type;

    @DecimalMin(value = "-999999999.99", message = "Balance must be at least -999999999.99")
    @DecimalMax(value = "999999999.99", message = "Balance must not exceed 999999999.99")
    private BigDecimal balance;

    @Size(max = 50, message = "Account number must not exceed 50 characters")
    private String accountNumber;

    @Size(max = 100, message = "Provider must not exceed 100 characters")
    private String provider;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Size(max = 500, message = "Description must not exceed 500 characters")
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

