package com.tj.expensetracker.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tj.expensetracker.data.database.AppDatabase
import com.tj.expensetracker.data.repository.TransactionRepository
import com.tj.expensetracker.ui.analytics.AnalyticsScreen
import com.tj.expensetracker.ui.main.MainScreen
import com.tj.expensetracker.ui.theme.AppVisualStyle
import com.tj.expensetracker.ui.settings.ThemeMode
import com.tj.expensetracker.ui.transaction.AddTransactionScreen
import com.tj.expensetracker.viewmodel.TransactionViewModel
import com.tj.expensetracker.viewmodel.TransactionViewModelFactory
import com.tj.expensetracker.ui.theme.GlassSurface
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.BoxScope
import java.util.Locale

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object AddTransaction : Screen("add_transaction")
    data object Analytics : Screen("analytics")
}

@Composable
fun AppNavigation(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    visualStyle: AppVisualStyle,
    onVisualStyleChange: (AppVisualStyle) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                visualStyle = visualStyle,
                onVisualStyleChange = onVisualStyleChange,
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as Application
            val database = AppDatabase.getDatabase(application)
            val repository = TransactionRepository(database.transactionDao(), database.balanceDao())
            val transactionViewModel: TransactionViewModel = viewModel(
                factory = TransactionViewModelFactory(repository)
            )

            AddTransactionScreen(
                viewModel = transactionViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Analytics.route) {
            val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as Application
            val database = AppDatabase.getDatabase(application)
            val repository = TransactionRepository(database.transactionDao(), database.balanceDao())
            val transactionViewModel: TransactionViewModel = viewModel(
                factory = TransactionViewModelFactory(repository)
            )

            val dailyExpenses by transactionViewModel.dailyExpense.collectAsStateWithLifecycle()
            val analyticsFilter by transactionViewModel.analyticsFilter.collectAsStateWithLifecycle()
            val totalIncome by transactionViewModel.filteredIncome.collectAsStateWithLifecycle()
            val totalExpense by transactionViewModel.filteredExpense.collectAsStateWithLifecycle()
            val categoryExpenses by transactionViewModel.filteredCategoryExpenses.collectAsStateWithLifecycle()
            val isGlass = visualStyle == AppVisualStyle.GLASS

            AnalyticsScreen(
                dailyExpenses = dailyExpenses,
                analyticsFilter = analyticsFilter,
                onFilterChange = { filter ->
                    transactionViewModel.setAnalyticsFilter(filter)
                },
                onCustomRangeChange = { start, end ->
                    transactionViewModel.setCustomDateRange(start, end)
                },
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                categoryExpenses = categoryExpenses,
                visualStyle = visualStyle,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
