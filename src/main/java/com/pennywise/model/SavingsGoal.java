package com.pennywise.model;

import com.pennywise.enums.GoalStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing a financial savings goal and its accumulated funds.
 */
public class SavingsGoal {
    private Long id;
    private Long userId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private GoalStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SavingsGoal() {
        this.targetAmount = BigDecimal.ZERO;
        this.currentAmount = BigDecimal.ZERO;
        this.status = GoalStatus.IN_PROGRESS;
        this.targetDate = LocalDate.now().plusMonths(6);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public SavingsGoal(Long userId, String goalName, BigDecimal targetAmount,
                       BigDecimal currentAmount, LocalDate targetDate, String description) {
        this();
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount != null ? targetAmount : BigDecimal.ZERO;
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
        this.targetDate = targetDate != null ? targetDate : LocalDate.now().plusMonths(6);
        this.description = description;
    }

    public SavingsGoal(Long id, Long userId, String goalName, BigDecimal targetAmount,
                       BigDecimal currentAmount, LocalDate targetDate, GoalStatus status,
                       String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount != null ? targetAmount : BigDecimal.ZERO;
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
        this.targetDate = targetDate != null ? targetDate : LocalDate.now().plusMonths(6);
        this.status = status != null ? status : GoalStatus.IN_PROGRESS;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    // Financial calculations
    public double getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal current = currentAmount != null ? currentAmount : BigDecimal.ZERO;
        BigDecimal percentage = current.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
        return Math.min(percentage.doubleValue(), 100.0);
    }

    public BigDecimal getRemainingAmount() {
        if (targetAmount == null) return BigDecimal.ZERO;
        BigDecimal current = currentAmount != null ? currentAmount : BigDecimal.ZERO;
        BigDecimal rem = targetAmount.subtract(current);
        return rem.compareTo(BigDecimal.ZERO) > 0 ? rem : BigDecimal.ZERO;
    }

    public boolean isGoalReached() {
        return currentAmount != null && targetAmount != null && currentAmount.compareTo(targetAmount) >= 0;
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

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount != null ? targetAmount : BigDecimal.ZERO;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status != null ? status : GoalStatus.IN_PROGRESS;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
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

