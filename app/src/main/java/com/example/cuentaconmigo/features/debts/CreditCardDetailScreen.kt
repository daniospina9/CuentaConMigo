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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.filterAmountInput
import com.example.cuentaconmigo.core.util.parseToCentavos
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.ExtractReconciliation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreditCardDetailViewModel = hiltViewModel()
) {
    val card by viewModel.card.collectAsState()
    val currentDebt by viewModel.currentDebt.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val depositAccounts by viewModel.depositAccounts.collectAsState()
    val destinationAccounts by viewModel.destinationAccounts.collectAsState()
    val purchaseSubAccounts by viewModel.purchaseSubAccounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val reconciliation by viewModel.reconciliation.collectAsState()

    var showPurchaseDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showChargeDialog by remember { mutableStateOf(false) }
    var showExtractDialog by remember { mutableStateOf(false) }
    var showReconciliationDialog by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<CreditCardTransaction?>(null) }
    var txToEdit by remember { mutableStateOf<CreditCardTransaction?>(null) }
    val tem = viewModel.tem
    val snackbarHostState = remember { SnackbarHostState() }

    val available = (card?.creditLimit ?: 0L) - currentDebt
    val minPayment = viewModel.minPaymentAmount

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.name ?: "Tarjeta") },
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
            // Card info
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val suffix = card?.lastFourDigits?.let { " ···$it" } ?: ""
                        Text(
                            "${card?.name ?: ""}$suffix",
                            style = MaterialTheme.typography.titleMedium
                        )
                        card?.interestRateAnnual?.let { tea ->
                            Text("TEA: $tea%", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "TEM: ${"%.4f".format(tem * 100)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        card?.let { c ->
                            if (c.monthlyFee > 0) {
                                Text(
                                    "Cuota de manejo: ${c.monthlyFee.toCopString()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        card?.cutOffDay?.let {
                            Text("Día de corte: $it", style = MaterialTheme.typography.bodySmall)
                        }
                        card?.paymentDueDay?.let {
                            Text("Día de vencimiento: $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Debt summary card
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Deuda actual", style = MaterialTheme.typography.labelMedium)
                            Text(
                                currentDebt.toCopString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (currentDebt > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Disponible", style = MaterialTheme.typography.labelMedium)
                            Text(
                                available.toCopString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Límite", style = MaterialTheme.typography.labelMedium)
                            Text(
                                (card?.creditLimit ?: 0L).toCopString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Reconciliation banner
            reconciliation?.let { rec ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (rec.hasDifference)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (rec.hasDifference)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Column {
                                    if (rec.hasDifference) {
                                        Text(
                                            "Diferencia con extracto",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            rec.diff.toCopString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    } else {
                                        Text(
                                            "Extracto registrado",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            "Saldos coinciden",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                            TextButton(onClick = { showReconciliationDialog = true }) {
                                Text("Ver detalle")
                            }
                        }
                    }
                }
            }

            // Action buttons
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPurchaseDialog = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Registrar compra") }
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Realizar pago") }
                    }
                    OutlinedButton(
                        onClick = { showChargeDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Registrar cargo") }
                    OutlinedButton(
                        onClick = { showExtractDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Registrar extracto") }
                }
            }

            // Movements header
            item {
                Text(
                    "Movimientos",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        "Sin movimientos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    CreditCardTxRow(
                        tx = tx,
                        onEdit = { txToEdit = tx },
                        onDelete = { txToDelete = tx }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showPurchaseDialog) {
        RegisterPurchaseDialog(
            destinationAccounts = destinationAccounts,
            subAccounts = purchaseSubAccounts,
            tem = tem,
            onParentSelected = { viewModel.setPurchaseParent(it) },
            onConfirm = { amount, description, destAccountId, date, installments ->
                viewModel.registerPurchase(amount, description, destAccountId, date, installments)
                showPurchaseDialog = false
            },
            onDismiss = {
                viewModel.setPurchaseParent(null)
                showPurchaseDialog = false
            }
        )
    }

    if (showChargeDialog) {
        RegisterChargeDialog(
            onConfirm = { amount, description, type, date ->
                viewModel.registerCharge(amount, description, type, date)
                showChargeDialog = false
            },
            onDismiss = { showChargeDialog = false }
        )
    }

    if (showPaymentDialog) {
        RegisterPaymentDialog(
            totalDebt = currentDebt,
            minPayment = minPayment,
            depositAccounts = depositAccounts,
            onConfirm = { amount, depositAccountId, date ->
                viewModel.registerPayment(amount, depositAccountId, date)
                showPaymentDialog = false
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    txToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Eliminar este registro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTransaction(tx); txToDelete = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { txToDelete = null }) { Text("Cancelar") } }
        )
    }

    txToEdit?.let { tx ->
        EditCreditCardTxDialog(
            tx = tx,
            destinationAccounts = destinationAccounts,
            depositAccounts = depositAccounts,
            tem = tem,
            onConfirm = { updated -> viewModel.updateTransaction(updated); txToEdit = null },
            onDismiss = { txToEdit = null }
        )
    }

    if (showExtractDialog) {
        RegisterExtractDialog(
            onConfirm = { billingAmount, currentInterest, lateInterest, otherCharges, paymentsAndCredits, totalBankBalance, minimumPayment, uncollectedInterest ->
                viewModel.registerExtract(
                    billingAmount = billingAmount,
                    currentInterest = currentInterest,
                    lateInterest = lateInterest,
                    otherCharges = otherCharges,
                    paymentsAndCredits = paymentsAndCredits,
                    totalBankBalance = totalBankBalance,
                    minimumPayment = minimumPayment,
                    uncollectedInterest = uncollectedInterest
                )
                showExtractDialog = false
            },
            onDismiss = { showExtractDialog = false }
        )
    }

    if (showReconciliationDialog) {
        reconciliation?.let { rec ->
            ReconciliationDetailDialog(
                reconciliation = rec,
                onAdjust = { type ->
                    viewModel.reconcileExtract(rec.extract, kotlin.math.abs(rec.diff), type)
                    showReconciliationDialog = false
                },
                onIgnore = {
                    viewModel.ignoreReconciliation(rec.extract)
                    showReconciliationDialog = false
                },
                onDismiss = { showReconciliationDialog = false }
            )
        }
    }

}

@Composable
private fun CreditCardTxRow(
    tx: CreditCardTransaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val label = when (tx.type) {
        CreditCardTransactionType.PURCHASE -> if (tx.installments > 1) "Compra (${tx.installments} cuotas)" else "Compra"
        CreditCardTransactionType.PAYMENT -> "Pago"
        CreditCardTransactionType.INTEREST -> "Interés"
        CreditCardTransactionType.FEE -> "Cargo"
    }
    val isCredit = tx.type == CreditCardTransactionType.PAYMENT

    ListItem(
        headlineContent = {
            Text(
                tx.amount.toCopString(),
                color = if (isCredit) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
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
private fun RegisterPurchaseDialog(
    destinationAccounts: List<DestinationAccount>,
    subAccounts: List<DestinationAccount>,
    tem: Double,
    onParentSelected: (DestinationAccount?) -> Unit,
    onConfirm: (amount: Long, description: String?, destinationAccountId: Long?, date: Long, installments: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var amountTfv by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<DestinationAccount?>(null) }
    var selectedSubAccount by remember { mutableStateOf<DestinationAccount?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var subMenuExpanded by remember { mutableStateOf(false) }
    var cuotasStr by remember { mutableStateOf("1") }

    // Reset sub-account selection when sub-account list changes (new parent picked)
    LaunchedEffect(subAccounts) { selectedSubAccount = null }

    val isInvestment = selectedAccount?.type == AccountType.INVESTMENT
    val effectiveDestId = if (isInvestment) selectedSubAccount?.id else selectedAccount?.id

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar compra") },
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
                OutlinedTextField(
                    value = cuotasStr,
                    onValueChange = { cuotasStr = it },
                    label = { Text("Número de cuotas (1 = de contado)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val cuotas = cuotasStr.toIntOrNull() ?: 1
                val amountCentavos = amountTfv.text.parseToCentavos() ?: 0L
                if (cuotas > 1 && amountCentavos > 0 && tem > 0) {
                    val cuotaMensual = amountCentavos * (tem * Math.pow(1 + tem, cuotas.toDouble())) / (Math.pow(1 + tem, cuotas.toDouble()) - 1)
                    Text(
                        "Cuota mensual aprox: ${cuotaMensual.toLong().toCopString()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (destinationAccounts.isEmpty()) {
                    Text(
                        "Crea una cuenta destino primero",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Parent / category selector
                    ExposedDropdownMenuBox(
                        expanded = menuExpanded,
                        onExpandedChange = { menuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedAccount?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría (obligatorio)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            destinationAccounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text(acc.name) },
                                    onClick = {
                                        selectedAccount = acc
                                        menuExpanded = false
                                        onParentSelected(acc)
                                    }
                                )
                            }
                        }
                    }

                    // Sub-account selector (only for investment accounts)
                    if (isInvestment) {
                        if (subAccounts.isEmpty()) {
                            Text(
                                "Esta cuenta de inversión no tiene subcuentas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = subMenuExpanded,
                                onExpandedChange = { subMenuExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedSubAccount?.name ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Subcuenta de inversión (obligatorio)") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subMenuExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = subMenuExpanded,
                                    onDismissRequest = { subMenuExpanded = false }
                                ) {
                                    subAccounts.forEach { sub ->
                                        DropdownMenuItem(
                                            text = { Text(sub.name) },
                                            onClick = { selectedSubAccount = sub; subMenuExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val amount = amountTfv.text.parseToCentavos() ?: 0L
            val cuotas = cuotasStr.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val canConfirm = amount > 0 && selectedAccount != null &&
                (!isInvestment || selectedSubAccount != null)
            TextButton(
                onClick = {
                    onConfirm(
                        amount,
                        description.ifBlank { null },
                        effectiveDestId,
                        System.currentTimeMillis(),
                        cuotas
                    )
                },
                enabled = canConfirm
            ) { Text("Registrar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private enum class PaymentOption { TOTAL, MINIMUM, CUSTOM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterPaymentDialog(
    totalDebt: Long,
    minPayment: Long,
    depositAccounts: List<DepositAccount>,
    onConfirm: (amount: Long, depositAccountId: Long, date: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(PaymentOption.TOTAL) }
    var customAmountTfv by remember { mutableStateOf(TextFieldValue("")) }
    var selectedAccount by remember(depositAccounts) { mutableStateOf(depositAccounts.firstOrNull()) }
    var menuExpanded by remember { mutableStateOf(false) }

    val amount = when (selectedOption) {
        PaymentOption.TOTAL -> totalDebt
        PaymentOption.MINIMUM -> minPayment
        PaymentOption.CUSTOM -> customAmountTfv.text.parseToCentavos() ?: 0L
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Realizar pago") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Quick options
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == PaymentOption.TOTAL,
                        onClick = { selectedOption = PaymentOption.TOTAL }
                    )
                    Text("Total (${totalDebt.toCopString()})")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == PaymentOption.MINIMUM,
                        onClick = { selectedOption = PaymentOption.MINIMUM }
                    )
                    Text("Mínimo (${minPayment.toCopString()})")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == PaymentOption.CUSTOM,
                        onClick = { selectedOption = PaymentOption.CUSTOM }
                    )
                    Text("Otro monto")
                }
                if (selectedOption == PaymentOption.CUSTOM) {
                    OutlinedTextField(
                        value = customAmountTfv,
                        onValueChange = { new -> customAmountTfv = filterAmountInput(customAmountTfv, new) },
                        label = { Text("Monto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Deposit account selector
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        depositAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = { selectedAccount = acc; menuExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedAccount?.id?.let { accId ->
                        onConfirm(amount, accId, System.currentTimeMillis())
                    }
                },
                enabled = amount > 0 && selectedAccount != null
            ) { Text("Pagar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterChargeDialog(
    onConfirm: (amount: Long, description: String, type: CreditCardTransactionType, date: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var amountTfv by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }

    val chargeOptions = listOf(
        "Cuota de manejo" to CreditCardTransactionType.FEE,
        "Seguro de vida / desempleo" to CreditCardTransactionType.FEE,
        "Interés" to CreditCardTransactionType.INTEREST,
        "Otro cargo" to CreditCardTransactionType.FEE
    )
    var selectedChargeLabel by remember { mutableStateOf(chargeOptions.first().first) }
    var selectedChargeType by remember { mutableStateOf(chargeOptions.first().second) }
    var menuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar cargo") },
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
                    label = { Text("Descripción") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedChargeLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de cargo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        chargeOptions.forEach { (label, type) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedChargeLabel = label
                                    selectedChargeType = type
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val amount = amountTfv.text.parseToCentavos() ?: 0L
            TextButton(
                onClick = {
                    onConfirm(
                        amount,
                        description,
                        selectedChargeType,
                        System.currentTimeMillis()
                    )
                },
                enabled = amount > 0 && description.isNotBlank()
            ) { Text("Registrar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCreditCardTxDialog(
    tx: CreditCardTransaction,
    destinationAccounts: List<DestinationAccount>,
    depositAccounts: List<DepositAccount>,
    tem: Double,
    onConfirm: (CreditCardTransaction) -> Unit,
    onDismiss: () -> Unit
) {
    val initialAmountText = remember(tx.id) {
        filterAmountInput("", (tx.amount / 100).toString())
    }
    var amountTfv by remember(tx.id) {
        mutableStateOf(TextFieldValue(initialAmountText, TextRange(initialAmountText.length)))
    }
    var description by remember(tx.id) { mutableStateOf(tx.description ?: "") }
    var cuotasStr by remember(tx.id) { mutableStateOf(tx.installments.toString()) }
    var selectedDestAccount by remember(tx.id, destinationAccounts) {
        mutableStateOf(destinationAccounts.firstOrNull { it.id == tx.destinationAccountId })
    }
    var destMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar movimiento") },
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

                when (tx.type) {
                    CreditCardTransactionType.PURCHASE -> {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción (opcional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cuotasStr,
                            onValueChange = { cuotasStr = it },
                            label = { Text("Número de cuotas (1 = de contado)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val cuotas = cuotasStr.toIntOrNull() ?: 1
                        val amountCentavos = amountTfv.text.parseToCentavos() ?: 0L
                        if (cuotas > 1 && amountCentavos > 0 && tem > 0) {
                            val cuotaMensual = amountCentavos * (tem * Math.pow(1 + tem, cuotas.toDouble())) / (Math.pow(1 + tem, cuotas.toDouble()) - 1)
                            Text(
                                "Cuota mensual aprox: ${cuotaMensual.toLong().toCopString()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (destinationAccounts.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = destMenuExpanded,
                                onExpandedChange = { destMenuExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedDestAccount?.name ?: "Sin categoría",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Cuenta destino (opcional)") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destMenuExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = destMenuExpanded,
                                    onDismissRequest = { destMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Ninguna") },
                                        onClick = { selectedDestAccount = null; destMenuExpanded = false }
                                    )
                                    destinationAccounts.forEach { acc ->
                                        DropdownMenuItem(
                                            text = { Text(acc.name) },
                                            onClick = { selectedDestAccount = acc; destMenuExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    CreditCardTransactionType.PAYMENT -> {
                        Text(
                            "El pago vinculado en la cuenta de depósito también se actualizará.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    CreditCardTransactionType.INTEREST,
                    CreditCardTransactionType.FEE -> {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            val newAmount = amountTfv.text.parseToCentavos() ?: 0L
            TextButton(
                onClick = {
                    val newCuotas = cuotasStr.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onConfirm(
                        tx.copy(
                            amount = newAmount,
                            description = description.ifBlank { null },
                            installments = newCuotas,
                            destinationAccountId = selectedDestAccount?.id
                        )
                    )
                },
                enabled = newAmount > 0
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun RegisterExtractDialog(
    onConfirm: (billingAmount: Long, currentInterest: Long, lateInterest: Long, otherCharges: Long, paymentsAndCredits: Long, totalBankBalance: Long, minimumPayment: Long, uncollectedInterest: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var billingAmountTfv by remember { mutableStateOf(TextFieldValue("")) }
    var currentInterestTfv by remember { mutableStateOf(TextFieldValue("")) }
    var lateInterestTfv by remember { mutableStateOf(TextFieldValue("")) }
    var otherChargesTfv by remember { mutableStateOf(TextFieldValue("")) }
    var paymentsAndCreditsTfv by remember { mutableStateOf(TextFieldValue("")) }
    var totalBankBalanceTfv by remember { mutableStateOf(TextFieldValue("")) }
    var minimumPaymentTfv by remember { mutableStateOf(TextFieldValue("")) }
    var uncollectedInterestTfv by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar extracto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Ingresa los valores del extracto bancario de este mes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = billingAmountTfv,
                    onValueChange = { billingAmountTfv = filterAmountInput(billingAmountTfv, it) },
                    label = { Text("Facturación del mes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currentInterestTfv,
                    onValueChange = { currentInterestTfv = filterAmountInput(currentInterestTfv, it) },
                    label = { Text("Intereses corrientes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lateInterestTfv,
                    onValueChange = { lateInterestTfv = filterAmountInput(lateInterestTfv, it) },
                    label = { Text("Intereses de mora") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = otherChargesTfv,
                    onValueChange = { otherChargesTfv = filterAmountInput(otherChargesTfv, it) },
                    label = { Text("Otros cargos (cuota de manejo, etc.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = paymentsAndCreditsTfv,
                    onValueChange = { paymentsAndCreditsTfv = filterAmountInput(paymentsAndCreditsTfv, it) },
                    label = { Text("Pagos y abonos según banco") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalBankBalanceTfv,
                    onValueChange = { totalBankBalanceTfv = filterAmountInput(totalBankBalanceTfv, it) },
                    label = { Text("Saldo total según banco") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = minimumPaymentTfv,
                    onValueChange = { minimumPaymentTfv = filterAmountInput(minimumPaymentTfv, it) },
                    label = { Text("Pago mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uncollectedInterestTfv,
                    onValueChange = { uncollectedInterestTfv = filterAmountInput(uncollectedInterestTfv, it) },
                    label = { Text("Intereses no cobrados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val totalBank = totalBankBalanceTfv.text.parseToCentavos() ?: 0L
            TextButton(
                onClick = {
                    onConfirm(
                        billingAmountTfv.text.parseToCentavos() ?: 0L,
                        currentInterestTfv.text.parseToCentavos() ?: 0L,
                        lateInterestTfv.text.parseToCentavos() ?: 0L,
                        otherChargesTfv.text.parseToCentavos() ?: 0L,
                        paymentsAndCreditsTfv.text.parseToCentavos() ?: 0L,
                        totalBank,
                        minimumPaymentTfv.text.parseToCentavos() ?: 0L,
                        uncollectedInterestTfv.text.parseToCentavos() ?: 0L
                    )
                },
                enabled = totalBank > 0
            ) { Text("Registrar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReconciliationDetailDialog(
    reconciliation: ExtractReconciliation,
    onAdjust: (type: CreditCardTransactionType) -> Unit,
    onIgnore: () -> Unit,
    onDismiss: () -> Unit
) {
    val extract = reconciliation.extract
    val adjustTypeOptions = listOf(
        "Interés" to CreditCardTransactionType.INTEREST,
        "Cargo / Comisión" to CreditCardTransactionType.FEE
    )
    var selectedTypeLabel by remember { mutableStateOf(adjustTypeOptions.first().first) }
    var selectedType by remember { mutableStateOf(adjustTypeOptions.first().second) }
    var menuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle del extracto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                @Composable
                fun ExtractRow(label: String, value: Long) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(value.toCopString(), style = MaterialTheme.typography.bodySmall)
                    }
                }

                ExtractRow("Facturación del mes", extract.billingAmount)
                ExtractRow("Intereses corrientes", extract.currentInterest)
                ExtractRow("Intereses de mora", extract.lateInterest)
                ExtractRow("Otros cargos", extract.otherCharges)
                ExtractRow("Pagos y abonos (banco)", extract.paymentsAndCredits)
                ExtractRow("Pago mínimo", extract.minimumPayment)
                ExtractRow("Intereses no cobrados", extract.uncollectedInterest)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Saldo banco", style = MaterialTheme.typography.labelMedium)
                    Text(extract.totalBankBalance.toCopString(), style = MaterialTheme.typography.bodyMedium)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Saldo app", style = MaterialTheme.typography.labelMedium)
                    Text(reconciliation.appDebt.toCopString(), style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Diferencia", style = MaterialTheme.typography.labelMedium)
                    Text(
                        reconciliation.diff.toCopString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (reconciliation.hasDifference) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                    )
                }
                if (reconciliation.hasDifference) {
                    Text(
                        "Selecciona el tipo de ajuste que se registrará por la diferencia:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = menuExpanded,
                        onExpandedChange = { menuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTypeLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de ajuste") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            adjustTypeOptions.forEach { (label, type) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedTypeLabel = label
                                        selectedType = type
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (reconciliation.hasDifference) {
                TextButton(onClick = { onAdjust(selectedType) }) {
                    Text("Ajustar y conciliar")
                }
            } else {
                TextButton(onClick = onIgnore) {
                    Text("Confirmar conciliación")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (reconciliation.hasDifference) {
                    TextButton(onClick = onIgnore) { Text("Ignorar diferencia") }
                }
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        }
    )
}