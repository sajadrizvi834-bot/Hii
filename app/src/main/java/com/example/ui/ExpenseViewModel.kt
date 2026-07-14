package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseViewModel(
    application: Application,
    private val repository: ExpenseRepository
) : AndroidViewModel(application) {

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val blockedItems: StateFlow<List<BlockedItem>> = repository.allBlockedItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalSpent: StateFlow<Double> = repository.totalSpent
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    private val _adviceText = MutableStateFlow<String>("")
    val adviceText: StateFlow<String> = _adviceText.asStateFlow()

    private val _isGeneratingAdvice = MutableStateFlow<Boolean>(false)
    val isGeneratingAdvice: StateFlow<Boolean> = _isGeneratingAdvice.asStateFlow()

    // Alert states
    private val _triggeredBlock = MutableStateFlow<BlockedItem?>(null)
    val triggeredBlock: StateFlow<BlockedItem?> = _triggeredBlock.asStateFlow()

    private val _pendingExpense = MutableStateFlow<Expense?>(null)
    val pendingExpense: StateFlow<Expense?> = _pendingExpense.asStateFlow()

    init {
        // Safe check and seed database on startup to avoid Room's onCreate deadlock
        viewModelScope.launch {
            repository.checkAndSeedDatabaseIfNeeded()
            generateAdvice()
        }
    }

    fun generateAdvice() {
        viewModelScope.launch {
            _isGeneratingAdvice.value = true
            try {
                val advice = GeminiService.generateFinancialAdvice(expenses.value, blockedItems.value)
                _adviceText.value = advice
            } catch (e: Exception) {
                _adviceText.value = GeminiService.generateLocalAdvice(expenses.value, blockedItems.value)
            } finally {
                _isGeneratingAdvice.value = false
            }
        }
    }

    /**
     * Checks if a new expense triggers any of our active Content Blocks.
     * If yes, stores it in pending states and returns the BlockedItem so the UI can prompt.
     * If no, saves immediately.
     */
    fun attemptAddExpense(title: String, amount: Double, category: String, notes: String?): BlockedItem? {
        val expense = Expense(title = title, amount = amount, category = category, notes = notes)
        
        // Find if this purchase matches any active blocked items (by name or category match)
        val activeBlocks = blockedItems.value.filter { it.isBlocked }
        val triggered = activeBlocks.firstOrNull { block ->
            title.contains(block.name, ignoreCase = true) ||
            category.contains(block.name, ignoreCase = true) ||
            (block.name.contains("Instagram") && (title.contains("IG", ignoreCase = true) || title.contains("Instagram", ignoreCase = true))) ||
            (block.name.contains("TikTok") && (title.contains("TikTok", ignoreCase = true) || title.contains("TT", ignoreCase = true)))
        }

        if (triggered != null) {
            _pendingExpense.value = expense
            _triggeredBlock.value = triggered
            return triggered
        } else {
            saveExpense(expense)
            return null
        }
    }

    fun confirmPendingExpense() {
        _pendingExpense.value?.let {
            saveExpense(it)
        }
        clearPendingExpense()
    }

    fun clearPendingExpense() {
        _pendingExpense.value = null
        _triggeredBlock.value = null
    }

    private fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
            // Re-generate advice when expenses change
            generateAdvice()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            generateAdvice()
        }
    }

    fun toggleBlockStatus(item: BlockedItem) {
        viewModelScope.launch {
            val updated = item.copy(isBlocked = !item.isBlocked)
            repository.updateBlockedItem(updated)
            generateAdvice()
        }
    }

    fun addCustomBlock(name: String, category: String, redirectSuggestion: String) {
        viewModelScope.launch {
            val item = BlockedItem(
                name = name,
                category = category,
                isBlocked = true,
                redirectSuggestion = redirectSuggestion.ifBlank { "You set a content block on $name to build financial discipline." }
            )
            repository.insertBlockedItem(item)
            generateAdvice()
        }
    }

    fun removeBlockedItem(item: BlockedItem) {
        viewModelScope.launch {
            repository.deleteBlockedItem(item)
            generateAdvice()
        }
    }
}

class ExpenseViewModelFactory(
    private val application: Application,
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
