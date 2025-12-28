package com.opensource.moneymanager.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String type; // INCOME, EXPENSE, or TRANSFER

    public Transaction() {
    }

    public Transaction(BigDecimal amount, String description, LocalDateTime dateTime, String type) {
        this.amount = amount;
        this.description = description;
        this.dateTime = dateTime;
        setType(type); // Validate on construction
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
