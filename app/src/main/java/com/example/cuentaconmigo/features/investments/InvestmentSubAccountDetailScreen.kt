package com.example.cuentaconmigo.features.investments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.example.cuentaconmigo.domain.model.InvestmentFluctuation
import com.example.cuentaconmigo.domain.model.InvestmentSubtype
import com.example.cuentaconmigo.domain.model.Transaction
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentSubAccountDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: InvestmentSubAccountDetailViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val hasFab = account?.investmentSubtype != InvestmentSubtype.EXPENSE && account != null

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
            if (hasFab) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (account?.investmentSubtype) {
                InvestmentSubtype.LIQUID  -> SubAccountLiquidContent(viewModel)
                InvestmentSubtype.EXPENSE -> SubAccountExpenseContent(viewModel)
                else -> {}
            }
        }
    }

    if (showAddDialog) {
        when (account?.investmentSubtype) {
            InvestmentSubtype.LIQUID -> SubAccountFluctuationDialog(
                title = "Registrar movimiento",
                positiveLabel = "Ingreso",
                negativeLabel = "Egreso",
                onConfirm = { amount, desc -> viewModel.addFluctuation(amount, desc); showAddDialog = false },
                onDismiss = { showAddDialog = false }
            )
            else -> {}
        }
    }

    errorMessage?.let { msg ->
        LaunchedEffect(msg) { viewModel.clearError() }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
    }
}

@Composable
private fun SubAccountLiquidContent(viewModel: InvestmentSubAccountDetailViewModel) {
    val balance by viewModel.balance.collectAsState()
    val fluctuations by viewModel.fluctuations.collectAsState()
    val depositAccounts by viewModel.depositAccounts.collectAsState()
    var toDelete by remember { mutableStateOf<InvestmentFluctuation?>(null) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
        item { SubBalanceCard("Saldo disponible", balance) }
        item {
            OutlinedButton(
                onClick = { showWithdrawDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Retirar a cuenta de depósito")
            }
        }
        item { SubSectionHeader("Movimientos") }
        if (fluctuations.isEmpty()) {
            item { SubEmptyState("Sin movimientos. Toca + para añadir uno.") }
        } else {
            items(fluctuations, key = { it.id }) { fl ->
                SubFluctuationRow(fl, "Ingreso", "Egreso") { toDelete = fl }
                HorizontalDivider()
            }
        }
    }

    toDelete?.let { fl ->
        SubDeleteDialog(fl.amount, onConfirm = { viewModel.deleteFluctuation(fl); toDelete = null }, onDismiss = { toDelete = null })
    }

    if (showWithdrawDialog) {
        WithdrawToDepositDialog(
            depositAccounts = depositAccounts,
            onConfirm = { depositAccountId, amount, description ->
                viewModel.withdraw(depositAccountId, amount, description)
                showWithdrawDialog = false
            },
            onDismiss = { showWithdrawDialog = false }
        )
    }
}

@Composable
private fun SubAccountExpenseContent(viewModel: InvestmentSubAccountDetailViewModel) {
    val totalInvested by viewModel.totalInvested.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { SubBalanceCard("Total invertido", totalInvested) }
        item { SubSectionHeader("Gastos registrados") }
        if (transactions.isEmpty()) {
            item { SubEmptyState("Sin gastos. Regístralos desde el formulario de transacciones.") }
        } else {
            items(transactions, key = { it.id }) { tx ->
                SubExpenseRow(tx)
                HorizontalDivider()
            }
        }
    }
}

// ─── Shared private composables ───────────────────────────────────────────────

@Composable
private fun SubBalanceCard(label: String, value: Long) {
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(value.toCopString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SubSectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
}

@Composable
private fun SubEmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SubFluctuationRow(fluctuation: InvestmentFluctuation, positiveLabel: String, negativeLabel: String, onDelete: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val isPositive = fluctuation.amount >= 0
    ListItem(
        headlineContent = {
            Text(fluctuation.amount.toSignedCopString(), color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        },
        supportingContent = {
            Column {
                Text("${if (isPositive) positiveLabel else negativeLabel} · ${fluctuation.date.format(formatter)}", style = MaterialTheme.typography.bodySmall)
                fluctuation.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        },
        trailingContent = { IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") } }
    )
}

@Composable
private fun SubExpenseRow(tx: Transaction) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    ListItem(
        headlineContent = { Text(tx.amount.toCopString()) },
        supportingContent = {
            Column {
                Text(tx.date.format(formatter), style = MaterialTheme.typography.bodySmall)
                tx.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
    )
}

@Composable
private fun SubDeleteDialog(amount: Long, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar registro") },
        text = { Text("¿Eliminar este registro de ${amount.toSignedCopString()}?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WithdrawToDepositDialog(
    depositAccounts: List<DepositAccount>,
    onConfirm: (depositAccountId: Long, amount: Long, description: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAccount by remember { mutableStateOf(depositAccounts.firstOrNull()) }
    var amountTfv by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val centavos = amountTfv.text.parseToCentavos()
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
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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
                    value = amountTfv,
                    onValueChange = { amountTfv = filterAmountInput(amountTfv, it) },
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
private fun SubAccountFluctuationDialog(title: String, positiveLabel: String, negativeLabel: String, onConfirm: (Long, String?) -> Unit, onDismiss: () -> Unit) {
    var isPositive by remember { mutableStateOf(true) }
    var amountTfv by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }
    val centavos = amountTfv.text.parseToCentavos()
    val isValid = centavos != null && centavos > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = isPositive, onClick = { isPositive = true }, label = { Text(positiveLabel) })
                    FilterChip(selected = !isPositive, onClick = { isPositive = false }, label = { Text(negativeLabel) })
                }
                OutlinedTextField(
                    value = amountTfv,
                    onValueChange = { amountTfv = filterAmountInput(amountTfv, it) },
                    label = { Text("Monto (COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción (opcional)") },
                    maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { val raw = centavos!!; onConfirm(if (isPositive) raw else -raw, description.ifBlank { null }) },
                enabled = isValid
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}