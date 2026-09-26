package com.example.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun getTimeForMonth(
    monthOffset: Int,
    day: Int,
    hour: Int
): Long {

    val currentMillis = getCurrentTimeMillis()

    val instant = Instant.fromEpochMilliseconds(currentMillis)
    val timeZone = TimeZone.currentSystemDefault()

    val currentDate = instant.toLocalDateTime(timeZone)

    val firstDayOfMonth = LocalDate(
        year = currentDate.year,
        monthNumber = currentDate.monthNumber,
        dayOfMonth = 1
    )

    val targetDate = firstDayOfMonth.plus(
        monthOffset,
        DateTimeUnit.MONTH
    )

    val targetDateTime = LocalDateTime(
        year = targetDate.year,
        monthNumber = targetDate.monthNumber,
        dayOfMonth = day,
        hour = hour,
        minute = 0,
        second = 0,
        nanosecond = 0
    )

    return targetDateTime
        .toInstant(timeZone)
        .toEpochMilliseconds()
}