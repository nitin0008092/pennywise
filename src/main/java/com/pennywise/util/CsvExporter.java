package com.pennywise.util;

import com.pennywise.model.Transaction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility for exporting financial transactions and analytical summaries to standard CSV format.
 */
public final class CsvExporter {

    private CsvExporter() {}

    /**
     * Exports a list of polymorphic transactions (Income & Expense) to a CSV file.
     */
    public static void exportTransactions(List<? extends Transaction> transactions, File file) throws IOException {
        if (transactions == null || file == null) {
            throw new IllegalArgumentException("Transactions and destination file cannot be null");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // CSV Header
            writer.println("Transaction ID,Type,Date,Category/Source,Description,Payment Method,Amount");

            for (Transaction t : transactions) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCsv(String.valueOf(t.getId()))).append(",");
                line.append(escapeCsv(t.getTransactionType().name())).append(",");
                line.append(escapeCsv(t.getTransactionDate() != null ? t.getTransactionDate().toString() : "")).append(",");
                line.append(escapeCsv(t.getCategoryOrSource())).append(",");
                line.append(escapeCsv(t.getDescription())).append(",");
                line.append(escapeCsv(t.getPaymentMethod() != null ? t.getPaymentMethod().getDisplayName() : "")).append(",");
                line.append(t.getAmount() != null ? t.getAmount().toPlainString() : "0.00");
                writer.println(line);
            }
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return "\"" + value + "\"";
    }
}

