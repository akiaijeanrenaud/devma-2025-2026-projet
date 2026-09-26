package com.example.viewmodel

import com.example.model.Category
import com.example.model.Transaction
import com.example.model.YearMonth

data class EcoBudgetUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val transactions: List<Transaction> = emptyList(),

    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,

    val remainingBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val budgetUsageRatio: Float = 0f,
    val budgetUsagePercentage: Int = 0,

    val selectedCategories: Set<Category> = emptySet(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val categorySpent: Double = 0.0,

    val isAddDialogOpen: Boolean = false,
    val editingTransaction: Transaction? = null,
    val isLoading: Boolean = true
) {
    val isAllCategoriesSelected: Boolean
        get() = selectedCategories.isEmpty()
}