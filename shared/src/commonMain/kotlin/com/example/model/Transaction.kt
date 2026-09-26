package com.example.model

import com.example.util.generateUUID
import kotlinx.datetime.Instant

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class Transaction(
    val id: String = generateUUID(),
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val dateMillis: Long,
    val note: String? = null
) {
    val date: Instant
        get() = Instant.fromEpochMilliseconds(dateMillis)
}