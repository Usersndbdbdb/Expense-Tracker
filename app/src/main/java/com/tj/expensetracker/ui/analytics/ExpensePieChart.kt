
package com.tj.expensetracker.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.tj.expensetracker.data.entity.CategoryExpense
import java.util.Locale

@Composable
fun ExpensePieChart(
    categoryExpenses: List<CategoryExpense>
) {
    val total = categoryExpenses.sumOf { it.totalAmount }

    if (total <= 0L) {
        Text("No expense data available.")
        return
    }

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier.size(210.dp)
        ) {
            val strokeWidth = size.minDimension * 0.20f
            val diameter = size.minDimension - strokeWidth
            val topLeftX = (size.width - diameter) / 2f
            val topLeftY = (size.height - diameter) / 2f

            var startAngle = -90f

            categoryExpenses.forEachIndexed { index, item ->
                val sweepAngle =
                    item.totalAmount.toFloat() / total.toFloat() * 360f

                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        topLeftX, topLeftY
                    ),
                    size = Size(diameter, diameter),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Butt
                    )
                )

                startAngle += sweepAngle
            }
        }

        Spacer(Modifier.height(20.dp))

        categoryExpenses.forEachIndexed { index, item ->
            val percentage =
                item.totalAmount.toDouble() / total.toDouble() * 100.0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(Modifier.size(12.dp)) {
                    drawCircle(colors[index % colors.size])
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = item.category,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = String.format(
                        Locale.US, "%.1f%%", percentage
                    )
                )
            }
        }
    }
}
