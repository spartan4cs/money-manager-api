package com.opensource.moneymanager.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDto.class);

    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDateTime dateTime;
    private String type; // INCOME, EXPENSE, or TRANSFER

    public TransactionDto() {
        logger.debug("Creating new TransactionDto instance");
        this.dateTime = LocalDateTime.now();
    }

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
        // Validate that type is one of: INCOME, EXPENSE, TRANSFER
        if (type != null && !type.matches("^(INCOME|EXPENSE|TRANSFER)$")) {
            logger.error("Invalid transaction type attempted in DTO: {}", type);
            throw new IllegalArgumentException("Type must be one of: INCOME, EXPENSE, TRANSFER");
        }
        logger.debug("Setting DTO type: {}", type);
        this.type = type;
    }
}
