package com.pennywise.validation;

import com.pennywise.model.SavingsGoal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates savings goal milestones and deposit contributions.
 */
public final class SavingsGoalValidator {

    private SavingsGoalValidator() {}

    public static ValidationResult<SavingsGoal> validate(SavingsGoal goal) {
        List<String> errors = new ArrayList<>();

        if (goal == null) {
            return ValidationResult.failure("Savings goal cannot be null.", null);
        }

        if (goal.getGoalName() == null || goal.getGoalName().trim().isEmpty()) {
            errors.add("Goal title is required.");
        } else if (goal.getGoalName().length() > 100) {
            errors.add("Goal title cannot exceed 100 characters.");
        }

        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Target amount must be greater than zero.");
        }

        if (goal.getTargetDate() == null) {
            errors.add("Target completion date is required.");
        } else if (goal.getTargetDate().isBefore(LocalDate.now())) {
            errors.add("Target date cannot be in the past.");
        }

        if (goal.getCurrentAmount() != null && goal.getCurrentAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Current savings cannot be negative.");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(errors, goal);
        }

        return ValidationResult.success(goal);
    }

    public static ValidationResult<BigDecimal> validateContribution(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.failure("Contribution amount must be greater than zero.", amount);
        }
        return ValidationResult.success(amount);
    }
}

