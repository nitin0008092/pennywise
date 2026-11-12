package com.pennywise.service;

import com.pennywise.model.Budget;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service contract for Budget management.
 */
public interface BudgetService {

    Budget setBudget(Budget budget);

    boolean deleteBudget(Long budgetId, Long userId);

    List<Budget> getBudgetsForPeriod(Long userId, int month, int year);

    BigDecimal getTotalBudgetForPeriod(Long userId, int month, int year);

    List<Budget> checkBudgetAlerts(Long userId, int month, int year);
}

