package com.example.cuentaconmigo.features.debts

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.filterAmountInput
import com.example.cuentaconmigo.core.util.parseToCentavos
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.MinPaymentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (creditCardId: Long) -> Unit,
    viewModel: DebtListViewModel = hiltViewModel()
) {
    val cards by viewModel.cards.collectAsState()
    val debtsMap by viewModel.debtsMap.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<CreditCard?>(null) }
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
                title = { Text("Deudas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva tarjeta")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            if (cards.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sin tarjetas de crédito registradas.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(cards, key = { it.id }) { card ->
                    val debt = debtsMap[card.id] ?: 0L
                    val available = card.creditLimit - debt
                    CreditCardListItem(
                        card = card,
                        debt = debt,
                        available = available,
                        onClick = { onNavigateToDetail(card.id) },
                        onDelete = { cardToDelete = card }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCreditCardDialog(
            onConfirm = { name, lastFour, limit, rate, cutOff, dueDay, minType, minPercent, minFixed, monthlyFee ->
                viewModel.createCard(name, lastFour, limit, rate, cutOff, dueDay, minType, minPercent, minFixed, monthlyFee)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Eliminar tarjeta") },
            text = { Text("¿Eliminar \"${card.name}\"? Esta acción la marcará como inactiva.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCard(card)
                    cardToDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) { Text("Cancelar") }
            }
        )
    }

}

@Composable
private fun CreditCardListItem(
    card: CreditCard,
    debt: Long,
    available: Long,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = {
            val suffix = card.lastFourDigits?.let { " ···$it" } ?: ""
            Text("${card.name}$suffix", style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Column {
                Text(
                    "Deuda: ${debt.toCopString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (debt > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Disponible: ${available.toCopString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
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
private fun CreateCreditCardDialog(
    onConfirm: (
        name: String,
        lastFourDigits: String?,
        creditLimit: Long,
        interestRateAnnual: Double,
        cutOffDay: Int,
        paymentDueDay: Int,
        minPaymentType: MinPaymentType,
        minPaymentPercent: Double,
        minPaymentFixed: Long,
        monthlyFee: Long
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lastFour by remember { mutableStateOf("") }
    var limitTfv by remember { mutableStateOf(TextFieldValue("")) }
    var rateStr by remember { mutableStateOf("") }
    var cutOffStr by remember { mutableStateOf("") }
    var dueDayStr by remember { mutableStateOf("") }
    var minType by remember { mutableStateOf(MinPaymentType.PERCENTAGE) }
    var minValueStr by remember { mutableStateOf("") }
    var minValueTfv by remember { mutableStateOf(TextFieldValue("")) }
    var monthlyFeeTfv by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva tarjeta de crédito") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la tarjeta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastFour,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) lastFour = it },
                    label = { Text("Últimos 4 dígitos (opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = limitTfv,
                    onValueChange = { new -> limitTfv = filterAmountInput(limitTfv, new) },
                    label = { Text("Límite de crédito") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it },
                    label = { Text("Tasa efectiva anual - TEA (%)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cutOffStr,
                    onValueChange = { cutOffStr = it },
                    label = { Text("Día de corte (1-31)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDayStr,
                    onValueChange = { dueDayStr = it },
                    label = { Text("Día de vencimiento (1-31)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tipo de pago mínimo", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = minType == MinPaymentType.PERCENTAGE,
                        onClick = { minType = MinPaymentType.PERCENTAGE }
                    )
                    Text("Porcentaje")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = minType == MinPaymentType.FIXED,
                        onClick = { minType = MinPaymentType.FIXED }
                    )
                    Text("Monto fijo")
                }
                if (minType == MinPaymentType.PERCENTAGE) {
                    OutlinedTextField(
                        value = minValueStr,
                        onValueChange = { minValueStr = it },
                        label = { Text("Porcentaje mínimo (%)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = minValueTfv,
                        onValueChange = { new -> minValueTfv = filterAmountInput(minValueTfv, new) },
                        label = { Text("Monto fijo mínimo") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = monthlyFeeTfv,
                    onValueChange = { new -> monthlyFeeTfv = filterAmountInput(monthlyFeeTfv, new) },
                    label = { Text("Cuota de manejo mensual (opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val limit = limitTfv.text.parseToCentavos() ?: 0L
            val rate = rateStr.replace(",", ".").toDoubleOrNull() ?: 0.0
            val cutOff = cutOffStr.toIntOrNull()?.coerceIn(1, 31) ?: 0
            val dueDay = dueDayStr.toIntOrNull()?.coerceIn(1, 31) ?: 0
            val minPercent = if (minType == MinPaymentType.PERCENTAGE) minValueStr.replace(",", ".").toDoubleOrNull() ?: 0.0 else 0.0
            val minFixed = if (minType == MinPaymentType.FIXED) minValueTfv.text.parseToCentavos() ?: 0L else 0L
            val monthlyFee = monthlyFeeTfv.text.parseToCentavos() ?: 0L
            val isValid = name.isNotBlank() && limit > 0 && cutOff in 1..31 && dueDay in 1..31
            TextButton(
                onClick = {
                    onConfirm(
                        name.trim(),
                        lastFour.ifBlank { null },
                        limit,
                        rate,
                        cutOff,
                        dueDay,
                        minType,
                        minPercent,
                        minFixed,
                        monthlyFee
                    )
                },
                enabled = isValid
            ) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}