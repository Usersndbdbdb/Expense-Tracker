
package com.tj.expensetracker.data.repository

import androidx.room.withTransaction
import com.tj.expensetracker.data.database.AppDatabase
import com.tj.expensetracker.data.entity.BalanceEntity
import com.tj.expensetracker.data.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.BufferedReader
import java.nio.charset.StandardCharsets

class DataManagementService(
    private val database: AppDatabase
) {
    private val transactionDao = database.transactionDao()
    private val balanceDao = database.balanceDao()

    suspend fun exportCsv(outputStream: OutputStream) {
        val transactions = transactionDao.getTransactionsForBackup()

        BufferedWriter(
            OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        ).use { writer ->
            writer.write("ID,Date,Type,Amount (INR),Category,Account,Note")
            writer.newLine()

            transactions.forEach { transaction ->
                val fields = listOf(
                    transaction.id.toString(),
                    transaction.timestamp.toString(),
                    transaction.type,
                    "%.2f".format(
                        java.util.Locale.US,
                        transaction.amount / 100.0
                    ),
                    transaction.category,
                    transaction.account,
                    transaction.note.orEmpty()
                )

                writer.write(fields.joinToString(",") { csvEscape(it) })
                writer.newLine()
            }
        }
    }

    private fun csvEscape(value: String): String {
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    suspend fun createBackup(outputStream: OutputStream) {
        val transactions = transactionDao.getTransactionsForBackup()
        val balance = balanceDao.getBalance().first()

        val transactionArray = JSONArray()

        transactions.forEach { transaction ->
            transactionArray.put(
                JSONObject().apply {
                    put("id", transaction.id)
                    put("amount", transaction.amount)
                    put("type", transaction.type)
                    put("timestamp", transaction.timestamp)
                    put("category", transaction.category)
                    put("account", transaction.account)
                    put("note", transaction.note ?: JSONObject.NULL)
                }
            )
        }

        val balanceObject = balance?.let {
            JSONObject().apply {
                put("id", it.id)
                put("amount", it.amount)
            }
        } ?: JSONObject.NULL

        val backup = JSONObject().apply {
            put("format", "expense_tracker_backup")
            put("version", 1)
            put("transactions", transactionArray)
            put("balance", balanceObject)
        }

        outputStream.writer(StandardCharsets.UTF_8).use { writer ->
            writer.write(backup.toString(2))
        }
    }

    suspend fun restoreBackup(inputStream: InputStream) {
        val jsonText = BufferedReader(
            InputStreamReader(inputStream, StandardCharsets.UTF_8)
        ).use { it.readText() }

        require(jsonText.isNotBlank()) {
            "There is no data in the file Bro."
        }

        val backup = JSONObject(jsonText)

        require(backup.optString("format") == "expense_tracker_backup") {
            "This is not an Expense Tracker backup file."
        }

        require(backup.optInt("version", -1) == 1) {
            "Unsupported backup version."
        }

        require(backup.has("transactions") &&
                backup.opt("transactions") is JSONArray) {
            "The backup does not contain a valid transactions list."
        }

        require(backup.has("balance")) {
            "The backup does not contain balance information."
        }

        val transactionArray = backup.getJSONArray("transactions")
        val restoredTransactions = mutableListOf<TransactionEntity>()
        val seenIds = mutableSetOf<Int>()

        for (index in 0 until transactionArray.length()) {
            val item = transactionArray.optJSONObject(index)
                ?: throw IllegalArgumentException(
                    "Invalid transaction at item ${index + 1}."
                )

            require(item.has("id") && !item.isNull("id")) {
                "A transaction is missing its ID."
            }
            require(item.has("amount") && !item.isNull("amount")) {
                "A transaction is missing its amount."
            }
            require(item.has("timestamp") && !item.isNull("timestamp")) {
                "A transaction is missing its timestamp."
            }

            val id = item.getInt("id")
            val amount = item.getLong("amount")
            val type = item.optString("type")
            val timestamp = item.getLong("timestamp")
            val category = item.optString("category")
            val account = item.optString("account")
            val note = if (item.isNull("note")) {
                null
            } else {
                item.optString("note")
            }

            require(id > 0 && seenIds.add(id)) {
                "The backup contains an invalid or duplicate transaction ID."
            }
            require(amount >= 0L) {
                "A transaction has an invalid amount."
            }
            require(type == "Income" || type == "Expense") {
                "A transaction has an unsupported type."
            }
            require(category.isNotBlank() && account.isNotBlank()) {
                "A transaction has a missing category or account."
            }

            restoredTransactions.add(
                TransactionEntity(
                    id = id,
                    amount = amount,
                    type = type,
                    timestamp = timestamp,
                    category = category,
                    account = account,
                    note = note
                )
            )
        }

        val balanceValue = backup.opt("balance")
        val restoredBalance: BalanceEntity? = when {
            balanceValue == JSONObject.NULL -> null

            balanceValue is JSONObject -> {
                require(
                    balanceValue.has("amount") &&
                            !balanceValue.isNull("amount")
                ) {
                    "The backup balance is invalid."
                }

                val amount = balanceValue.getLong("amount")
                require(amount >= 0L) {
                    "The backup contains an invalid balance."
                }

                BalanceEntity(
                    id = 1,
                    amount = amount
                )
            }

            else -> throw IllegalArgumentException(
                "The backup balance has an invalid format."
            )
        }

        // Replace both tables atomically: if an operation fails,
        // Room rolls back the database transaction.
        database.withTransaction {
            transactionDao.deleteAllTransactions()

            if (restoredTransactions.isNotEmpty()) {
                transactionDao.insertTransactions(restoredTransactions)
            }

            balanceDao.clearBalance()

            if (restoredBalance != null) {
                balanceDao.setBalance(restoredBalance)
            }
        }
    }

    suspend fun clearAllData() {
        database.withTransaction {
            transactionDao.deleteAllTransactions()
            balanceDao.clearBalance()
        }
    }
}
