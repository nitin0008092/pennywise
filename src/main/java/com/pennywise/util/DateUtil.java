package com.pennywise.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for date calculations, formatting, and monthly ranges.
 */
public final class DateUtil {

    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_DATETIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    public static final DateTimeFormatter MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy");

    private DateUtil() {}

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_DATE);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_DATETIME);
    }

    public static LocalDate getStartOfMonth(int month, int year) {
        return LocalDate.of(year, month, 1);
    }

    public static LocalDate getEndOfMonth(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        return ym.atEndOfMonth();
    }

    public static LocalDate getStartOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(1);
    }

    public static LocalDate getEndOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }

    public static String getMonthName(int month) {
        if (month < 1 || month > 12) return "";
        return YearMonth.of(2000, month).getMonth().name();
    }
}

