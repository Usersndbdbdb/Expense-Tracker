package com.tj.expensetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val amount: Long,

    val type: String,

    val timestamp: Long,

    val category: String,

    val account: String,

    val note: String? = null
)