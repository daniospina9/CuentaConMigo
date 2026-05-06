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
import com.example.cuentaconmigo.domain.model.SavingsMovement
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsSubAccountDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: SavingsSubAccountDetailViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsState()
    val movements by viewModel.movements.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val depositAccounts by viewModel.depositAccounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var movementToDelete by remember { mutableStateOf<SavingsMovement?>(null) }

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
                Text(
                    "Movimientos",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (movements.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Sin retiros registrados.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(movements, key = { it.id }) { movement ->
                    SavingsMovementRow(movement = movement, onDelete = { movementToDelete = movement })
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

    movementToDelete?.let { mv ->
        AlertDialog(
            onDismissRequest = { movementToDelete = null },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Eliminar este registro de ${mv.amount.toSignedCopString()}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteMovement(mv); movementToDelete = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { movementToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    errorMessage?.let { msg ->
        LaunchedEffect(msg) { viewModel.clearError() }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
    }
}

@Composable
private fun SavingsMovementRow(movement: SavingsMovement, onDelete: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val isDeposit = movement.amount >= 0

    ListItem(
        headlineContent = {
            Text(
                movement.amount.toSignedCopString(),
                color = if (isDeposit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        supportingContent = {
            val label = if (isDeposit) "Depósito" else "Retiro"
            val desc = movement.description?.let { " · $it" } ?: ""
            Text("$label · ${movement.date.format(formatter)}$desc", style = MaterialTheme.typography.bodySmall)
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