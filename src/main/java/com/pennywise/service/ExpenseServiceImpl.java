package com.pennywise.service;

import com.pennywise.exception.ResourceNotFoundException;
import com.pennywise.exception.ValidationException;
import com.pennywise.model.Expense;
import com.pennywise.model.ExpenseCategory;
import com.pennywise.repository.CategoryRepository;
import com.pennywise.repository.CategoryRepositoryImpl;
import com.pennywise.repository.ExpenseRepository;
import com.pennywise.repository.ExpenseRepositoryImpl;
import com.pennywise.validation.TransactionValidator;
import com.pennywise.validation.ValidationResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for Expense operations.
 */
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseServiceImpl() {
        this.expenseRepository = new ExpenseRepositoryImpl();
        this.categoryRepository = new CategoryRepositoryImpl();
    }

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Expense addExpense(Expense expense) {
        ValidationResult<Expense> validation = TransactionValidator.validate(expense);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getCombinedErrors());
        }
        if (expense.getCategoryId() == null || expense.getCategoryId() <= 0) {
            throw new ValidationException("Please select a valid expense category.");
        }
        return expenseRepository.save(expense);
    }

    @Override
    public Expense updateExpense(Expense expense) {
        ValidationResult<Expense> validation = TransactionValidator.validate(expense);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getCombinedErrors());
        }
        Expense existing = expenseRepository.findById(expense.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expense.getId()));

        if (!existing.getUserId().equals(expense.getUserId())) {
            throw new ValidationException("Unauthorized modification of expense record.");
        }

        boolean updated = expenseRepository.update(expense);
        if (!updated) {
            throw new ValidationException("Failed to update expense record.");
        }
        return expense;
    }

    @Override
    public boolean deleteExpense(Long expenseId, Long userId) {
        Expense existing = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        if (!existing.getUserId().equals(userId)) {
            throw new ValidationException("Unauthorized deletion of expense record.");
        }

        return expenseRepository.deleteById(expenseId);
    }

    @Override
    public List<Expense> getExpenseHistory(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Override
    public List<Expense> getExpensesByDateRange(Long userId, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new ValidationException("Invalid date range specified.");
        }
        return expenseRepository.findByUserIdAndDateRange(userId, from, to);
    }

    @Override
    public List<Expense> getExpensesByCategory(Long userId, Long categoryId) {
        return expenseRepository.findByUserIdAndCategory(userId, categoryId);
    }

    @Override
    public List<Expense> searchExpenses(Long userId, String query) {
        if (query == null || query.isBlank()) {
            return getExpenseHistory(userId);
        }
        String q = query.trim().toLowerCase();
        return expenseRepository.findByUserId(userId).stream()
                .filter(e -> (e.getDescription() != null && e.getDescription().toLowerCase().contains(q)) ||
                             (e.getCategory() != null && e.getCategory().getName().toLowerCase().contains(q)) ||
                             (e.getPaymentMethod() != null && e.getPaymentMethod().getDisplayName().toLowerCase().contains(q)) ||
                             (e.getAmount() != null && e.getAmount().toPlainString().contains(q)))
                .toList();
    }

    @Override
    public BigDecimal getTotalExpense(Long userId) {
        return expenseRepository.getTotalExpenseByUserId(userId);
    }

    @Override
    public BigDecimal getMonthlyExpense(Long userId, int month, int year) {
        return expenseRepository.getTotalExpenseByUserIdAndMonth(userId, month, year);
    }

    @Override
    public Map<String, BigDecimal> getSpendingByCategory(Long userId, int month, int year) {
        return expenseRepository.getSpendingByCategoryForMonth(userId, month, year);
    }

    @Override
    public Map<LocalDate, BigDecimal> getDailySpendingTrends(Long userId, LocalDate from, LocalDate to) {
        return expenseRepository.getDailySpendingTrends(userId, from, to);
    }

    @Override
    public List<ExpenseCategory> getAvailableCategories(Long userId) {
        return categoryRepository.findAvailableForUser(userId);
    }

    @Override
    public ExpenseCategory addCategory(ExpenseCategory category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty.");
        }
        return categoryRepository.save(category);
    }
}

