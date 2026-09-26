package com.example.data.repository

import com.example.model.Transaction
import com.example.model.YearMonth
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactions(): Flow<List<Transaction>>

    fun getTransactionsForMonth(
        yearMonth: YearMonth
    ): Flow<List<Transaction>>

    suspend fun addTransaction(transaction: Transaction)

    suspend fun updateTransaction(transaction: Transaction)

    suspend fun deleteTransaction(id: String)
}