package com.pennywise.service;

import com.pennywise.enums.IncomeSource;
import com.pennywise.model.Income;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service contract for Income management.
 */
public interface IncomeService {

    Income addIncome(Income income);

    Income updateIncome(Income income);

    boolean deleteIncome(Long incomeId, Long userId);

    List<Income> getIncomeHistory(Long userId);

    List<Income> getIncomeByDateRange(Long userId, LocalDate from, LocalDate to);

    List<Income> getIncomeBySource(Long userId, IncomeSource source);

    List<Income> searchIncome(Long userId, String query);

    BigDecimal getTotalIncome(Long userId);

    BigDecimal getMonthlyIncome(Long userId, int month, int year);
}

