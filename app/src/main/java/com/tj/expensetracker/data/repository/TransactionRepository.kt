package com.tj.expensetracker.data.repository

import com.tj.expensetracker.data.dao.TransactionDao
import com.tj.expensetracker.data.entity.BalanceEntity
import com.tj.expensetracker.data.entity.CategoryExpense
import com.tj.expensetracker.data.dao.BalanceDao
import com.tj.expensetracker.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val balanceDao: BalanceDao
) {
    val totalIncome: Flow<Long> =
        transactionDao.getTotalIncome()

    val totalExpense: Flow<Long> =
        transactionDao.getTotalExpense()

    val expenseByCategory: Flow<List<CategoryExpense>> =
        transactionDao.getExpenseByCategory()

    suspend fun getTransactionsForBackup(): List<TransactionEntity> {
        return transactionDao.getTransactionsForBackup()
    }

    suspend fun replaceTransactions(
        transactions: List<TransactionEntity>
    ) {
        transactionDao.deleteAllTransactions()
        transactionDao.insertTransactions(transactions)
    }

    suspend fun clearAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    suspend fun clearStoredBalance() {
        balanceDao.clearBalance()
    }

    // =========================
    // TRANSACTIONS
    // =========================
    val allTransactions: Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    suspend fun insertTransaction(
        transaction: TransactionEntity
    ) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(
        transaction: TransactionEntity
    ) {
        transactionDao.deleteTransaction(transaction)
    }

    // =========================
    // FILTERED ANALYTICS
    // =========================

    fun getIncomeBetween(startTime: Long, endTime: Long): Flow<Long> =
        transactionDao.getIncomeBetween(startTime, endTime)

    fun getExpenseBetween(startTime: Long, endTime: Long): Flow<Long> =
        transactionDao.getExpenseBetween(startTime, endTime)

    fun getExpenseByCategoryBetween(startTime: Long, endTime: Long): Flow<List<CategoryExpense>> =
        transactionDao.getExpenseByCategoryBetween(startTime, endTime)

    // =========================
    // BALANCE
    // =========================

    val balance: Flow<BalanceEntity?> =
        balanceDao.getBalance()

    suspend fun setBalance(
        amount: Long
    ) {
        balanceDao.setBalance(
            BalanceEntity(
                amount = amount
            )
        )
    }

    suspend fun addToBalance(
        amount: Long
    ) {
        balanceDao.addToBalance(amount)
    }

    suspend fun subtractFromBalance(
        amount: Long
    ) {
        balanceDao.subtractFromBalance(amount)
    }

    // =========================
    // GET EXPENSE TRANSACTIONS
    // =========================

    fun getExpenseTransactionsBetween(
        startTime: Long,
        endTime: Long
    ): Flow<List<TransactionEntity>> =
        transactionDao.getExpenseTransactionsBetween(startTime, endTime)
}
