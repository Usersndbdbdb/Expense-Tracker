package com.tj.expensetracker.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.tj.expensetracker.data.entity.AnalyticsFilter
import com.tj.expensetracker.data.entity.CategoryExpense
import com.tj.expensetracker.data.entity.DailyExpense
import com.tj.expensetracker.ui.theme.AppVisualStyle
import com.tj.expensetracker.ui.theme.GlassSurface
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.BoxScope
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    dailyExpenses: List<DailyExpense>,
    analyticsFilter: AnalyticsFilter,
    onFilterChange: (AnalyticsFilter) -> Unit,
    onCustomRangeChange: (Long, Long) -> Unit,
    totalIncome: Long,
    totalExpense: Long,
    categoryExpenses: List<CategoryExpense>,
    visualStyle: AppVisualStyle,
    onBack: () -> Unit
) {
    val netChange = totalIncome - totalExpense
    val isGlass = visualStyle == AppVisualStyle.GLASS

    var filterMenuExpanded by remember {
        mutableStateOf(false)
    }

    var showDateRangePicker by remember {
        mutableStateOf(false)
    }

    val dateRangePickerState = rememberDateRangePickerState()

    fun formatAmount(amountPaise: Long): String {
        val rupees = amountPaise / 100
        return "₹%,d".format(Locale.US, rupees)
    }

    if (showDateRangePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDateRangePicker = false
            },
            confirmButton = {
                TextButton(
                    enabled =
                        dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null,
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis

                        if (start != null && end != null && end >= start) {
                            onCustomRangeChange(start, end)
                            showDateRangePicker = false
                        }
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDateRangePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.height(500.dp),
                title = {
                    Text(
                        "Select date range",
                        modifier = Modifier.padding(16.dp)
                    )
                },
                showModeToggle = false
            )
        }
    }

    Scaffold(
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Analytics \uD83D\uDCC8")
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // -------------------------
            // FILTER DROPDOWN
            // -------------------------
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            filterMenuExpanded = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when (analyticsFilter) {
                                AnalyticsFilter.THIS_MONTH -> "This Month"
                                AnalyticsFilter.LAST_MONTH -> "Last Month"
                                AnalyticsFilter.ALL_TIME -> "All Time"
                                AnalyticsFilter.CUSTOM -> "Custom Range"
                            }
                        )
                    }

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = {
                            filterMenuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text("This Month") },
                            onClick = {
                                onFilterChange(AnalyticsFilter.THIS_MONTH)
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Last Month") },
                            onClick = {
                                onFilterChange(AnalyticsFilter.LAST_MONTH)
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("All Time") },
                            onClick = {
                                onFilterChange(AnalyticsFilter.ALL_TIME)
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Custom Range") },
                            onClick = {
                                filterMenuExpanded = false
                                showDateRangePicker = true
                            }
                        )
                    }
                }
            }

            // -------------------------
            // SUMMARY OVERVIEW
            // -------------------------
            item {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Summary Overview")
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    AnalyticsSurface(
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

                    AnalyticsSurface(
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

            // -------------------------
            // NET CHANGE
            // -------------------------
            item {

                AnalyticsSurface(
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
                            text = "Net Change",
                            textAlign = TextAlign.Center
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = formatAmount(netChange),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = if (netChange >= 0) {

                                " gained more than spent. \uD83D\uDE0E "
                            } else {
                                "spent more , AURA LOSS . \uD83D\uDDFF"
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // -------------------------
            // SPENDING TREND
            // -------------------------
            item {
                Text(
                    text = "Spending Trend",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                AnalyticsSurface(
                    isGlass = isGlass,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        SpendingTrendChart(
                            dailyExpenses = dailyExpenses
                        )
                    }
                }
            }

            // -------------------------
            // CATEGORY EXPENSES TITLE
            // -------------------------
            item {
                Text(
                    text = "Expense Breakdown",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (categoryExpenses.isEmpty()) {
                item {
                    AnalyticsSurface(
                        isGlass = isGlass,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text("No expense data yet.")

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                "Add expense transactions to see your category breakdown."
                            )
                        }
                    }
                }
            } else {
                item {
                    AnalyticsSurface(
                        isGlass = isGlass,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            ExpensePieChart(
                                categoryExpenses = categoryExpenses
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Category Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                items(
                    items = categoryExpenses,
                    key = { it.category }
                ) { item ->
                    val categoryTotal =
                        categoryExpenses.sumOf { it.totalAmount }

                    val percentage =
                        item.totalAmount.toDouble() /
                                categoryTotal.toDouble() * 100.0

                    AnalyticsSurface(
                        isGlass = isGlass,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.category,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%.1f%% of expenses",
                                        percentage
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Text(
                                text = formatAmount(item.totalAmount),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsSurface(
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
