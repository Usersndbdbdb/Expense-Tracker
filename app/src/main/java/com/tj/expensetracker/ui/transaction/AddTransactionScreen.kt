package com.tj.expensetracker.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tj.expensetracker.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var transactionType by remember {
        mutableStateOf("Expense")
    }

    var category by remember {
        mutableStateOf("Food")
    }

    var account by remember {
        mutableStateOf("Cash")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Add Transaction")
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Amount",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Enter amount")
                },
                prefix = {
                    Text("₹ ")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Transaction Type",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (transactionType == "Expense") {

                    Button(
                        onClick = {
                            transactionType = "Expense"
                        }
                    ) {
                        Text("Expense")
                    }

                    OutlinedButton(
                        onClick = {
                            transactionType = "Income"
                        }
                    ) {
                        Text("Income")
                    }

                } else {

                    OutlinedButton(
                        onClick = {
                            transactionType = "Expense"
                        }
                    ) {
                        Text("Expense")
                    }

                    Button(
                        onClick = {
                            transactionType = "Income"
                        }
                    ) {
                        Text("Income")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                CategoryButton(
                    name = "Food",
                    selected = category == "Food"
                ) {
                    category = "Food"
                }

                CategoryButton(
                    name = "Travel",
                    selected = category == "Travel"
                ) {
                    category = "Travel"
                }

                CategoryButton(
                    name = "Other",
                    selected = category == "Other"
                ) {
                    category = "Other"
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                AccountButton(
                    name = "Cash",
                    selected = account == "Cash"
                ) {
                    account = "Cash"
                }

                AccountButton(
                    name = "UPI",
                    selected = account == "UPI"
                ) {
                    account = "UPI"
                }

                AccountButton(
                    name = "Bank",
                    selected = account == "Bank"
                ) {
                    account = "Bank"
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Note",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Optional note")
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {

                    val amountRupees =
                        amount.toDoubleOrNull()

                    if (amountRupees != null &&
                        amountRupees > 0
                    ) {

                        val amountPaise =
                            (amountRupees * 100).toLong()

                        viewModel.addTransaction(
                            amount = amountPaise,
                            type = transactionType,
                            category = category,
                            account = account,
                            note = note.ifBlank {
                                null
                            }
                        )

                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text("Save Transaction")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun CategoryButton(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick) {
            Text(name)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(name)
        }
    }
}

@Composable
private fun AccountButton(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick) {
            Text(name)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(name)
        }
    }
}