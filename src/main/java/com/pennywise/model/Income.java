package com.pennywise.model;

import com.pennywise.enums.IncomeSource;
import com.pennywise.enums.PaymentMethod;
import com.pennywise.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Concrete transaction representing revenue and earnings inflows.
 * Demonstrates Inheritance and Polymorphic method overriding.
 */
public class Income extends Transaction {
    private IncomeSource source;

    public Income() {
        super();
        this.source = IncomeSource.SALARY;
    }

    public Income(Long userId, BigDecimal amount, IncomeSource source,
                  String description, LocalDate transactionDate, PaymentMethod paymentMethod) {
        super(userId, amount, description, transactionDate, paymentMethod);
        this.source = source != null ? source : IncomeSource.SALARY;
    }

    public Income(Long id, Long userId, BigDecimal amount, IncomeSource source,
                  String description, LocalDate transactionDate, PaymentMethod paymentMethod,
                  LocalDateTime createdAt) {
        super(id, userId, amount, description, transactionDate, paymentMethod, createdAt);
        this.source = source != null ? source : IncomeSource.SALARY;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.INCOME;
    }

    @Override
    public BigDecimal getEffectiveImpact() {
        return getAmount(); // Positive impact
    }

    @Override
    public String getCategoryOrSource() {
        return source != null ? source.getDisplayName() : IncomeSource.OTHER.getDisplayName();
    }

    public IncomeSource getSource() {
        return source;
    }

    public void setSource(IncomeSource source) {
        this.source = source != null ? source : IncomeSource.OTHER;
    }

    @Override
    public String toString() {
        return "Income{" +
                "id=" + getId() +
                ", amount=" + getAmount() +
                ", source=" + source +
                ", date=" + getTransactionDate() +
                '}';
    }
}

