package com.opensource.moneymanager.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDto.class);

    private Long id;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Transaction amount must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Transaction amount must not exceed 999999999.99")
    private BigDecimal amount;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private LocalDateTime dateTime;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "^(INCOME|EXPENSE|TRANSFER)$", message = "Type must be one of: INCOME, EXPENSE, TRANSFER")
    private String type;

    // Account IDs
    private Long accountId; // Primary account for INCOME/EXPENSE
    private Long sourceAccountId; // Source account for TRANSFER
    private Long destinationAccountId; // Destination account for TRANSFER

    public TransactionDto() {
        logger.debug("Creating new TransactionDto instance");
        this.dateTime = LocalDateTime.now();
    }

    // ...existing getters and setters...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime != null ? dateTime : LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        logger.debug("Setting DTO type: {}", type);
        this.type = type;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
}
