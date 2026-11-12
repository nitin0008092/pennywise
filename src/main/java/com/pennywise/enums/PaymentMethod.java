package com.pennywise.enums;

/**
 * Payment and disbursement instruments.
 */
public enum PaymentMethod {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    UPI("UPI"),
    NET_BANKING("Net Banking"),
    BANK_TRANSFER("Bank Transfer"),
    CHEQUE("Cheque"),
    OTHER("Other");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromString(String text) {
        if (text == null || text.isBlank()) {
            return OTHER;
        }
        for (PaymentMethod pm : PaymentMethod.values()) {
            if (pm.name().equalsIgnoreCase(text.trim()) || pm.displayName.equalsIgnoreCase(text.trim())) {
                return pm;
            }
        }
        return OTHER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

