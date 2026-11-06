package com.tj.expensetracker.ui.balance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import com.tj.expensetracker.ui.theme.AppVisualStyle
import com.tj.expensetracker.ui.theme.GlassSurface
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tj.expensetracker.viewmodel.TransactionViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(
    viewModel: TransactionViewModel,
    visualStyle: AppVisualStyle,
    onBack: () -> Unit
) {
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val isGlass = visualStyle == AppVisualStyle.GLASS

    var amount by remember {
        mutableStateOf("")
    }

    fun formatAmount(amountPaise: Long): String {
        val rupees = amountPaise / 100
        return "₹%,d".format(Locale.US, rupees)
    }

    Scaffold(
        containerColor = if (isGlass) Color.Transparent else androidx.compose.material3.MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(),
                        contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Balance Setting")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isGlass) Color.Transparent else androidx.compose.material3.MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    androidx.compose.material3.TextButton(
                        onClick = onBack
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            BalanceSurface(
                isGlass = isGlass,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text("Bank Balance")

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = formatAmount(balance)
                    )
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Amount")
                },
                placeholder = {
                    Text("Enter amount")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = {
                        val rupees = amount.toDoubleOrNull()

                        if (rupees != null && rupees > 0) {
                            val amountPaise =
                                (rupees * 100).toLong()

                            viewModel.addToBalance(
                                amountPaise
                            )

                            amount = ""
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Money")
                }

                OutlinedButton(
                    onClick = {
                        val rupees = amount.toDoubleOrNull()

                        if (rupees != null && rupees > 0) {
                            val amountPaise =
                                (rupees * 100).toLong()

                            if (amountPaise <= balance) {
                                viewModel.subtractFromBalance(
                                    amountPaise
                                )

                                amount = ""
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remove Money")
                }
            }

            Button(
                onClick = {
                    val rupees = amount.toDoubleOrNull()

                    if (rupees != null && rupees >= 0) {
                        val amountPaise =
                            (rupees * 100).toLong()

                        viewModel.setBalance(
                            amountPaise
                        )

                        amount = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set / Edit Balance")
            }
        }
    }
}

@Composable
private fun BalanceSurface(
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

