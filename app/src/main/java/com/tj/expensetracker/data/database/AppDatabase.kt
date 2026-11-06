package com.tj.expensetracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tj.expensetracker.data.dao.BalanceDao
import com.tj.expensetracker.data.dao.TransactionDao
import com.tj.expensetracker.data.entity.BalanceEntity
import com.tj.expensetracker.data.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class,
        BalanceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun balanceDao(): BalanceDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS balance (
                        id INTEGER NOT NULL,
                        amount INTEGER NOT NULL,
                        PRIMARY KEY(id)
                        )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}