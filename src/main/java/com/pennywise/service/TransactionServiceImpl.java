package com.pennywise.service;

import com.pennywise.enums.TransactionType;
import com.pennywise.model.Budget;
import com.pennywise.model.Expense;
import com.pennywise.model.FinancialSummary;
import com.pennywise.model.Income;
import com.pennywise.model.Transaction;
import com.pennywise.repository.BudgetRepository;
import com.pennywise.repository.BudgetRepositoryImpl;
import com.pennywise.repository.ExpenseRepository;
import com.pennywise.repository.ExpenseRepositoryImpl;
import com.pennywise.repository.IncomeRepository;
import com.pennywise.repository.IncomeRepositoryImpl;
import com.pennywise.repository.SavingsGoalRepository;
import com.pennywise.repository.SavingsGoalRepositoryImpl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Service implementation demonstrating Polymorphism and Java Streams.
 */
public class TransactionServiceImpl implements TransactionService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public TransactionServiceImpl() {
        this.incomeRepository = new IncomeRepositoryImpl();
        this.expenseRepository = new ExpenseRepositoryImpl();
        this.budgetRepository = new BudgetRepositoryImpl();
        this.savingsGoalRepository = new SavingsGoalRepositoryImpl();
    }

    public TransactionServiceImpl(IncomeRepository incomeRepository,
                                  ExpenseRepository expenseRepository,
                                  BudgetRepository budgetRepository,
                                  SavingsGoalRepository savingsGoalRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.savingsGoalRepository = savingsGoalRepository;
    }

    /**
     * Polymorphically combines Income and Expense into a unified, chronologically sorted list.
     */
    @Override
    public List<Transaction> getAllTransactions(Long userId) {
        List<Income> incomes = incomeRepository.findByUserId(userId);
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        List<Transaction> unified = new ArrayList<>(incomes.size() + expenses.size());
        unified.addAll(incomes);
        unified.addAll(expenses);

        // Sort descending: newest transactions first using Comparable natural ordering
        Collections.sort(unified);
        return unified;
    }

    @Override
    public List<Transaction> getRecentTransactions(Long userId, int limit) {
        List<Transaction> all = getAllTransactions(userId);
        return all.stream().limit(Math.max(limit, 0)).toList();
    }

    @Override
    public List<Transaction> filter(List<Transaction> list, String query, LocalDate from, LocalDate to, TransactionType type) {
        if (list == null) return Collections.emptyList();

        return list.stream()
                .filter(t -> {
                    // Type filter
                    if (type != null && t.getTransactionType() != type) {
                        return false;
                    }
                    // Date range filter
                    if (from != null && t.getTransactionDate().isBefore(from)) {
                        return false;
                    }
                    if (to != null && t.getTransactionDate().isAfter(to)) {
                        return false;
                    }
                    // Search text filter
                    if (query != null && !query.isBlank()) {
                        String q = query.trim().toLowerCase();
                        boolean matchDesc = t.getDescription() != null && t.getDescription().toLowerCase().contains(q);
                        boolean matchCat = t.getCategoryOrSource() != null && t.getCategoryOrSource().toLowerCase().contains(q);
                        boolean matchMethod = t.getPaymentMethod() != null && t.getPaymentMethod().getDisplayName().toLowerCase().contains(q);
                        boolean matchAmount = t.getAmount() != null && t.getAmount().toPlainString().contains(q);
                        return matchDesc || matchCat || matchMethod || matchAmount;
                    }
                    return true;
                })
                .sorted()
                .toList();
    }

    @Override
    public FinancialSummary calculateSummary(Long userId, int month, int year) {
        BigDecimal totalIncome = incomeRepository.getTotalIncomeByUserIdAndMonth(userId, month, year);
        BigDecimal totalExpense = expenseRepository.getTotalExpenseByUserIdAndMonth(userId, month, year);
        BigDecimal totalSavings = savingsGoalRepository.getTotalSavingsByUserId(userId);
        BigDecimal monthlyBudget = budgetRepository.getTotalBudgetByUserIdAndPeriod(userId, month, year);

        Map<String, BigDecimal> spendingMap = expenseRepository.getSpendingByCategoryForMonth(userId, month, year);
        String topCategory = "None";
        BigDecimal topAmount = BigDecimal.ZERO;
        if (!spendingMap.isEmpty()) {
            var firstEntry = spendingMap.entrySet().iterator().next();
            topCategory = firstEntry.getKey();
            topAmount = firstEntry.getValue();
        }

        // Daily average spending in current month up to today
        LocalDate now = LocalDate.now();
        int days = (now.getYear() == year && now.getMonthValue() == month) ? Math.max(now.getDayOfMonth(), 1) : 30;
        BigDecimal avgDaily = totalExpense.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);

        int count = incomeRepository.findByUserId(userId).size() + expenseRepository.findByUserId(userId).size();

        return new FinancialSummary(totalIncome, totalExpense, totalSavings, monthlyBudget,
                topCategory, topAmount, avgDaily, count);
    }

    @Override
    public FinancialSummary calculateOverallSummary(Long userId) {
        BigDecimal totalIncome = incomeRepository.getTotalIncomeByUserId(userId);
        BigDecimal totalExpense = expenseRepository.getTotalExpenseByUserId(userId);
        BigDecimal totalSavings = savingsGoalRepository.getTotalSavingsByUserId(userId);

        LocalDate now = LocalDate.now();
        BigDecimal monthlyBudget = budgetRepository.getTotalBudgetByUserIdAndPeriod(userId, now.getMonthValue(), now.getYear());

        Map<String, BigDecimal> spendingMap = expenseRepository.getSpendingByCategoryForMonth(userId, now.getMonthValue(), now.getYear());
        String topCategory = "None";
        BigDecimal topAmount = BigDecimal.ZERO;
        if (!spendingMap.isEmpty()) {
            var firstEntry = spendingMap.entrySet().iterator().next();
            topCategory = firstEntry.getKey();
            topAmount = firstEntry.getValue();
        }

        BigDecimal avgDaily = totalExpense.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        int count = incomeRepository.findByUserId(userId).size() + expenseRepository.findByUserId(userId).size();

        return new FinancialSummary(totalIncome, totalExpense, totalSavings, monthlyBudget,
                topCategory, topAmount, avgDaily, count);
    }
}

