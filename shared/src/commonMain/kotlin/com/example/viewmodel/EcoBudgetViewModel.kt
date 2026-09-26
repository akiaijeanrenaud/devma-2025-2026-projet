package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FakeTransactionRepository
import com.example.data.repository.TransactionRepository
import com.example.model.Category
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.model.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class EcoBudgetViewModel(
    private val repository: TransactionRepository = FakeTransactionRepository()
) : ViewModel() {

    // Budget mensuel fixe : 500 000 F CFA
    private val monthlyBudget = 500_000.0

    private val _selectedYearMonth =
        MutableStateFlow(YearMonth.now())

    private val _selectedCategories =
        MutableStateFlow<Set<Category>>(emptySet())

    private val _isAddDialogOpen =
        MutableStateFlow(false)

    private val _editingTransaction =
        MutableStateFlow<Transaction?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _transactions =
        _selectedYearMonth.flatMapLatest { yearMonth ->
            repository.getTransactionsForMonth(yearMonth)
        }

    val uiState: StateFlow<EcoBudgetUiState> =
        combine(
            _selectedYearMonth,
            _transactions,
            _selectedCategories,
            _isAddDialogOpen,
            _editingTransaction
        ) { yearMonth,
            transactions,
            selectedCategories,
            isAddDialogOpen,
            editingTransaction ->

            val income = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }

            val expense = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            val filteredTransactions =
                if (selectedCategories.isEmpty()) {
                    transactions
                } else {
                    transactions.filter {
                        it.category in selectedCategories
                    }
                }

            val categorySpent = filteredTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            // Calcul du taux d'utilisation du budget de 500 000 F
            val usageRatio =
                if (monthlyBudget > 0.0) {
                    (expense / monthlyBudget)
                        .coerceIn(0.0, 1.0)
                } else {
                    0.0
                }

            // Budget restant
            val remainingBudget =
                monthlyBudget - expense

            EcoBudgetUiState(
                currentMonth = yearMonth,
                transactions = transactions,

                totalIncome = income,
                totalExpense = expense,
                balance = income - expense,

                // Budget mensuel fixe de 500 000 F
                remainingBudget = remainingBudget,
                totalSpent = expense,

                budgetUsageRatio = usageRatio.toFloat(),
                budgetUsagePercentage = (usageRatio * 100).toInt(),

                selectedCategories = selectedCategories,
                filteredTransactions = filteredTransactions,
                categorySpent = categorySpent,

                isAddDialogOpen = isAddDialogOpen,
                editingTransaction = editingTransaction,

                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EcoBudgetUiState()
        )

    // Navigation mensuelle

    fun previousMonth() {
        _selectedYearMonth.value =
            _selectedYearMonth.value.previous()
    }

    fun nextMonth() {
        _selectedYearMonth.value =
            _selectedYearMonth.value.next()
    }

    fun goToCurrentMonth() {
        _selectedYearMonth.value = YearMonth.now()
    }

    // Filtre des catégories

    fun clearCategoryFilter() {
        _selectedCategories.value = emptySet()
    }

    fun toggleCategory(category: Category) {
        val categories =
            _selectedCategories.value.toMutableSet()

        if (category in categories) {
            categories.remove(category)
        } else {
            categories.add(category)
        }

        _selectedCategories.value = categories
    }

    // Gestion du dialogue

    fun openAddDialog() {
        _editingTransaction.value = null
        _isAddDialogOpen.value = true
    }

    fun openEditDialog(transaction: Transaction) {
        _editingTransaction.value = transaction
        _isAddDialogOpen.value = true
    }

    fun dismissDialog() {
        _isAddDialogOpen.value = false
        _editingTransaction.value = null
    }

    // Ajout / modification

    fun saveTransaction(
        title: String,
        amount: Double,
        category: Category
    ) {
        viewModelScope.launch {

            val editingTransaction =
                _editingTransaction.value

            if (editingTransaction == null) {

                val transaction = Transaction(
                    title = title,
                    amount = amount,
                    type = TransactionType.EXPENSE,
                    category = category,
                    dateMillis =
                        Clock.System.now().toEpochMilliseconds()
                )

                repository.addTransaction(transaction)

            } else {

                val transaction = editingTransaction.copy(
                    title = title,
                    amount = amount,
                    category = category
                )

                repository.updateTransaction(transaction)
            }

            dismissDialog()
        }
    }

    // Suppression

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }
}