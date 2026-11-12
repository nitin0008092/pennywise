package com.pennywise.repository;

import com.pennywise.enums.IncomeSource;
import com.pennywise.model.Income;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Income data access.
 */
public interface IncomeRepository extends CrudRepository<Income, Long> {

    List<Income> findByUserId(Long userId);

    List<Income> findByUserIdAndDateRange(Long userId, LocalDate from, LocalDate to);

    List<Income> findByUserIdAndSource(Long userId, IncomeSource source);

    BigDecimal getTotalIncomeByUserId(Long userId);

    BigDecimal getTotalIncomeByUserIdAndMonth(Long userId, int month, int year);
}

