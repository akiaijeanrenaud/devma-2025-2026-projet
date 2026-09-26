package com.example.data.repository

import com.example.model.Category
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.model.YearMonth
import com.example.util.getCurrentTimeMillis
import com.example.util.getTimeForMonth
import com.example.util.generateUUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeTransactionRepository : TransactionRepository {

    private val _transactions = MutableStateFlow(
        listOf(
            // Mois actuel (0)
            // Mois actuel (0)
            Transaction(
                id = generateUUID(),
                title = "Supermarché Bio",
                amount = 45000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(0, 22, 14),
                category = Category.FOOD
            ),
            Transaction(
                id = generateUUID(),
                title = "Session Tennis",
                amount = 12000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(0, 20, 10),
                category = Category.ENTERTAINMENT
            ),
            Transaction(
                id = generateUUID(),
                title = "Ticket de Bus Express",
                amount = 2500.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(0, 18, 8),
                category = Category.TRANSPORT
            ),
            Transaction(
                id = generateUUID(),
                title = "Loyer Mensuel",
                amount = 250000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(0, 5, 9),
                category = Category.HOUSING
            ),
            Transaction(
                id = generateUUID(),
                title = "Boulangerie & Pâtisserie",
                amount = 4800.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(0, 15, 16),
                category = Category.FOOD
            ),
            Transaction(
                id = generateUUID(),
                title = "Recharge Vélo Électrique",
                amount = 3500.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(0, 12, 11),
                category = Category.TRANSPORT
            ),
            Transaction(
                id = generateUUID(),
                title = "Facture Électricité",
                amount = 48000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(0, 8, 15),
                category = Category.HOUSING
            ),

// Mois précédent (-1)
            Transaction(
                id = generateUUID(),
                title = "Loyer Mois Précédent",
                amount = 250000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(-1, 5, 9),
                category = Category.HOUSING
            ),
            Transaction(
                id = generateUUID(),
                title = "Courses du mois",
                amount = 65000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(-1, 10, 15),
                category = Category.FOOD
            ),
            Transaction(
                id = generateUUID(),
                title = "Abonnement Transport",
                amount = 35000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(-1, 2, 8),
                category = Category.TRANSPORT
            ),
            Transaction(
                id = generateUUID(),
                title = "Sortie Restaurant",
                amount = 22000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(-1, 20, 20),
                category = Category.ENTERTAINMENT
            ),

// Mois suivant (+1)
            Transaction(
                id = generateUUID(),
                title = "Avance Loyer Prévue",
                amount = 250000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(1, 1, 9),
                category = Category.HOUSING
            ),
            Transaction(
                id = generateUUID(),
                title = "Abonnement Salle de Sport",
                amount = 20000.0,
                type = TransactionType.EXPENSE,
                dateMillis = getTimeForMonth(1, 3, 10),
                category = Category.ENTERTAINMENT
            )
        )
    )

    override fun getTransactions(): Flow<List<Transaction>> {
        return _transactions.asStateFlow()
    }

    override fun getTransactionsForMonth(
        yearMonth: YearMonth
    ): Flow<List<Transaction>> {
        return _transactions.map { transactions ->
            transactions.filter { transaction ->
                yearMonth.containsTimestamp(transaction.dateMillis)
            }
        }
    }

    override suspend fun addTransaction(
        transaction: Transaction
    ) {
        _transactions.value =
            _transactions.value + transaction
    }

    override suspend fun updateTransaction(
        transaction: Transaction
    ) {
        _transactions.value =
            _transactions.value.map {
                if (it.id == transaction.id) {
                    transaction
                } else {
                    it
                }
            }
    }

    override suspend fun deleteTransaction(
        id: String
    ) {
        _transactions.value =
            _transactions.value.filterNot {
                it.id == id
            }
    }
}