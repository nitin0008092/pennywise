package com.pennywise.service;

import com.pennywise.enums.IncomeSource;
import com.pennywise.exception.ResourceNotFoundException;
import com.pennywise.exception.ValidationException;
import com.pennywise.model.Income;
import com.pennywise.repository.IncomeRepository;
import com.pennywise.repository.IncomeRepositoryImpl;
import com.pennywise.validation.TransactionValidator;
import com.pennywise.validation.ValidationResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for Income operations.
 */
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeServiceImpl() {
        this.incomeRepository = new IncomeRepositoryImpl();
    }

    public IncomeServiceImpl(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @Override
    public Income addIncome(Income income) {
        ValidationResult<Income> validation = TransactionValidator.validate(income);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getCombinedErrors());
        }
        return incomeRepository.save(income);
    }

    @Override
    public Income updateIncome(Income income) {
        ValidationResult<Income> validation = TransactionValidator.validate(income);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getCombinedErrors());
        }
        Income existing = incomeRepository.findById(income.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Income record not found with ID: " + income.getId()));

        if (!existing.getUserId().equals(income.getUserId())) {
            throw new ValidationException("Unauthorized modification of income record.");
        }

        boolean updated = incomeRepository.update(income);
        if (!updated) {
            throw new ValidationException("Failed to update income record.");
        }
        return income;
    }

    @Override
    public boolean deleteIncome(Long incomeId, Long userId) {
        Income existing = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Income record not found with ID: " + incomeId));

        if (!existing.getUserId().equals(userId)) {
            throw new ValidationException("Unauthorized deletion of income record.");
        }

        return incomeRepository.deleteById(incomeId);
    }

    @Override
    public List<Income> getIncomeHistory(Long userId) {
        return incomeRepository.findByUserId(userId);
    }

    @Override
    public List<Income> getIncomeByDateRange(Long userId, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new ValidationException("Invalid date range specified.");
        }
        return incomeRepository.findByUserIdAndDateRange(userId, from, to);
    }

    @Override
    public List<Income> getIncomeBySource(Long userId, IncomeSource source) {
        return incomeRepository.findByUserIdAndSource(userId, source);
    }

    @Override
    public List<Income> searchIncome(Long userId, String query) {
        if (query == null || query.isBlank()) {
            return getIncomeHistory(userId);
        }
        String q = query.trim().toLowerCase();
        return incomeRepository.findByUserId(userId).stream()
                .filter(i -> (i.getDescription() != null && i.getDescription().toLowerCase().contains(q)) ||
                             (i.getSource() != null && i.getSource().getDisplayName().toLowerCase().contains(q)) ||
                             (i.getPaymentMethod() != null && i.getPaymentMethod().getDisplayName().toLowerCase().contains(q)) ||
                             (i.getAmount() != null && i.getAmount().toPlainString().contains(q)))
                .toList();
    }

    @Override
    public BigDecimal getTotalIncome(Long userId) {
        return incomeRepository.getTotalIncomeByUserId(userId);
    }

    @Override
    public BigDecimal getMonthlyIncome(Long userId, int month, int year) {
        return incomeRepository.getTotalIncomeByUserIdAndMonth(userId, month, year);
    }
}

