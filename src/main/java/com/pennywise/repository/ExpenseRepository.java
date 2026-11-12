package com.pennywise.repository;

import com.pennywise.model.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Repository interface for Expense data access.
 */
public interface ExpenseRepository extends CrudRepository<Expense, Long> {

    List<Expense> findByUserId(Long userId);

    List<Expense> findByUserIdAndDateRange(Long userId, LocalDate from, LocalDate to);

    List<Expense> findByUserIdAndCategory(Long userId, Long categoryId);

    BigDecimal getTotalExpenseByUserId(Long userId);

    BigDecimal getTotalExpenseByUserIdAndMonth(Long userId, int month, int year);

    BigDecimal getTotalSpentForCategoryInMonth(Long userId, Long categoryId, int month, int year);

    Map<String, BigDecimal> getSpendingByCategoryForMonth(Long userId, int month, int year);

    Map<LocalDate, BigDecimal> getDailySpendingTrends(Long userId, LocalDate from, LocalDate to);
}

