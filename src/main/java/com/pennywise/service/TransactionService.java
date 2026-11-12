package com.pennywise.service;

import com.pennywise.enums.TransactionType;
import com.pennywise.model.FinancialSummary;
import com.pennywise.model.Transaction;
import java.time.LocalDate;
import java.util.List;

/**
 * Service contract for polymorphic transaction aggregation and filtering.
 */
public interface TransactionService {

    List<Transaction> getAllTransactions(Long userId);

    List<Transaction> getRecentTransactions(Long userId, int limit);

    List<Transaction> filter(List<Transaction> list, String query, LocalDate from, LocalDate to, TransactionType type);

    FinancialSummary calculateSummary(Long userId, int month, int year);

    FinancialSummary calculateOverallSummary(Long userId);
}

