package com.pennywise.service;

import com.pennywise.model.FinancialSummary;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Service contract for financial reporting, analytics, and CSV exporting.
 */
public interface ReportService {

    FinancialSummary getMonthlyReport(Long userId, int month, int year);

    FinancialSummary getAnnualReport(Long userId, int year);

    Map<String, BigDecimal> getCategoryBreakdown(Long userId, int month, int year);

    Map<String, BigDecimal> getMonthToMonthComparison(Long userId, int year);

    void exportReportToCsv(Long userId, File file) throws IOException;
}

