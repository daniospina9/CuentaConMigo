package com.example.cuentaconmigo.features.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.AccountPercentage
import com.example.cuentaconmigo.features.main.Routes

@Composable
fun ReportsContent(
    userId: Long,
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
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
                    ExpenseRow(
                        item = item,
                        onClick = {
                            val (startDay, endDay) = viewModel.currentDateRange()
                            navController.navigate(
                                Routes.accountTransactions(
                                    destinationAccountId = item.destinationAccountId,
                                    accountName = item.destinationAccountName,
                                    startDay = startDay,
                                    endDay = endDay
                                )
                            )
                        }
                    )
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
                OutlinedButton(
                    onClick = { navController.navigate(Routes.financialReport(userId)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("Generar informe personalizado")
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(item: AccountPercentage, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(item.destinationAccountName) },
        supportingContent = {
            LinearProgressIndicator(
                progress = { item.percentage / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(item.total.toCopString(), style = MaterialTheme.typography.bodyMedium)
                    Text("${item.percentage}%", style = MaterialTheme.typography.bodySmall)
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
