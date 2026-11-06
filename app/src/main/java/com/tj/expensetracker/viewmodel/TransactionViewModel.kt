package com.tj.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tj.expensetracker.data.entity.AnalyticsFilter
import com.tj.expensetracker.data.entity.CategoryExpense
import com.tj.expensetracker.data.entity.DailyExpense
import com.tj.expensetracker.data.entity.TransactionEntity
import com.tj.expensetracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // =========================
    // TRANSACTIONS
    // =========================

    val transactions: StateFlow<List<TransactionEntity>> =
        repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTransaction(
        amount: Long,
        type: String,
        category: String,
        account: String,
        note: String?
    ) {
        val transaction = TransactionEntity(
            amount = amount,
            type = type,
            timestamp = System.currentTimeMillis(),
            category = category,
            account = account,
            note = note
        )

        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(
        transaction: TransactionEntity
    ) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // =========================
    // BANK BALANCE
    // =========================

    val balance: StateFlow<Long> =
        repository.balance
            .map { balanceEntity ->
                balanceEntity?.amount ?: 0L
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0L
            )

    fun setBalance(amount: Long) {
        viewModelScope.launch {
            repository.setBalance(amount)
        }
    }

    fun addToBalance(amount: Long) {
        viewModelScope.launch {
            repository.addToBalance(amount)
        }
    }

    fun subtractFromBalance(amount: Long) {
        viewModelScope.launch {
            repository.subtractFromBalance(amount)
        }
    }

    // =========================
    // ANALYTICS
    // =========================

    val totalIncome: StateFlow<Long> =
        repository.totalIncome
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0L
            )

    val totalExpense: StateFlow<Long> =
        repository.totalExpense
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0L
            )

    val categoryExpenses: StateFlow<List<CategoryExpense>> =
        repository.expenseByCategory
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // =========================
    // ANALYTICS FILTER
    // =========================

    private val _analyticsFilter =
        MutableStateFlow(AnalyticsFilter.THIS_MONTH)

    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)

    val analyticsFilter: StateFlow<AnalyticsFilter> =
        _analyticsFilter

    private val analyticsDateRange =
        combine(
            _analyticsFilter,
            _customStartDate,
            _customEndDate
        ) { filter, customStart, customEnd ->
            getAnalyticsDateRange(filter, customStart, customEnd)
        }

    fun setCustomDateRange(startDateMillis: Long, endDateMillis: Long) {
        _customStartDate.value = startDateMillis
        _customEndDate.value = endDateMillis
        _analyticsFilter.value = AnalyticsFilter.CUSTOM
    }

    val filteredIncome: StateFlow<Long> =
        analyticsDateRange
            .flatMapLatest { (startTime, endTime) ->
                repository.getIncomeBetween(
                    startTime,
                    endTime
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0L
            )

    val filteredExpense: StateFlow<Long> =
        analyticsDateRange
            .flatMapLatest { (startTime, endTime) ->
                repository.getExpenseBetween(
                    startTime,
                    endTime
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0L
            )

    val filteredCategoryExpenses: StateFlow<List<CategoryExpense>> =
        analyticsDateRange
            .flatMapLatest { (startTime, endTime) ->
                repository.getExpenseByCategoryBetween(
                    startTime,
                    endTime
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val dailyExpense: StateFlow<List<DailyExpense>> =
        analyticsDateRange
            .flatMapLatest { (startTime, endTime) ->
                repository.getExpenseTransactionsBetween(
                    startTime,
                    endTime
                )
            }
            .map { transactions ->
                transactions.groupBy { tx ->
                    java.util.Calendar.getInstance().apply {
                        timeInMillis = tx.timestamp

                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
                .toSortedMap()
                .map { (dayStart, dayTransactions) ->
                    val label = java.text.SimpleDateFormat(
                        "d MMM",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date(dayStart))

                    DailyExpense(
                        dayStart = dayStart,
                        label = label,
                        totalAmount = dayTransactions.sumOf {
                            it.amount
                        }
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun setAnalyticsFilter(filter: AnalyticsFilter) {
        _analyticsFilter.value = filter
    }

    private fun getAnalyticsDateRange(
        filter: AnalyticsFilter,
        customStart: Long? = null,
        customEnd: Long? = null
    ): Pair<Long, Long> {
        val calendar = java.util.Calendar.getInstance()

        return when (filter) {
            AnalyticsFilter.THIS_MONTH -> {
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)

                val start = calendar.timeInMillis

                calendar.add(java.util.Calendar.MONTH, 1)
                val end = calendar.timeInMillis

                Pair(start, end)
            }

            AnalyticsFilter.LAST_MONTH -> {
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)

                calendar.add(java.util.Calendar.MONTH, -1)
                val start = calendar.timeInMillis

                calendar.add(java.util.Calendar.MONTH, 1)
                val end = calendar.timeInMillis

                Pair(start, end)
            }

            AnalyticsFilter.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }

            AnalyticsFilter.CUSTOM -> {
                if (customStart != null && customEnd != null && customEnd >= customStart) {
                    val start = localDayStart(customStart)
                    val endCalendar = java.util.Calendar.getInstance().apply {
                        timeInMillis = localDayStart(customEnd)
                        add(java.util.Calendar.DAY_OF_MONTH, 1)
                    }
                    Pair(start, endCalendar.timeInMillis)
                } else {
                    Pair(0L, Long.MAX_VALUE)
                }
            }
        }
    }

    private fun localDayStart(dateMillis: Long): Long {
        val utc = java.util.Calendar.getInstance(
            java.util.TimeZone.getTimeZone("UTC")
        ).apply {
            timeInMillis = dateMillis
        }
        return java.util.Calendar.getInstance().apply {
            clear()
            set(
                utc.get(java.util.Calendar.YEAR),
                utc.get(java.util.Calendar.MONTH),
                utc.get(java.util.Calendar.DAY_OF_MONTH),
                0, 0, 0
            )
        }.timeInMillis
    }
}

// =========================
// VIEWMODEL FACTORY
// =========================

class TransactionViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
