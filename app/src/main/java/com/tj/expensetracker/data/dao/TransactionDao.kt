package com.tj.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.tj.expensetracker.data.entity.CategoryExpense
import com.tj.expensetracker.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // -------------------------
    // TRANSACTIONS
    // -------------------------

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(
        transactions: List<TransactionEntity>
    )

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions ORDER BY timestamp ASC")
    suspend fun getTransactionsForBackup(): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // -------------------------
    // ANALYTICS
    // -------------------------

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = 'Income'
        """
    )
    fun getTotalIncome(): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = 'Expense'
        """
    )
    fun getTotalExpense(): Flow<Long>

    @Query(
        """
       SELECT category, SUM(amount) AS totalAmount
       FROM transactions
       WHERE type = 'Expense'
       GROUP BY category
       ORDER BY totalAmount DESC
        """
    )
    fun getExpenseByCategory(): Flow<List<CategoryExpense>>

    @Query("""
        SELECT * FROM transactions
        WHERE type = 'Expense'
        AND timestamp >= :startTime
        AND timestamp < :endTime
        ORDER BY timestamp ASC
    """) fun getExpenseTransactionsBetween(
        startTime: Long,
        endTime: Long
    ): Flow<List<TransactionEntity>>

    // -------------------------
    // FILTERED ANALYTICS
    // -------------------------

    @Query(
        """
    SELECT COALESCE(SUM(amount), 0)
    FROM transactions
    WHERE type = 'Income'
    AND timestamp >= :startTime
    AND timestamp < :endTime
    """
    )
    fun getIncomeBetween(
        startTime: Long,
        endTime: Long
    ): Flow<Long>

    @Query(
        """
    SELECT COALESCE(SUM(amount), 0)
    FROM transactions
    WHERE type = 'Expense'
    AND timestamp >= :startTime
    AND timestamp < :endTime
    """
    )
    fun getExpenseBetween(
        startTime: Long,
        endTime: Long
    ): Flow<Long>

    @Query(
        """
    SELECT
        category,
        SUM(amount) AS totalAmount
    FROM transactions
    WHERE type = 'Expense'
    AND timestamp >= :startTime
    AND timestamp < :endTime
    GROUP BY category
    ORDER BY totalAmount DESC
    """
    )
    fun getExpenseByCategoryBetween(
        startTime: Long,
        endTime: Long
    ): Flow<List<CategoryExpense>>
}
