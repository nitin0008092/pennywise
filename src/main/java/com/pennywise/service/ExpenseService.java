package com.pennywise.service;

import com.pennywise.model.Expense;
import com.pennywise.model.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service contract for Expense management.
 */
public interface ExpenseService {

    Expense addExpense(Expense expense);

    Expense updateExpense(Expense expense);

    boolean deleteExpense(Long expenseId, Long userId);

    List<Expense> getExpenseHistory(Long userId);

    List<Expense> getExpensesByDateRange(Long userId, LocalDate from, LocalDate to);

    List<Expense> getExpensesByCategory(Long userId, Long categoryId);

    List<Expense> searchExpenses(Long userId, String query);

    BigDecimal getTotalExpense(Long userId);

    BigDecimal getMonthlyExpense(Long userId, int month, int year);

    Map<String, BigDecimal> getSpendingByCategory(Long userId, int month, int year);

    Map<LocalDate, BigDecimal> getDailySpendingTrends(Long userId, LocalDate from, LocalDate to);

    List<ExpenseCategory> getAvailableCategories(Long userId);

    ExpenseCategory addCategory(ExpenseCategory category);
}

