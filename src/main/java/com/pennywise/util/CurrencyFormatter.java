package com.pennywise.util;

import com.pennywise.enums.CurrencyUnit;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility for formatting BigDecimal currency values consistently across the application.
 */
public final class CurrencyFormatter {

    private static CurrencyUnit defaultCurrency = CurrencyUnit.INR;

    private CurrencyFormatter() {}

    public static void setDefaultCurrency(CurrencyUnit currency) {
        if (currency != null) {
            defaultCurrency = currency;
        }
    }

    public static CurrencyUnit getDefaultCurrency() {
        return defaultCurrency;
    }

    /**
     * Formats an amount with the default active currency symbol.
     */
    public static String format(BigDecimal amount) {
        return format(amount, defaultCurrency);
    }

    /**
     * Formats an amount with a specific currency unit.
     */
    public static String format(BigDecimal amount, CurrencyUnit currency) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        CurrencyUnit unit = currency != null ? currency : defaultCurrency;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);

        // For INR, apply Indian numbering pattern: ##,##,##0.00
        if (unit == CurrencyUnit.INR) {
            return formatInr(amount);
        }

        return unit.getSymbol() + " " + df.format(amount);
    }

    /**
     * Formats positive with '+' and negative with '-' sign and color indication context.
     */
    public static String formatSigned(BigDecimal amount, CurrencyUnit currency) {
        if (amount == null) amount = BigDecimal.ZERO;
        CurrencyUnit unit = currency != null ? currency : defaultCurrency;
        String formatted = format(amount.abs(), unit);
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return "+ " + formatted;
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return "- " + formatted;
        } else {
            return formatted;
        }
    }

    private static String formatInr(BigDecimal amount) {
        boolean negative = amount.compareTo(BigDecimal.ZERO) < 0;
        BigDecimal abs = amount.abs();

        long whole = abs.longValue();
        int cents = abs.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).intValue();

        String wholeStr = String.valueOf(whole);
        StringBuilder sb = new StringBuilder();

        if (wholeStr.length() > 3) {
            String lastThree = wholeStr.substring(wholeStr.length() - 3);
            String remaining = wholeStr.substring(0, wholeStr.length() - 3);

            StringBuilder remWithCommas = new StringBuilder();
            int count = 0;
            for (int i = remaining.length() - 1; i >= 0; i--) {
                remWithCommas.insert(0, remaining.charAt(i));
                count++;
                if (count == 2 && i > 0) {
                    remWithCommas.insert(0, ',');
                    count = 0;
                }
            }
            sb.append(remWithCommas).append(',').append(lastThree);
        } else {
            sb.append(wholeStr);
        }

        sb.append(String.format(".%02d", cents));

        return (negative ? "-" : "") + "₹ " + sb;
    }
}

