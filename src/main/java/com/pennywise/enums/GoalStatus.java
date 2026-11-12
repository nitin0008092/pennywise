package com.pennywise.enums;

/**
 * State tracking for savings goals.
 */
public enum GoalStatus {
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    GoalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GoalStatus fromString(String text) {
        if (text == null || text.isBlank()) {
            return IN_PROGRESS;
        }
        for (GoalStatus gs : GoalStatus.values()) {
            if (gs.name().equalsIgnoreCase(text.trim()) || gs.displayName.equalsIgnoreCase(text.trim())) {
                return gs;
            }
        }
        return IN_PROGRESS;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

