package com.pennywise.enums;

/**
 * Standard classification sources for income.
 */
public enum IncomeSource {
    SALARY("Salary"),
    FREELANCING("Freelancing"),
    BUSINESS("Business"),
    SCHOLARSHIP("Scholarship"),
    INVESTMENTS("Investments"),
    GIFTS("Gifts"),
    OTHER("Other");

    private final String displayName;

    IncomeSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static IncomeSource fromString(String text) {
        if (text == null || text.isBlank()) {
            return OTHER;
        }
        for (IncomeSource source : IncomeSource.values()) {
            if (source.name().equalsIgnoreCase(text.trim()) || source.displayName.equalsIgnoreCase(text.trim())) {
                return source;
            }
        }
        return OTHER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

