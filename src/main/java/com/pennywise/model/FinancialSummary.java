package com.pennywise.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Data Transfer Object summarizing key financial metrics for a user.
 */
public class FinancialSummary {
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal currentBalance = BigDecimal.ZERO;
    private BigDecimal totalSavings = BigDecimal.ZERO;
    private BigDecimal monthlyBudget = BigDecimal.ZERO;
    private double budgetUtilization = 0.0;
    private double savingsRate = 0.0;
    private String highestSpendingCategory = "None";
    private BigDecimal highestCategoryAmount = BigDecimal.ZERO;
    private BigDecimal averageDailySpending = BigDecimal.ZERO;
    private int transactionCount = 0;

    public FinancialSummary() {}

    public FinancialSummary(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal totalSavings,
                            BigDecimal monthlyBudget, String highestSpendingCategory,
                            BigDecimal highestCategoryAmount, BigDecimal averageDailySpending,
                            int transactionCount) {
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        this.totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        this.currentBalance = this.totalIncome.subtract(this.totalExpenses);
        this.totalSavings = totalSavings != null ? totalSavings : BigDecimal.ZERO;
        this.monthlyBudget = monthlyBudget != null ? monthlyBudget : BigDecimal.ZERO;
        this.highestSpendingCategory = highestSpendingCategory != null ? highestSpendingCategory : "None";
        this.highestCategoryAmount = highestCategoryAmount != null ? highestCategoryAmount : BigDecimal.ZERO;
        this.averageDailySpending = averageDailySpending != null ? averageDailySpending : BigDecimal.ZERO;
        this.transactionCount = transactionCount;

        // Calculate budget utilization
        if (this.monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
            this.budgetUtilization = this.totalExpenses.multiply(BigDecimal.valueOf(100))
                    .divide(this.monthlyBudget, 2, RoundingMode.HALF_UP).doubleValue();
        }

        // Calculate savings rate: (Net Savings / Total Income) * 100
        if (this.totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal net = this.totalIncome.subtract(this.totalExpenses);
            this.savingsRate = net.multiply(BigDecimal.valueOf(100))
                    .divide(this.totalIncome, 2, RoundingMode.HALF_UP).doubleValue();
        }
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
    }

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public double getBudgetUtilization() {
        return budgetUtilization;
    }

    public void setBudgetUtilization(double budgetUtilization) {
        this.budgetUtilization = budgetUtilization;
    }

    public double getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(double savingsRate) {
        this.savingsRate = savingsRate;
    }

    public String getHighestSpendingCategory() {
        return highestSpendingCategory;
    }

    public void setHighestSpendingCategory(String highestSpendingCategory) {
        this.highestSpendingCategory = highestSpendingCategory;
    }

    public BigDecimal getHighestCategoryAmount() {
        return highestCategoryAmount;
    }

    public void setHighestCategoryAmount(BigDecimal highestCategoryAmount) {
        this.highestCategoryAmount = highestCategoryAmount;
    }

    public BigDecimal getAverageDailySpending() {
        return averageDailySpending;
    }

    public void setAverageDailySpending(BigDecimal averageDailySpending) {
        this.averageDailySpending = averageDailySpending;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }
}

