package com.example.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class YearMonth(
    val year: Int,
    val month: Int
) {
    val displayLabel: String
        get() {
            val monthName = when (month) {
                1 -> "Janvier"
                2 -> "Février"
                3 -> "Mars"
                4 -> "Avril"
                5 -> "Mai"
                6 -> "Juin"
                7 -> "Juillet"
                8 -> "Août"
                9 -> "Septembre"
                10 -> "Octobre"
                11 -> "Novembre"
                12 -> "Décembre"
                else -> ""
            }

            return "$monthName $year"
        }

    fun containsTimestamp(timestampMillis: Long): Boolean {
        val instant = Instant.fromEpochMilliseconds(timestampMillis)
        val localDateTime =
            instant.toLocalDateTime(TimeZone.currentSystemDefault())

        return localDateTime.year == year &&
                localDateTime.monthNumber == month
    }

    fun previous(): YearMonth =
        if (month == 1) YearMonth(year - 1, 12)
        else YearMonth(year, month - 1)

    fun next(): YearMonth =
        if (month == 12) YearMonth(year + 1, 1)
        else YearMonth(year, month + 1)

    companion object {
        fun now(): YearMonth {
            val local = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())

            return YearMonth(
                local.year,
                local.monthNumber
            )
        }

        fun current(): YearMonth = now()
    }
}