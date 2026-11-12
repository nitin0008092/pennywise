package com.pennywise.enums;

/**
 * Supported currencies in PennyWise. Default is INR (₹).
 */
public enum CurrencyUnit {
    INR("INR", "₹", "Indian Rupee"),
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound");

    private final String code;
    private final String symbol;
    private final String fullName;

    CurrencyUnit(String code, String symbol, String fullName) {
        this.code = code;
        this.symbol = symbol;
        this.fullName = fullName;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getFullName() {
        return fullName;
    }

    public static CurrencyUnit fromCode(String code) {
        if (code == null || code.isBlank()) {
            return INR;
        }
        for (CurrencyUnit cu : CurrencyUnit.values()) {
            if (cu.code.equalsIgnoreCase(code.trim()) || cu.symbol.equals(code.trim())) {
                return cu;
            }
        }
        return INR;
    }

    @Override
    public String toString() {
        return code + " (" + symbol + ") - " + fullName;
    }
}

