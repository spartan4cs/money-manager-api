package com.opensource.moneymanager.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDateTime dateTime;
    private String type; // INCOME, EXPENSE, or TRANSFER

    public TransactionDto() {
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
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        // Validate that type is one of: INCOME, EXPENSE, TRANSFER
        if (type != null && !type.matches("^(INCOME|EXPENSE|TRANSFER)$")) {
            throw new IllegalArgumentException("Type must be one of: INCOME, EXPENSE, TRANSFER");
        }
        this.type = type;
    }
}
