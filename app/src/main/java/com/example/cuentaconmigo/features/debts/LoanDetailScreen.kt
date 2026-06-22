package com.example.cuentaconmigo.features.debts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.filterAmountInput
import com.example.cuentaconmigo.core.util.parseToCentavos
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.model.SimpleDebtTransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: LoanDetailViewModel = hiltViewModel()
) {
    val loan by viewModel.loan.collectAsState()
    val currentBalance by viewModel.currentBalance.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val depositAccounts by viewModel.depositAccounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showLoanDialog by remember { mutableStateOf(false) }
    var showCollectionDialog by remember { mutableStateOf(false) }
    var showInterestDialog by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<SimpleDebtTransaction?>(null) }
    var txToEdit by remember { mutableStateOf<SimpleDebtTransaction?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(loan?.name ?: "Préstamo otorgado") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Resumen del préstamo
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        loan?.description?.let { desc ->
                            if (desc.isNotBlank()) {
                                Text(desc, style = MaterialTheme.typography.bodyMedium)
                                HorizontalDivider()
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Te deben", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                currentBalance.toCopString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentBalance > 0) Color(0xFF2E7D32)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Botones de acción
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showLoanDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Registrar préstamo")
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showInterestDialog = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Añadir intereses") }
                        OutlinedButton(
                            onClick = { showCollectionDialog = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Registrar cobro") }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Historial de movimientos
            item {
                Text(
                    "Movimientos",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin movimientos registrados.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    LoanTxRow(
                        tx = tx,
                        onEdit = { txToEdit = tx },
                        onDelete = { txToDelete = tx }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    // Dialogs
    if (showLoanDialog) {
        LoanAmountDialog(
            title = "Registrar préstamo",
            confirmLabel = "Registrar",
            showDepositAccount = true,
            depositAccounts = depositAccounts,
            onConfirm = { amount, depositAccountId, description, date ->
                viewModel.registerLoan(amount, depositAccountId!!, description, date)
                showLoanDialog = false
            },
            onDismiss = { showLoanDialog = false }
        )
    }

    if (showCollectionDialog) {
        LoanAmountDialog(
            title = "Registrar cobro",
            confirmLabel = "Cobrar",
            showDepositAccount = true,
            depositAccounts = depositAccounts,
            onConfirm = { amount, depositAccountId, description, date ->
                viewModel.registerCollectionTx(amount, depositAccountId!!, description, date)
                showCollectionDialog = false
            },
            onDismiss = { showCollectionDialog = false }
        )
    }

    if (showInterestDialog) {
        LoanAmountDialog(
            title = "Añadir intereses",
            confirmLabel = "Registrar",
            showDepositAccount = false,
            depositAccounts = emptyList(),
            onConfirm = { amount, _, description, date ->
                viewModel.registerInterestTx(amount, description, date)
                showInterestDialog = false
            },
            onDismiss = { showInterestDialog = false }
        )
    }

    txToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Eliminar este movimiento de ${tx.amount.toCopString()}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTx(tx)
                    txToDelete = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    txToEdit?.let { tx ->
        EditLoanTxDialog(
            tx = tx,
            onConfirm = { updated ->
                viewModel.updateTx(updated)
                txToEdit = null
            },
            onDismiss = { txToEdit = null }
        )
    }
}

@Composable
private fun LoanTxRow(
    tx: SimpleDebtTransaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val label = when (tx.type) {
        SimpleDebtTransactionType.LOAN_GIVEN -> "Préstamo"
        SimpleDebtTransactionType.COLLECTION -> "Cobro"
        SimpleDebtTransactionType.INTEREST -> "Interés"
        else -> tx.type.name
    }
    val color = when (tx.type) {
        SimpleDebtTransactionType.COLLECTION -> Color(0xFF2E7D32)
        SimpleDebtTransactionType.LOAN_GIVEN -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }

    ListItem(
        headlineContent = {
            Text(tx.amount.toCopString(), color = color)
        },
        supportingContent = {
            val desc = tx.description?.let { " · $it" } ?: ""
            Text(
                "$label · ${dateFormatter.format(Date(tx.date))}$desc",
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanAmountDialog(
    title: String,
    confirmLabel: String,
    showDepositAccount: Boolean,
    depositAccounts: List<DepositAccount>,
    onConfirm: (amount: Long, depositAccountId: Long?, description: String?, date: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var amountTfv by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<DepositAccount?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val amount = amountTfv.text.parseToCentavos() ?: 0L
    val isValid = amount > 0 && (!showDepositAccount || selectedAccount != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
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
                if (showDepositAccount) {
                    ExposedDropdownMenuBox(
                        expanded = menuExpanded,
                        onExpandedChange = { menuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedAccount?.name ?: "Seleccionar cuenta",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cuenta de depósito") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            depositAccounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text(acc.name) },
                                    onClick = {
                                        selectedAccount = acc
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fecha: ${dateFormatter.format(Date(selectedDateMillis))}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(amount, selectedAccount?.id, description.ifBlank { null }, selectedDateMillis)
                },
                enabled = isValid
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLoanTxDialog(
    tx: SimpleDebtTransaction,
    onConfirm: (SimpleDebtTransaction) -> Unit,
    onDismiss: () -> Unit
) {
    val initialAmount = remember(tx.id) {
        val raw = tx.amount
        val pesos = raw / 100
        val centavos = raw % 100
        if (centavos == 0L) pesos.toString()
        else "$pesos,${centavos.toString().padStart(2, '0')}"
    }
    var amountTfv by remember(tx.id) { mutableStateOf(TextFieldValue(initialAmount)) }
    var description by remember(tx.id) { mutableStateOf(tx.description ?: "") }

    val label = when (tx.type) {
        SimpleDebtTransactionType.LOAN_GIVEN -> "Editar préstamo"
        SimpleDebtTransactionType.COLLECTION -> "Editar cobro"
        SimpleDebtTransactionType.INTEREST -> "Editar interés"
        else -> "Editar movimiento"
    }

    val amount = amountTfv.text.parseToCentavos() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            TextButton(
                onClick = {
                    onConfirm(tx.copy(amount = amount, description = description.ifBlank { null }))
                },
                enabled = amount > 0
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
