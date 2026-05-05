package com.example.cuentaconmigo.features.investments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.filterAmountInput
import com.example.cuentaconmigo.core.util.parseToCentavos
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.core.util.toSignedCopString
import com.example.cuentaconmigo.domain.model.AssetLiability
import com.example.cuentaconmigo.domain.model.AssetOperation
import com.example.cuentaconmigo.domain.model.AssetOperationType
import com.example.cuentaconmigo.domain.model.DepositAccount
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetSubAccountDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssetSubAccountDetailViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsState()
    val assetValue by viewModel.assetValue.collectAsState()
    val accountBalance by viewModel.accountBalance.collectAsState()
    val pendingLiabilities by viewModel.pendingLiabilities.collectAsState()
    val history by viewModel.history.collectAsState()
    val depositAccounts by viewModel.depositAccounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var fabExpanded by remember { mutableStateOf(false) }

    // Dialog states
    var showEditInitialValueDialog by remember { mutableStateOf(false) }
    var showInvestDialog by remember { mutableStateOf(false) }
    var showAssetIncomeDialog by remember { mutableStateOf(false) }
    var showCreateLiabilityDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var liabilityToPay by remember { mutableStateOf<AssetLiability?>(null) }
    var operationToDelete by remember { mutableStateOf<AssetOperation?>(null) }
    var liabilityToDelete by remember { mutableStateOf<AssetLiability?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditInitialValueDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar valuación inicial")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (fabExpanded) {
                    SmallFloatingActionButton(onClick = {
                        fabExpanded = false
                        showWithdrawDialog = true
                    }) {
                        Text("Retirar", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                    SmallFloatingActionButton(onClick = {
                        fabExpanded = false
                        showCreateLiabilityDialog = true
                    }) {
                        Text("Crear pasivo", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                    SmallFloatingActionButton(onClick = {
                        fabExpanded = false
                        showAssetIncomeDialog = true
                    }) {
                        Text("Ingreso del activo", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                    SmallFloatingActionButton(onClick = {
                        fabExpanded = false
                        showInvestDialog = true
                    }) {
                        Text("Invertir en activo", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
                FloatingActionButton(onClick = { fabExpanded = !fabExpanded }) {
                    Icon(Icons.Default.Add, contentDescription = if (fabExpanded) "Cerrar menú" else "Abrir menú")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Valor del activo card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "VALOR DEL ACTIVO",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            assetValue.toCopString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Balance de cuenta card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("BALANCE DE CUENTA", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            accountBalance.toCopString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Pasivos pendientes
            if (pendingLiabilities.isNotEmpty()) {
                item {
                    Text(
                        "Pasivos pendientes",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(pendingLiabilities, key = { "liability_${it.id}" }) { liability ->
                    LiabilityRow(
                        liability = liability,
                        onPay = { liabilityToPay = liability },
                        onDelete = { liabilityToDelete = liability }
                    )
                    HorizontalDivider()
                }
            }

            // Historial
            item {
                Text(
                    "Historial",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (history.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin movimientos. Toca + para registrar.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(history, key = { item ->
                    when (item) {
                        is AssetHistoryItem.OperationItem -> "op_${item.operation.id}"
                        is AssetHistoryItem.DepositItem -> "dep_${item.transaction.id}"
                    }
                }) { item ->
                    when (item) {
                        is AssetHistoryItem.OperationItem -> OperationHistoryRow(
                            op = item.operation,
                            onDelete = { operationToDelete = item.operation }
                        )
                        is AssetHistoryItem.DepositItem -> DepositHistoryRow(
                            transaction = item.transaction
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    // Dialogs
    if (showEditInitialValueDialog) {
        EditInitialValueDialog(
            currentValue = account?.assetInitialValue ?: 0L,
            onConfirm = { amount ->
                viewModel.updateInitialValue(amount)
                showEditInitialValueDialog = false
            },
            onDismiss = { showEditInitialValueDialog = false }
        )
    }

    if (showInvestDialog) {
        InvestDialog(
            onConfirm = { totalSpent, assetValueIncrease, description ->
                viewModel.invest(totalSpent, assetValueIncrease, description)
                showInvestDialog = false
            },
            onDismiss = { showInvestDialog = false }
        )
    }

    if (showAssetIncomeDialog) {
        AssetIncomeDialog(
            onConfirm = { amount, assetValueDelta, description ->
                viewModel.recordAssetIncome(amount, assetValueDelta, description)
                showAssetIncomeDialog = false
            },
            onDismiss = { showAssetIncomeDialog = false }
        )
    }

    if (showCreateLiabilityDialog) {
        CreateLiabilityDialog(
            onConfirm = { description, amount ->
                viewModel.createLiability(description, amount)
                showCreateLiabilityDialog = false
            },
            onDismiss = { showCreateLiabilityDialog = false }
        )
    }

    if (showWithdrawDialog) {
        AssetWithdrawDialog(
            depositAccounts = depositAccounts,
            onConfirm = { depositAccountId, amount, description ->
                viewModel.withdraw(depositAccountId, amount, description)
                showWithdrawDialog = false
            },
            onDismiss = { showWithdrawDialog = false }
        )
    }

    liabilityToPay?.let { liability ->
        PayLiabilityDialog(
            liability = liability,
            onConfirm = {
                viewModel.payLiability(liability)
                liabilityToPay = null
            },
            onDismiss = { liabilityToPay = null }
        )
    }

    operationToDelete?.let { op ->
        DeleteOperationDialog(
            onConfirm = {
                viewModel.deleteOperation(op)
                operationToDelete = null
            },
            onDismiss = { operationToDelete = null }
        )
    }

    liabilityToDelete?.let { liability ->
        AlertDialog(
            onDismissRequest = { liabilityToDelete = null },
            title = { Text("Eliminar pasivo") },
            text = { Text("¿Eliminar \"${liability.description}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteLiability(liability); liabilityToDelete = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { liabilityToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    errorMessage?.let { msg ->
        LaunchedEffect(msg) { viewModel.clearError() }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
    }
}

// ─── Row composables ──────────────────────────────────────────────────────────

@Composable
private fun LiabilityRow(
    liability: AssetLiability,
    onPay: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    ListItem(
        headlineContent = { Text(liability.description) },
        supportingContent = {
            Text(
                "${liability.amount.toCopString()} · ${liability.createdDate.format(formatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        trailingContent = {
            Row {
                TextButton(onClick = onPay) { Text("Pagar") }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
private fun OperationHistoryRow(
    op: AssetOperation,
    onDelete: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val typeLabel = when (op.type) {
        AssetOperationType.INVEST             -> "Inversión"
        AssetOperationType.ASSET_INCOME       -> "Ingreso del activo"
        AssetOperationType.LIABILITY_PAYMENT  -> "Pago de pasivo"
        AssetOperationType.WITHDRAWAL         -> "Retiro"
    }
    val balanceColor = if (op.balanceEffect >= 0) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.error

    ListItem(
        headlineContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(typeLabel, style = MaterialTheme.typography.labelSmall) }
                )
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Balance: ${op.balanceEffect.toSignedCopString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = balanceColor
                )
                if (op.assetValueDelta != 0L) {
                    Text(
                        "Valor activo: ${op.assetValueDelta.toSignedCopString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (op.assetValueDelta >= 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                    )
                }
                op.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text(op.date.format(formatter), style = MaterialTheme.typography.bodySmall)
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    )
}

@Composable
private fun DepositHistoryRow(transaction: com.example.cuentaconmigo.domain.model.Transaction) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    ListItem(
        headlineContent = {
            Text(
                transaction.amount.toCopString(),
                color = MaterialTheme.colorScheme.primary
            )
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Ingreso de depósito", style = MaterialTheme.typography.bodySmall)
                transaction.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text(transaction.date.format(formatter), style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}

// ─── Dialogs ─────────────────────────────────────────────────────────────────

@Composable
private fun EditInitialValueDialog(
    currentValue: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val centavos = amountText.parseToCentavos()
    val isValid = centavos != null && centavos >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar valuación inicial") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Valor actual: ${currentValue.toCopString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = filterAmountInput(amountText, it) },
                    label = { Text("Nuevo valor inicial (COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(centavos!!) }, enabled = isValid) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun InvestDialog(
    onConfirm: (totalSpent: Long, assetValueIncrease: Long, description: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var totalSpentText by remember { mutableStateOf("") }
    var assetIncreaseText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val totalSpentCentavos = totalSpentText.parseToCentavos()
    val assetIncreaseCentavos = assetIncreaseText.parseToCentavos() ?: 0L
    val isValid = totalSpentCentavos != null && totalSpentCentavos > 0 &&
            assetIncreaseCentavos >= 0 && assetIncreaseCentavos <= (totalSpentCentavos ?: 0L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invertir en activo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = totalSpentText,
                    onValueChange = { totalSpentText = filterAmountInput(totalSpentText, it) },
                    label = { Text("Total gastado (COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = assetIncreaseText,
                    onValueChange = { assetIncreaseText = filterAmountInput(assetIncreaseText, it) },
                    label = { Text("Aumenta valor del activo (COP, ≤ total gastado)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = assetIncreaseCentavos > (totalSpentCentavos ?: 0L),
                    modifier = Modifier.fillMaxWidth()
                )
                if (assetIncreaseCentavos > (totalSpentCentavos ?: 0L)) {
                    Text(
                        "El aumento del activo no puede superar el total gastado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(totalSpentCentavos!!, assetIncreaseCentavos, description.ifBlank { null }) },
                enabled = isValid
            ) { Text("Invertir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AssetIncomeDialog(
    onConfirm: (amount: Long, assetValueDelta: Long, description: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var affectsValue by remember { mutableStateOf(false) }
    var valueDeltaText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val centavos = amountText.parseToCentavos()
    val valueDeltaCentavos = if (affectsValue) valueDeltaText.parseToCentavos() ?: 0L else 0L
    val isValid = centavos != null && centavos > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ingreso del activo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = filterAmountInput(amountText, it) },
                    label = { Text("Monto ingresado (COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¿Afecta valor del activo?", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = affectsValue, onCheckedChange = { affectsValue = it })
                }
                if (affectsValue) {
                    OutlinedTextField(
                        value = valueDeltaText,
                        onValueChange = { valueDeltaText = filterAmountInput(valueDeltaText, it) },
                        label = { Text("Cambio en valor del activo (COP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(centavos!!, valueDeltaCentavos, description.ifBlank { null }) },
                enabled = isValid
            ) { Text("Registrar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun CreateLiabilityDialog(
    onConfirm: (description: String, amount: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val centavos = amountText.parseToCentavos()
    val isValid = description.isNotBlank() && centavos != null && centavos > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear pasivo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = filterAmountInput(amountText, it) },
                    label = { Text("Monto (COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(description.trim(), centavos!!) },
                enabled = isValid
            ) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssetWithdrawDialog(
    depositAccounts: List<DepositAccount>,
    onConfirm: (depositAccountId: Long, amount: Long, description: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAccount by remember { mutableStateOf(depositAccounts.firstOrNull()) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val centavos = amountText.parseToCentavos()
    val isValid = centavos != null && centavos > 0 && selectedAccount != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Retirar a cuenta de depósito") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Selecciona una cuenta",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cuenta de destino") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        depositAccounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = { selectedAccount = account; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = filterAmountInput(amountText, it) },
                    label = { Text("Monto (COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedAccount!!.id, centavos!!, description.ifBlank { null }) },
                enabled = isValid
            ) { Text("Retirar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun PayLiabilityDialog(
    liability: AssetLiability,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pagar pasivo") },
        text = {
            Text("¿Pagar \"${liability.description}\" por ${liability.amount.toCopString()}? Se descontará del balance de la cuenta.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Pagar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun DeleteOperationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar operación") },
        text = { Text("¿Eliminar esta operación? Esta acción no se puede deshacer.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
