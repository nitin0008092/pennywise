package com.pennywise.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing an individual monetary deposit into a savings goal.
 */
public class SavingsContribution {
    private Long id;
    private Long goalId;
    private Long userId;
    private BigDecimal amount;
    private LocalDate contributionDate;
    private String notes;
    private LocalDateTime createdAt;

    public SavingsContribution() {
        this.amount = BigDecimal.ZERO;
        this.contributionDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    public SavingsContribution(Long goalId, Long userId, BigDecimal amount,
                               LocalDate contributionDate, String notes) {
        this.goalId = goalId;
        this.userId = userId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.contributionDate = contributionDate != null ? contributionDate : LocalDate.now();
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public SavingsContribution(Long id, Long goalId, Long userId, BigDecimal amount,
                               LocalDate contributionDate, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.goalId = goalId;
        this.userId = userId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.contributionDate = contributionDate != null ? contributionDate : LocalDate.now();
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public LocalDate getContributionDate() {
        return contributionDate;
    }

    public void setContributionDate(LocalDate contributionDate) {
        this.contributionDate = contributionDate;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

