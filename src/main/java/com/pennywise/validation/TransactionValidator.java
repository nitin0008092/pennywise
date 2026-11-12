package com.pennywise.validation;

import com.pennywise.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates financial transactions (Income & Expense).
 */
public final class TransactionValidator {

    private TransactionValidator() {}

    public static <T extends Transaction> ValidationResult<T> validate(T transaction) {
        List<String> errors = new ArrayList<>();

        if (transaction == null) {
            return ValidationResult.failure("Transaction payload cannot be null.", null);
        }

        if (transaction.getAmount() == null) {
            errors.add("Amount is required.");
        } else if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Amount must be strictly greater than zero.");
        } else if (transaction.getAmount().scale() > 2) {
            errors.add("Amount cannot have more than 2 decimal places.");
        }

        if (transaction.getTransactionDate() == null) {
            errors.add("Transaction date is required.");
        } else if (transaction.getTransactionDate().isAfter(LocalDate.now().plusDays(1))) {
            errors.add("Transaction date cannot be in the future.");
        }

        if (transaction.getPaymentMethod() == null) {
            errors.add("Payment method must be selected.");
        }

        if (transaction.getDescription() != null && transaction.getDescription().length() > 255) {
            errors.add("Description cannot exceed 255 characters.");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(errors, transaction);
        }

        return ValidationResult.success(transaction);
    }
}

