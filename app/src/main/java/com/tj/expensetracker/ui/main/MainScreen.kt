package com.tj.expensetracker.ui.main

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tj.expensetracker.data.database.AppDatabase
import com.tj.expensetracker.data.repository.DataManagementService
import com.tj.expensetracker.data.repository.TransactionRepository
import com.tj.expensetracker.ui.analytics.AnalyticsScreen
import com.tj.expensetracker.ui.balance.BalanceScreen
import com.tj.expensetracker.ui.dashboard.DashboardScreen
import com.tj.expensetracker.ui.settings.SettingsScreen
import com.tj.expensetracker.ui.settings.ThemeMode
import com.tj.expensetracker.ui.theme.AppVisualStyle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import com.tj.expensetracker.ui.theme.GlassBackground
import com.tj.expensetracker.ui.theme.GlassSurface
import com.tj.expensetracker.viewmodel.TransactionViewModel
import com.tj.expensetracker.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    visualStyle: AppVisualStyle,
    onVisualStyleChange: (AppVisualStyle) -> Unit,
    onNavigateToAddTransaction: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val database = AppDatabase.getDatabase(application)
    val repository = TransactionRepository(database.transactionDao(), database.balanceDao())
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )

    var selectedTab by remember { mutableIntStateOf(0) }

    val transactions by transactionViewModel.transactions.collectAsStateWithLifecycle()
    val analyticsFilter by transactionViewModel.analyticsFilter.collectAsStateWithLifecycle()
    val totalIncome by transactionViewModel.filteredIncome.collectAsStateWithLifecycle()
    val totalExpense by transactionViewModel.filteredExpense.collectAsStateWithLifecycle()
    val categoryExpenses by transactionViewModel.filteredCategoryExpenses.collectAsStateWithLifecycle()
    val dailyExpenses by transactionViewModel.dailyExpense.collectAsStateWithLifecycle()

    val dataService = remember(database) {
        DataManagementService(database)
    }
    val scope = rememberCoroutineScope()

    var showDataManagementDialog by remember {
        mutableStateOf(false)
    }
    var pendingRestoreUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var showRestoreConfirmation by remember {
        mutableStateOf(false)
    }
    var showClearConfirmation by remember {
        mutableStateOf(false)
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver
                            .openOutputStream(uri)
                            ?: error("Could not open the selected file.")

                        output.use {
                            dataService.exportCsv(it)
                        }
                    }
                    Toast.makeText(
                        context,
                        "CSV exported successfully",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "CSV export failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver
                            .openOutputStream(uri)
                            ?: error("Could not open the selected file.")

                        output.use {
                            dataService.createBackup(it)
                        }
                    }
                    Toast.makeText(
                        context,
                        "Backup created successfully",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Backup failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirmation = true
        }
    }

    val isGlass = visualStyle == AppVisualStyle.GLASS

    Scaffold(
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isGlass) {
                GlassSurface(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    NavigationBar(
                        tonalElevation = 0.dp,
                        containerColor = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // HOME
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") }
                        )

                        // ANALYSIS
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Analytics, contentDescription = "Analysis") },
                            label = { Text("Analysis") }
                        )

                        // SETTINGS / BALANCE
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )

                        // PROFILE
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Me") }
                        )
                    }
                }
            } else {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    // HOME
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )

                    // ANALYSIS
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = "Analysis") },
                        label = { Text("Analysis") }
                    )

                    // SETTINGS / BALANCE
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )

                    // PROFILE
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Me") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val tabContent: @Composable () -> Unit = {
                when (selectedTab) {
                    0 -> {
                        DashboardScreen(
                            transactions = transactions,
                            viewModel = transactionViewModel,
                            onAddTransaction = onNavigateToAddTransaction,
                            onAnalytics = { selectedTab = 1 },
                            onBalance = { selectedTab = 2 },
                            onDeleteTransaction = { transaction ->
                                transactionViewModel.deleteTransaction(transaction)
                            },
                            visualStyle = visualStyle
                        )
                    }
                    1 -> {
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
                            onBack = { selectedTab = 0 }
                        )
                    }
                    2 -> {
                        SettingsScreen(
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                            visualStyle = visualStyle,
                            onVisualStyleChange = onVisualStyleChange,
                            onDataManagementClick = {
                                showDataManagementDialog = true
                            }
                        )
                    }
                    3 -> {
                        BalanceScreen(
                            viewModel = transactionViewModel,
                            visualStyle = visualStyle,
                            onBack = { selectedTab = 0 }
                        )
                    }
                }
            }

            if (isGlass) {
                GlassBackground(
                    modifier = Modifier.fillMaxSize()
                ) {
                    tabContent()
                }
            } else {
                tabContent()
            }
        }
    }

    if (showDataManagementDialog) {
        AlertDialog(
            onDismissRequest = {
                showDataManagementDialog = false
            },
            title = { Text("Data Management") },
            text = {
                androidx.compose.foundation.layout.Column {
                    TextButton(
                        onClick = {
                            showDataManagementDialog = false
                            csvLauncher.launch("expense_transactions.csv")
                        }
                    ) {
                        Text("Export Transactions (CSV)")
                    }

                    TextButton(
                        onClick = {
                            showDataManagementDialog = false
                            backupLauncher.launch("expense_tracker_backup.json")
                        }
                    ) {
                        Text("Create Backup (JSON)")
                    }

                    TextButton(
                        onClick = {
                            showDataManagementDialog = false
                            restoreLauncher.launch(arrayOf("*/*"))
                        }
                    ) {
                        Text("Restore Backup")
                    }

                    TextButton(
                        onClick = {
                            showDataManagementDialog = false
                            showClearConfirmation = true
                        }
                    ) {
                        Text(
                            "Clear All Data",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDataManagementDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmation = false
                pendingRestoreUri = null
            },
            title = { Text("Restore Backup?") },
            text = {
                Text(
                    "This will replace your current transactions and saved " +
                            "balance. Make sure you have a backup of your current data."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingRestoreUri
                        showRestoreConfirmation = false
                        pendingRestoreUri = null

                        if (uri != null) {
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        val input = context.contentResolver
                                            .openInputStream(uri)
                                            ?: error("Could not open backup file.")

                                        input.use {
                                            dataService.restoreBackup(it)
                                        }
                                    }

                                    Toast.makeText(
                                        context,
                                        "Backup restored successfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Restore failed: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmation = false
                        pendingRestoreUri = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showClearConfirmation = false
            },
            title = { Text("Clear All Data?") },
            text = {
                Text(
                    "This permanently deletes all transactions and your saved " +
                            "starting balance. Export or create a backup first. " +
                            "This action cannot be undone."
                    )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false

                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    dataService.clearAllData()
                                }

                                Toast.makeText(
                                    context,
                                    "All data cleared",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Could not clear data: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                ) {
                    Text(
                        "Clear Everything",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
