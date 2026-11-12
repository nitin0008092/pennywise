package com.pennywise.enums;

/**
 * Severity and categorization of financial insights and notifications.
 */
public enum NotificationType {
    INFO("#2563EB", "Information"),
    SUCCESS("#16A34A", "Goal Achieved"),
    WARNING("#D97706", "Warning"),
    DANGER("#DC2626", "Budget Exceeded");

    private final String colorHex;
    private final String description;

    NotificationType(String colorHex, String description) {
        this.colorHex = colorHex;
        this.description = description;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getDescription() {
        return description;
    }
}

