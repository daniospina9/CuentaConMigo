package com.example.cuentaconmigo.features.savings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.filterAmountInput
import com.example.cuentaconmigo.core.util.parseToCentavos
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.core.util.toSignedCopString
import com.example.cuentaconmigo.domain.model.DepositAccount
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsSubAccountDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: SavingsSubAccountDetailViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val depositAccounts by viewModel.depositAccounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<SavingsEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showWithdrawDialog = true }) {
                Text("−", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Saldo disponible", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(balance.toCopString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showExpenseDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text("Registrar gasto")
                }
            }

            item {
                Text(
                    "Movimientos",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (allEntries.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Sin movimientos registrados.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(
                    allEntries,
                    key = { entry ->
                        when (entry) {
                            is SavingsEntry.Movement -> "m_${entry.source.id}"
                            is SavingsEntry.Deposit -> "d_${entry.source.id}"
                        }
                    }
                ) { entry ->
                    SavingsEntryRow(entry = entry, onDelete = { entryToDelete = entry })
                    HorizontalDivider()
                }
            }
        }
    }

    if (showWithdrawDialog) {
        SavingsMovementDialog(
            title = "Retirar",
            confirmLabel = "Retirar",
            depositAccounts = depositAccounts,
            onConfirm = { amount, accountId, desc ->
                viewModel.withdraw(amount, accountId, desc)
                showWithdrawDialog = false
            },
            onDismiss = { showWithdrawDialog = false }
        )
    }

    if (showExpenseDialog) {
        SavingsExpenseDialog(
            onConfirm = { amount, desc ->
                viewModel.recordExpense(amount, desc)
                showExpenseDialog = false
            },
            onDismiss = { showExpenseDialog = false }
        )
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Eliminar este registro de ${entry.amount.toSignedCopString()}?") },
            confirmButton = {
                TextButton(onClick = {
                    when (entry) {
                        is SavingsEntry.Movement -> viewModel.deleteMovement(entry.source)
                        is SavingsEntry.Deposit -> viewModel.deleteDeposit(entry.source)
                    }
                    entryToDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    errorMessage?.let { msg ->
        LaunchedEffect(msg) { viewModel.clearError() }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
    }
}

@Composable
private fun SavingsEntryRow(entry: SavingsEntry, onDelete: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val isPositive = entry.amount >= 0
    val label = when (entry) {
        is SavingsEntry.Deposit -> "Depósito"
        is SavingsEntry.Movement -> when {
            entry.source.groupId != null -> "Retiro"
            else -> "Gasto"
        }
    }

    ListItem(
        headlineContent = {
            Text(
                entry.amount.toSignedCopString(),
                color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        supportingContent = {
            val desc = entry.description?.let { " · $it" } ?: ""
            Text("$label · ${entry.date.format(formatter)}$desc", style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavingsMovementDialog(
    title: String,
    confirmLabel: String,
    depositAccounts: List<DepositAccount>,
    onConfirm: (amount: Long, depositAccountId: Long, description: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var amountTfv by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }
    var selectedAccount by remember(depositAccounts) {
        mutableStateOf(depositAccounts.firstOrNull())
    }
    var menuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountTfv,
                    onValueChange = { new -> amountTfv = filterAmountInput(amountTfv, new) },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Sin cuentas",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cuenta de depósito") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        depositAccounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = { selectedAccount = account; menuExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val amount = amountTfv.text.parseToCentavos() ?: 0L
            TextButton(
                onClick = {
                    selectedAccount?.let { acc -> acc.id?.let { id -> onConfirm(amount, id, description.ifBlank { null }) } }
                },
                enabled = amount > 0 && selectedAccount?.id != null
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun SavingsExpenseDialog(
    onConfirm: (amount: Long, description: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var amountTfv by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountTfv,
                    onValueChange = { new -> amountTfv = filterAmountInput(amountTfv, new) },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val amount = amountTfv.text.parseToCentavos() ?: 0L
            TextButton(
                onClick = { onConfirm(amount, description.ifBlank { null }) },
                enabled = amount > 0
            ) { Text("Registrar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}