package com.pennywise.model;

import com.pennywise.enums.PaymentMethod;
import com.pennywise.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class for all monetary transactions in PennyWise.
 * Demonstrates Abstraction and serves as the root for Inheritance hierarchies.
 */
public abstract class Transaction implements Comparable<Transaction> {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;

    public Transaction() {
        this.amount = BigDecimal.ZERO;
        this.transactionDate = LocalDate.now();
        this.paymentMethod = PaymentMethod.CASH;
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(Long userId, BigDecimal amount, String description,
                       LocalDate transactionDate, PaymentMethod paymentMethod) {
        this.userId = userId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.description = description;
        this.transactionDate = transactionDate != null ? transactionDate : LocalDate.now();
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.CASH;
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(Long id, Long userId, BigDecimal amount, String description,
                       LocalDate transactionDate, PaymentMethod paymentMethod, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.description = description;
        this.transactionDate = transactionDate != null ? transactionDate : LocalDate.now();
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.CASH;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Abstract methods demonstrating Polymorphism
    /**
     * Identifies the transaction type (INCOME or EXPENSE).
     */
    public abstract TransactionType getTransactionType();

    /**
     * Returns the signed impact on user cash balance (+amount for income, -amount for expense).
     */
    public abstract BigDecimal getEffectiveImpact();

    /**
     * Returns the category or source name for unified reporting.
     */
    public abstract String getCategoryOrSource();

    // Standard encapsulated Getters and Setters
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate != null ? transactionDate : LocalDate.now();
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.CASH;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int compareTo(Transaction other) {
        if (other == null) return 1;
        // Primary sort: Date descending (newest first)
        int dateCompare = other.transactionDate.compareTo(this.transactionDate);
        if (dateCompare != 0) return dateCompare;
        // Secondary sort: ID descending
        if (this.id != null && other.id != null) {
            return other.id.compareTo(this.id);
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }
}

