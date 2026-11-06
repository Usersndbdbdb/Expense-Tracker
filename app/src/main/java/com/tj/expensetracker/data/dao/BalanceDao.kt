package com.tj.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tj.expensetracker.data.entity.BalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {
    @Query("SELECT * FROM balance WHERE id = 1")
    fun getBalance(): Flow<BalanceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBalance(balance: BalanceEntity)

    @Query("UPDATE balance SET amount = amount + :amount WHERE id = 1")
    suspend fun addToBalance(amount: Long)

    @Query("UPDATE balance SET amount = amount - :amount WHERE id = 1")
    suspend fun subtractFromBalance(amount: Long)

    @Query("DELETE FROM balance")
    suspend fun clearBalance()


}


