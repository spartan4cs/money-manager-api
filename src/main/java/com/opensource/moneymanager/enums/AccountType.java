package com.opensource.moneymanager.enums;

public enum AccountType {
    BANK("BANK"),
    CREDIT_CARD("CREDIT_CARD"),
    DEBIT_CARD("DEBIT_CARD"),
    E_WALLET("E_WALLET"),
    CASH("CASH"),
    SAVINGS("SAVINGS"),
    INVESTMENT("INVESTMENT");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AccountType fromString(String value) {
        for (AccountType type : AccountType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid account type: " + value);
    }
}

