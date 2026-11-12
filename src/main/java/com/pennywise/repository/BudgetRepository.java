package com.pennywise.repository;

import com.pennywise.model.Budget;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Budget data access.
 */
public interface BudgetRepository extends CrudRepository<Budget, Long> {

    List<Budget> findByUserIdAndPeriod(Long userId, int month, int year);

    Optional<Budget> findByUserCategoryAndPeriod(Long userId, Long categoryId, int month, int year);

    BigDecimal getTotalBudgetByUserIdAndPeriod(Long userId, int month, int year);
}

