package com.tj.expensetracker.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tj.expensetracker.data.entity.TransactionEntity
import com.tj.expensetracker.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.tj.expensetracker.ui.theme.AppVisualStyle
import com.tj.expensetracker.ui.theme.GlassBackground
import com.tj.expensetracker.ui.theme.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    transactions: List<TransactionEntity>,
    viewModel: TransactionViewModel,
    onAddTransaction: () -> Unit,
    onAnalytics: () -> Unit,
    onBalance: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    visualStyle: AppVisualStyle
) {

    var transactionToDelete by remember {
        mutableStateOf<TransactionEntity?>(null)
    }

    // =========================
    // TOTALS
    // =========================

    val bankBalance by viewModel.balance.collectAsStateWithLifecycle()

    // Calculate total income
    val totalIncome = transactions
        .filter { it.type == "Income" }
        .sumOf { it.amount }

    // Calculate total expense
    val totalExpense = transactions
        .filter { it.type == "Expense" }
        .sumOf { it.amount }

    // Balance = Income - Expense
    val currentBalance = bankBalance + totalIncome - totalExpense

    // Convert paise to rupees
    fun formatAmount(amountPaise: Long): String {
        val rupees = amountPaise / 100
        return "₹%,d".format(Locale.US, rupees)
    }

    val isGlass = visualStyle == AppVisualStyle.GLASS

    Scaffold(
        containerColor = if (isGlass) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.background
        },

        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = null
                        )
                        Text("TJ's Expense Tracker")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isGlass) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction"
                )
            }
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val listContent: @Composable () -> Unit = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // =========================
                    // CURRENT BALANCE
                    // =========================

                    item {
                        DashboardSurface(
                            isGlass = isGlass,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Current Balance",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = formatAmount(currentBalance),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = "Bank: ${formatAmount(bankBalance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // =========================
                    // INCOME + EXPENSE
                    // =========================

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // INCOME
                            DashboardSurface(
                                isGlass = isGlass,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Income")

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(
                                        text = formatAmount(totalIncome)
                                    )
                                }
                            }

                            // EXPENSE
                            DashboardSurface(
                                isGlass = isGlass,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Expense")

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(
                                        text = formatAmount(totalExpense)
                                    )
                                }
                            }
                        }
                    }

                    // =========================
                    // REAL TRANSACTIONS
                    // =========================

                    items(
                        items = transactions,
                        key = { transaction ->
                            transaction.id
                        }
                    ) { transaction ->

                        DashboardSurface(
                            isGlass = isGlass,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),

                                verticalAlignment =
                                    Alignment.CenterVertically,

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Column {

                                    // Category
                                    Text(
                                        text = transaction.category
                                    )

                                    // Note
                                    if (!transaction.note.isNullOrBlank()) {

                                        Text(
                                            text = transaction.note
                                        )
                                    }

                                    // Date + Time
                                    Text(
                                        text = SimpleDateFormat(
                                            "dd MMM, hh:mm a",
                                            Locale.getDefault()
                                        ).format(
                                            Date(transaction.timestamp)
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Amount
                                    Text(
                                        text = if (transaction.type == "Expense") {
                                            "-${formatAmount(transaction.amount)}"
                                        } else {
                                            "+${formatAmount(transaction.amount)}"
                                        }
                                    )

                                    // DELETE BUTTON
                                    IconButton(
                                        onClick = {
                                            transactionToDelete = transaction
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Transaction"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isGlass) {
                GlassBackground(
                    modifier = Modifier.fillMaxSize()
                ) {
                    listContent()
                }
            } else {
                listContent()
            }
        }
    }

    // =========================
    // DELETE CONFIRMATION
    // =========================
    if (transactionToDelete != null) {

        AlertDialog(

            onDismissRequest = {
                transactionToDelete = null
            },

            title = {
                Text("Really !! ? \uD83E\uDDD0" )
            },

            text = {
                Text(
                    "Are you sure , Think again Bro !!"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        transactionToDelete?.let { transaction ->

                            onDeleteTransaction(transaction)
                        }

                        transactionToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        transactionToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DashboardSurface(
    isGlass: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    if (isGlass) {
        GlassSurface(
            modifier = modifier,
            content = content
        )
    } else {
        Card(modifier = modifier) {
            Box(content = content)
        }
    }
}
