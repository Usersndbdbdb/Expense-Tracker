package com.tj.expensetracker.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tj.expensetracker.data.entity.DailyExpense
import java.util.Locale

@Composable
fun SpendingTrendChart(
    dailyExpenses: List<DailyExpense>
) {
    if (dailyExpenses.isEmpty()) {
        Text("No spending data available for this period.")
        return
    }

    val maxAmount = dailyExpenses.maxOf {
        it.totalAmount
    }.coerceAtLeast(1L)

    val totalSpent = dailyExpenses.sumOf {
        it.totalAmount
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Total: ₹%,d".format(
                Locale.US,
                totalSpent / 100
            ),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Daily expenses · Scroll horizontally to see more days",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            dailyExpenses.forEach { item ->

                val barHeight = (
                        item.totalAmount.toFloat() /
                                maxAmount.toFloat() * 120f
                        ).coerceAtLeast(3f)

                Column(
                    modifier = Modifier.width(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "₹%,d".format(
                            Locale.US,
                            item.totalAmount / 100
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )

                    Box(
                        modifier = Modifier
                            .height(130.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(barHeight.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 5.dp,
                                        topEnd = 5.dp
                                    )
                                )
                                .background(
                                    MaterialTheme.colorScheme.primary
                                )
                        )
                    }

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}
