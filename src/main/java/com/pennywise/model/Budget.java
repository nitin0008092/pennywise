package com.pennywise.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Domain entity representing a category-specific monthly budget.
 * Contains financial utility methods to compute utilization percentage and thresholds.
 */
public class Budget {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private BigDecimal monthlyAmount;
    private int budgetMonth;
    private int budgetYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Dynamically computed transient financial fields
    private BigDecimal spentAmount = BigDecimal.ZERO;

    public Budget() {
        this.monthlyAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Budget(Long userId, Long categoryId, BigDecimal monthlyAmount, int budgetMonth, int budgetYear) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.monthlyAmount = monthlyAmount != null ? monthlyAmount : BigDecimal.ZERO;
        this.budgetMonth = budgetMonth;
        this.budgetYear = budgetYear;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Budget(Long id, Long userId, Long categoryId, String categoryName, String categoryColor,
                  BigDecimal monthlyAmount, int budgetMonth, int budgetYear,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor != null ? categoryColor : "#2563EB";
        this.monthlyAmount = monthlyAmount != null ? monthlyAmount : BigDecimal.ZERO;
        this.budgetMonth = budgetMonth;
        this.budgetYear = budgetYear;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    // Business Logic Calculations
    public BigDecimal getRemainingAmount() {
        if (monthlyAmount == null) return BigDecimal.ZERO;
        BigDecimal spent = spentAmount != null ? spentAmount : BigDecimal.ZERO;
        return monthlyAmount.subtract(spent);
    }

    public double getUtilizationPercentage() {
        if (monthlyAmount == null || monthlyAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal spent = spentAmount != null ? spentAmount : BigDecimal.ZERO;
        BigDecimal percentage = spent.multiply(BigDecimal.valueOf(100))
                .divide(monthlyAmount, 2, RoundingMode.HALF_UP);
        return percentage.doubleValue();
    }

    public boolean isExceeded() {
        return getUtilizationPercentage() >= 100.0;
    }

    public boolean isWarning() {
        double util = getUtilizationPercentage();
        return util >= 70.0 && util < 90.0;
    }

    public boolean isDanger() {
        return getUtilizationPercentage() >= 90.0;
    }

    public String getStatusColorHex() {
        double util = getUtilizationPercentage();
        if (util >= 90.0) {
            return "#DC2626"; // Red (Above 90%)
        } else if (util >= 70.0) {
            return "#F59E0B"; // Orange (70% - 90%)
        } else {
            return "#16A34A"; // Green (Below 70%)
        }
    }

    // Encapsulated Getters & Setters
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName != null ? categoryName : "Category";
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor != null ? categoryColor : "#2563EB";
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public BigDecimal getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(BigDecimal monthlyAmount) {
        this.monthlyAmount = monthlyAmount != null ? monthlyAmount : BigDecimal.ZERO;
    }

    public int getBudgetMonth() {
        return budgetMonth;
    }

    public void setBudgetMonth(int budgetMonth) {
        this.budgetMonth = budgetMonth;
    }

    public int getBudgetYear() {
        return budgetYear;
    }

    public void setBudgetYear(int budgetYear) {
        this.budgetYear = budgetYear;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount != null ? spentAmount : BigDecimal.ZERO;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount != null ? spentAmount : BigDecimal.ZERO;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

