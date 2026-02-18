package com.opensource.moneymanager.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * DTO for returning account balance information.
 *
 * Fields:
 * - accountId: The ID of the account
 * - balance: The current balance of the account
 * - currency: The currency type (e.g., USD, INR)
 */
public class BalanceDto {

    private static final Logger logger = LoggerFactory.getLogger(BalanceDto.class);

    private String accountId;
    private BigDecimal balance;
    private String currency;

    public BalanceDto() {
        logger.debug("Creating new BalanceDto instance");
    }

    public BalanceDto(String accountId, BigDecimal balance, String currency) {
        this.accountId = accountId;
        this.balance = balance;
        this.currency = currency;
        logger.debug("Creating BalanceDto - accountId={}, balance={}, currency={}",
            accountId, balance, currency);
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "BalanceDto{" +
                "accountId='" + accountId + '\'' +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                '}';
    }
}

