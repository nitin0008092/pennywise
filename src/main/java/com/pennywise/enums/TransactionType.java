package com.pennywise.enums;

/**
 * Distinguishes inflows (INCOME) from outflows (EXPENSE).
 */
public enum TransactionType {
    INCOME("Income", "+"),
    EXPENSE("Expense", "-");

    private final String displayName;
    private final String sign;

    TransactionType(String displayName, String sign) {
        this.displayName = displayName;
        this.sign = sign;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSign() {
        return sign;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

