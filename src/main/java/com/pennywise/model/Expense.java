package com.pennywise.model;

import com.pennywise.enums.PaymentMethod;
import com.pennywise.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Concrete transaction representing spending and expenditures.
 * Demonstrates Inheritance and Polymorphic method overriding.
 */
public class Expense extends Transaction {
    private ExpenseCategory category;
    private Long categoryId;

    public Expense() {
        super();
    }

    public Expense(Long userId, Long categoryId, BigDecimal amount,
                   String description, LocalDate transactionDate, PaymentMethod paymentMethod) {
        super(userId, amount, description, transactionDate, paymentMethod);
        this.categoryId = categoryId;
    }

    public Expense(Long id, Long userId, Long categoryId, ExpenseCategory category,
                   BigDecimal amount, String description, LocalDate transactionDate,
                   PaymentMethod paymentMethod, LocalDateTime createdAt) {
        super(id, userId, amount, description, transactionDate, paymentMethod, createdAt);
        this.categoryId = categoryId != null ? categoryId : (category != null ? category.getId() : null);
        this.category = category;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.EXPENSE;
    }

    @Override
    public BigDecimal getEffectiveImpact() {
        return getAmount().negate(); // Negative impact on balance
    }

    @Override
    public String getCategoryOrSource() {
        return category != null ? category.getName() : "Uncategorized";
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getId();
        }
    }

    public Long getCategoryId() {
        return categoryId != null ? categoryId : (category != null ? category.getId() : null);
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + getId() +
                ", amount=" + getAmount() +
                ", category=" + (category != null ? category.getName() : categoryId) +
                ", date=" + getTransactionDate() +
                '}';
    }
}

