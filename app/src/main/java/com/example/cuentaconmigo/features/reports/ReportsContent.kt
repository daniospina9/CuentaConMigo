package com.example.cuentaconmigo.features.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.core.util.toSignedCopString
import com.example.cuentaconmigo.domain.model.AccountPercentage

@Composable
fun ReportsContent(viewModel: ReportsViewModel = hiltViewModel()) {
    val reportState by viewModel.reportState.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = dateFilter == DateFilter.ALL_TIME,
                onClick = { viewModel.setDateFilter(DateFilter.ALL_TIME) },
                label = { Text("Todo el historial") }
            )
            FilterChip(
                selected = dateFilter == DateFilter.THIS_MONTH,
                onClick = { viewModel.setDateFilter(DateFilter.THIS_MONTH) },
                label = { Text("Este mes") }
            )
        }

        if (reportState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    "Gastos por categoría",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (reportState.expensePercentages.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin gastos en el período seleccionado")
                    }
                }
            } else {
                items(reportState.expensePercentages) { item ->
                    ExpenseRow(item)
                    HorizontalDivider()
                }
                item {
                    val grandTotal = reportState.expenseTotals.sumOf { it.total }
                    ListItem(
                        headlineContent = {
                            Text("Total", style = MaterialTheme.typography.titleSmall)
                        },
                        trailingContent = {
                            Text(grandTotal.toCopString(), style = MaterialTheme.typography.titleSmall)
                        }
                    )
                    HorizontalDivider()
                }
            }

            item {
                Text(
                    "Inversiones",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Balance total")
                        Text(
                            reportState.investmentBalance.toSignedCopString(),
                            color = if (reportState.investmentBalance >= 0)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(item: AccountPercentage) {
    ListItem(
        headlineContent = { Text(item.destinationAccountName) },
        supportingContent = {
            LinearProgressIndicator(
                progress = { item.percentage / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(item.total.toCopString(), style = MaterialTheme.typography.bodyMedium)
                Text("${item.percentage}%", style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}
