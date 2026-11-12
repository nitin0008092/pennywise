package com.pennywise.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing an expense category for budgeting and analytics.
 */
public class ExpenseCategory {
    private Long id;
    private Long userId; // null for system default categories
    private String name;
    private String description;
    private String colorCode;
    private String iconName;
    private boolean isDefault;
    private LocalDateTime createdAt;

    public ExpenseCategory() {
        this.colorCode = "#2563EB";
        this.iconName = "tag";
        this.isDefault = true;
        this.createdAt = LocalDateTime.now();
    }

    public ExpenseCategory(Long id, Long userId, String name, String description,
                           String colorCode, String iconName, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.colorCode = colorCode != null ? colorCode : "#2563EB";
        this.iconName = iconName != null ? iconName : "tag";
        this.isDefault = isDefault;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseCategory that)) return false;
        return Objects.equals(id, that.id) || (name != null && name.equalsIgnoreCase(that.name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name != null ? name.toLowerCase() : "");
    }

    @Override
    public String toString() {
        return name;
    }
}

