package com.pennywise.service;

import com.pennywise.exception.ResourceNotFoundException;
import com.pennywise.exception.ValidationException;
import com.pennywise.model.Budget;
import com.pennywise.repository.BudgetRepository;
import com.pennywise.repository.BudgetRepositoryImpl;
import com.pennywise.validation.BudgetValidator;
import com.pennywise.validation.ValidationResult;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service implementation for Budget operations.
 */
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetServiceImpl() {
        this.budgetRepository = new BudgetRepositoryImpl();
    }

    public BudgetServiceImpl(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Budget setBudget(Budget budget) {
        ValidationResult<Budget> validation = BudgetValidator.validate(budget);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getCombinedErrors());
        }
        return budgetRepository.save(budget);
    }

    @Override
    public boolean deleteBudget(Long budgetId, Long userId) {
        Budget existing = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID: " + budgetId));

        if (!existing.getUserId().equals(userId)) {
            throw new ValidationException("Unauthorized deletion of budget.");
        }

        return budgetRepository.deleteById(budgetId);
    }

    @Override
    public List<Budget> getBudgetsForPeriod(Long userId, int month, int year) {
        return budgetRepository.findByUserIdAndPeriod(userId, month, year);
    }

    @Override
    public BigDecimal getTotalBudgetForPeriod(Long userId, int month, int year) {
        return budgetRepository.getTotalBudgetByUserIdAndPeriod(userId, month, year);
    }

    @Override
    public List<Budget> checkBudgetAlerts(Long userId, int month, int year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndPeriod(userId, month, year);
        // Returns budgets that have reached or exceeded 70% utilization
        return budgets.stream()
                .filter(b -> b.getUtilizationPercentage() >= 70.0)
                .toList();
    }
}

