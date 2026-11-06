package com.tj.expensetracker.data.entity

data class DailyExpense(
    val dayStart: Long,
    val label: String,
    val totalAmount: Long
)