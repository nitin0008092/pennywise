package com.pennywise.validation;

import com.pennywise.model.Budget;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates budget limits and monthly period bounds.
 */
public final class BudgetValidator {

    private BudgetValidator() {}

    public static ValidationResult<Budget> validate(Budget budget) {
        List<String> errors = new ArrayList<>();

        if (budget == null) {
            return ValidationResult.failure("Budget cannot be null.", null);
        }

        if (budget.getCategoryId() == null || budget.getCategoryId() <= 0) {
            errors.add("Please select an expense category.");
        }

        if (budget.getMonthlyAmount() == null || budget.getMonthlyAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Monthly budget amount must be greater than zero.");
        }

        if (budget.getBudgetMonth() < 1 || budget.getBudgetMonth() > 12) {
            errors.add("Budget month must be between 1 and 12.");
        }

        if (budget.getBudgetYear() < 2000 || budget.getBudgetYear() > 2100) {
            errors.add("Invalid budget year.");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(errors, budget);
        }

        return ValidationResult.success(budget);
    }
}

