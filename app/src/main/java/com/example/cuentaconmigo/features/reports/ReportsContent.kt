package com.example.cuentaconmigo.features.reports

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.AccountPercentage
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.features.main.Routes

@Composable
fun ReportsContent(
    userId: Long,
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val reportState by viewModel.reportState.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val destinationAccounts by viewModel.destinationAccounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var menuForAccountId by remember { mutableStateOf<Long?>(null) }
    var accountToEdit by remember { mutableStateOf<DestinationAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<DestinationAccount?>(null) }
    var orphanIdToForceDelete by remember { mutableStateOf<Long?>(null) }

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
                    val fullAccount = destinationAccounts.find { it.id == item.destinationAccountId }
                    val isOrphaned = fullAccount == null
                    val isEditable = fullAccount?.isDefault == false
                    Box {
                        ExpenseRow(
                            item = item,
                            onClick = {
                                val (startDay, endDay) = viewModel.currentDateRange()
                                navController.navigate(
                                    Routes.accountTransactions(
                                        userId = userId,
                                        destinationAccountId = item.destinationAccountId,
                                        accountName = item.destinationAccountName,
                                        startDay = startDay,
                                        endDay = endDay
                                    )
                                )
                            },
                            onLongClick = {
                                if (isOrphaned || isEditable) menuForAccountId = item.destinationAccountId
                            }
                        )
                        DropdownMenu(
                            expanded = menuForAccountId == item.destinationAccountId,
                            onDismissRequest = { menuForAccountId = null }
                        ) {
                            if (isOrphaned) {
                                DropdownMenuItem(
                                    text = { Text("Forzar eliminación", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        orphanIdToForceDelete = item.destinationAccountId
                                        menuForAccountId = null
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Editar nombre") },
                                    onClick = {
                                        accountToEdit = fullAccount
                                        menuForAccountId = null
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        accountToDelete = fullAccount
                                        menuForAccountId = null
                                    }
                                )
                            }
                        }
                    }
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

    orphanIdToForceDelete?.let { accountId ->
        AlertDialog(
            onDismissRequest = { orphanIdToForceDelete = null },
            title = { Text("Forzar eliminación") },
            text = { Text("Esta cuenta no existe pero tiene transacciones registradas. ¿Eliminar la cuenta y todas sus transacciones?") },
            confirmButton = {
                TextButton(onClick = { viewModel.forceDeleteOrphaned(accountId); orphanIdToForceDelete = null }) {
                    Text("Eliminar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { orphanIdToForceDelete = null }) { Text("Cancelar") }
            }
        )
    }

    accountToEdit?.let { account ->
        EditAccountNameDialog(
            initialName = account.name,
            onConfirm = { name -> viewModel.updateAccount(account, name); accountToEdit = null },
            onDismiss = { accountToEdit = null }
        )
    }

    accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("Eliminar cuenta") },
            text = { Text("¿Eliminar \"${account.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAccount(account); accountToDelete = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    errorMessage?.let { msg ->
        LaunchedEffect(msg) { delay(3_000); viewModel.clearError() }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpenseRow(item: AccountPercentage, onClick: () -> Unit, onLongClick: () -> Unit) {
    ListItem(
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
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

@Composable
private fun EditAccountNameDialog(initialName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar nombre") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
