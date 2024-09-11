package com.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Date utility class for common date operations.
 */
public class DateUtils147 {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Formats a LocalDateTime to a standard string.
     * @param dateTime the date time to format
     * @return formatted string
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * Calculates the number of days between two dates.
     * @param start the start date
     * @param end the end date
     * @return number of days between the two dates
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Checks if a date is in the past.
     * @param dateTime the date to check
     * @return true if the date is before now
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Returns the start of the current day.
     * @return LocalDateTime at midnight today
     */
    public static LocalDateTime startOfToday() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
    }
}
