package com.pennywise.service;

import com.pennywise.model.FinancialSummary;
import com.pennywise.model.Transaction;
import com.pennywise.repository.BudgetRepository;
import com.pennywise.repository.BudgetRepositoryImpl;
import com.pennywise.repository.ExpenseRepository;
import com.pennywise.repository.ExpenseRepositoryImpl;
import com.pennywise.repository.IncomeRepository;
import com.pennywise.repository.IncomeRepositoryImpl;
import com.pennywise.repository.SavingsGoalRepository;
import com.pennywise.repository.SavingsGoalRepositoryImpl;
import com.pennywise.util.CsvExporter;
import com.pennywise.util.DateUtil;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for financial reporting and analytics.
 */
public class ReportServiceImpl implements ReportService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionService transactionService;

    public ReportServiceImpl() {
        this.incomeRepository = new IncomeRepositoryImpl();
        this.expenseRepository = new ExpenseRepositoryImpl();
        this.budgetRepository = new BudgetRepositoryImpl();
        this.savingsGoalRepository = new SavingsGoalRepositoryImpl();
        this.transactionService = new TransactionServiceImpl(incomeRepository, expenseRepository, budgetRepository, savingsGoalRepository);
    }

    public ReportServiceImpl(IncomeRepository incomeRepository,
                             ExpenseRepository expenseRepository,
                             BudgetRepository budgetRepository,
                             SavingsGoalRepository savingsGoalRepository,
                             TransactionService transactionService) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionService = transactionService;
    }

    @Override
    public FinancialSummary getMonthlyReport(Long userId, int month, int year) {
        return transactionService.calculateSummary(userId, month, year);
    }

    @Override
    public FinancialSummary getAnnualReport(Long userId, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        var incomes = incomeRepository.findByUserIdAndDateRange(userId, start, end);
        var expenses = expenseRepository.findByUserIdAndDateRange(userId, start, end);

        BigDecimal totalIncome = incomes.stream().map(i -> i.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenses.stream().map(e -> e.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSavings = savingsGoalRepository.getTotalSavingsByUserId(userId);

        BigDecimal avgDaily = totalExpense.divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);

        // Find highest spending category in the year
        Map<String, BigDecimal> catTotals = new LinkedHashMap<>();
        for (var e : expenses) {
            String name = e.getCategory() != null ? e.getCategory().getName() : "Other";
            catTotals.merge(name, e.getAmount(), BigDecimal::add);
        }

        String topCat = "None";
        BigDecimal topAmt = BigDecimal.ZERO;
        for (var entry : catTotals.entrySet()) {
            if (entry.getValue().compareTo(topAmt) > 0) {
                topAmt = entry.getValue();
                topCat = entry.getKey();
            }
        }

        return new FinancialSummary(totalIncome, totalExpense, totalSavings, BigDecimal.ZERO,
                topCat, topAmt, avgDaily, incomes.size() + expenses.size());
    }

    @Override
    public Map<String, BigDecimal> getCategoryBreakdown(Long userId, int month, int year) {
        return expenseRepository.getSpendingByCategoryForMonth(userId, month, year);
    }

    @Override
    public Map<String, BigDecimal> getMonthToMonthComparison(Long userId, int year) {
        Map<String, BigDecimal> monthMap = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            String mName = DateUtil.getMonthName(m).substring(0, 3);
            BigDecimal spent = expenseRepository.getTotalExpenseByUserIdAndMonth(userId, m, year);
            monthMap.put(mName, spent);
        }
        return monthMap;
    }

    @Override
    public void exportReportToCsv(Long userId, File file) throws IOException {
        List<Transaction> transactions = transactionService.getAllTransactions(userId);
        CsvExporter.exportTransactions(transactions, file);
    }
}

