package com.opensource.moneymanager.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String type; // INCOME, EXPENSE, or TRANSFER

    // Account relationship
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account; // Primary account for INCOME/EXPENSE

    // For TRANSFER transactions
    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount; // Source account for TRANSFER

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount; // Destination account for TRANSFER

    public Transaction() {
        logger.debug("Creating new Transaction instance");
        this.dateTime = LocalDateTime.now();
    }

    public Transaction(BigDecimal amount, String description, LocalDateTime dateTime, String type) {
        logger.debug("Creating Transaction with amount={}, type={}", amount, type);
        this.amount = amount;
        this.description = description;
        this.dateTime = dateTime != null ? dateTime : LocalDateTime.now();
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
        this.dateTime = dateTime != null ? dateTime : LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        // Validate that type is one of: INCOME, EXPENSE, TRANSFER
        if (type != null && !type.matches("^(INCOME|EXPENSE|TRANSFER)$")) {
            logger.error("Invalid transaction type attempted: {}", type);
            throw new IllegalArgumentException("Type must be one of: INCOME, EXPENSE, TRANSFER");
        }
        logger.debug("Setting transaction type: {}", type);
        this.type = type;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(Account destinationAccount) {
        this.destinationAccount = destinationAccount;
    }
}
