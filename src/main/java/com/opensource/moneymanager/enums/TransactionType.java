package com.opensource.moneymanager.enums;

public enum TransactionType {
    INCOME("INCOME"),
    EXPENSE("EXPENSE"),
    TRANSFER("TRANSFER");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TransactionType fromString(String value) {
        for (TransactionType type : TransactionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type: " + value);
    }
}

