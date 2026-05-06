package com.example.cuentaconmigo.features.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositAccountTransactionsScreen(
    accountName: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Transaction) -> Unit,
    viewModel: DepositAccountTransactionsViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsState()
    val formatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "CO")) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Eliminar transacción") },
            text = { Text("¿Seguro que quieres eliminar esta transacción? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(accountName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin movimientos en esta cuenta")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items, key = { it.transaction.id }) { item ->
                    DepositTransactionListItem(
                        item = item,
                        formatter = formatter,
                        onEdit = if (!item.isTransfer) ({ onNavigateToEdit(item.transaction) }) else null,
                        onDelete = { viewModel.requestDelete(item) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DepositTransactionListItem(
    item: DepositTransactionItem,
    formatter: DateTimeFormatter,
    onEdit: (() -> Unit)?,
    onDelete: () -> Unit
) {
    val tx = item.transaction
    var menuExpanded by remember { mutableStateOf(false) }

    val typeLabel = when {
        item.isTransfer -> "Transferencia"
        tx.type == TransactionType.INCOME -> "Ingreso"
        else -> "Gasto"
    }
    val amountColor = if (tx.type == TransactionType.INCOME)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error

    Box(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = { menuExpanded = true }
            ),
            headlineContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(typeLabel, style = MaterialTheme.typography.labelSmall) }
                    )
                    item.destinationName?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            supportingContent = {
                Column {
                    Text(tx.date.format(formatter), style = MaterialTheme.typography.bodySmall)
                    tx.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            },
            trailingContent = {
                Text(
                    tx.amount.toCopString(),
                    color = amountColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            if (onEdit != null) {
                DropdownMenuItem(
                    text = { Text("Editar") },
                    onClick = { menuExpanded = false; onEdit() }
                )
            }
            DropdownMenuItem(
                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                onClick = { menuExpanded = false; onDelete() }
            )
        }
    }
}