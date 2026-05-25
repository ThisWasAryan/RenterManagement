package com.rms.app.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")
    private val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    fun formatDate(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return date.format(dateFormatter)
    }

    fun formatFullDate(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return date.format(fullDateFormatter)
    }

    fun formatMonthYear(month: Int, year: Int): String {
        val date = LocalDate.of(year, month, 1)
        return date.format(monthYearFormatter)
    }

    fun getCurrentMonth(): Int = LocalDate.now().monthValue
    fun getCurrentYear(): Int = LocalDate.now().year

    fun toTimestamp(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun fromTimestamp(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun daysBetween(startTimestamp: Long, endTimestamp: Long): Long {
        val start = fromTimestamp(startTimestamp)
        val end = fromTimestamp(endTimestamp)
        return ChronoUnit.DAYS.between(start, end)
    }

    fun isOverdue(dueTimestamp: Long): Boolean {
        return System.currentTimeMillis() > dueTimestamp
    }

    fun calculateElapsedMonths(startTimestamp: Long, endTimestamp: Long = System.currentTimeMillis()): Int {
        val start = fromTimestamp(startTimestamp)
        val end = fromTimestamp(endTimestamp)
        
        var months = ChronoUnit.MONTHS.between(start, end).toInt()
        // ChronoUnit.MONTHS.between already handles day-of-month logic correctly.
        // E.g. Apr 25 to May 24 = 0 months. Apr 25 to May 25 = 1 month.
        return maxOf(0, months)
    }

    fun getMonthName(month: Int): String {
        return LocalDate.of(2024, month, 1).format(DateTimeFormatter.ofPattern("MMMM"))
    }
}
