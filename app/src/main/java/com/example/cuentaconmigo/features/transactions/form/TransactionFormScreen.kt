package com.example.cuentaconmigo.features.transactions.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.filterAmountInput
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.InvestmentSubtype
import com.example.cuentaconmigo.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TransactionFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (viewModel.isEditMode) "Editar transacción" else "Nueva transacción") })
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tipo
            Text("Tipo de transacción", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.entries.forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { viewModel.setType(type) },
                        label = { Text(if (type == TransactionType.INCOME) "Ingreso" else "Gasto") }
                    )
                }
            }

            // Cuenta de depósito
            AccountDropdown(
                label = "Cuenta de depósito *",
                accounts = state.depositAccounts,
                selected = state.selectedDepositAccount,
                isError = state.depositError,
                onSelect = { viewModel.setDepositAccount(it) }
            )

            // Cuenta de destino (solo para gastos)
            if (state.type == TransactionType.EXPENSE) {
                DestinationDropdown(
                    accounts = state.destinationAccounts,
                    selected = state.selectedDestinationAccount,
                    isError = state.destinationError,
                    onSelect = { viewModel.setDestinationAccount(it) }
                )

                // Sub-cuenta (aparece cuando el destino es inversión o ahorro)
                if (state.destinationNeedsSubAccount) {
                    SubAccountDropdown(
                        subAccounts = state.subAccounts,
                        selected = state.selectedSubAccount,
                        isError = state.subAccountError,
                        isSavings = state.selectedDestinationAccount?.type == AccountType.SAVINGS,
                        onSelect = { viewModel.setSubAccount(it) }
                    )
                }
            }

            // Monto
            var amountTfv by remember { mutableStateOf(TextFieldValue(state.amountText)) }
            LaunchedEffect(state.amountText) {
                if (amountTfv.text != state.amountText)
                    amountTfv = TextFieldValue(state.amountText, TextRange(state.amountText.length))
            }
            OutlinedTextField(
                value = amountTfv,
                onValueChange = { new ->
                    amountTfv = filterAmountInput(amountTfv, new)
                    viewModel.setAmount(amountTfv.text)
                },
                label = { Text("Monto (COP) *") },
                isError = state.amountError,
                supportingText = { if (state.amountError) Text("Ingresa un monto válido") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Descripción
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.submit() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar transacción") }
        }

        state.errorMessage?.let { msg ->
            LaunchedEffect(msg) { viewModel.clearError() }
            Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdown(
    label: String,
    accounts: List<DepositAccount>,
    selected: DepositAccount?,
    isError: Boolean,
    onSelect: (DepositAccount) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            isError = isError,
            supportingText = { if (isError) Text("Selecciona una cuenta") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = { onSelect(account); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationDropdown(
    accounts: List<DestinationAccount>,
    selected: DestinationAccount?,
    isError: Boolean,
    onSelect: (DestinationAccount) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cuenta de destino *") },
            isError = isError,
            supportingText = { if (isError) Text("Selecciona una cuenta de destino") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = { onSelect(account); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubAccountDropdown(
    subAccounts: List<DestinationAccount>,
    selected: DestinationAccount?,
    isError: Boolean,
    isSavings: Boolean,
    onSelect: (DestinationAccount) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = if (isSavings) "Subcuenta de ahorro *" else "Subcuenta de inversión *"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let {
                if (isSavings) it.name else "${it.name} (${it.investmentSubtype.label()})"
            } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            isError = isError,
            supportingText = {
                if (isError) Text("Selecciona una subcuenta")
                else if (subAccounts.isEmpty()) Text("Sin subcuentas creadas en esta cuenta")
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (subAccounts.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin subcuentas", style = MaterialTheme.typography.bodySmall) },
                    onClick = {},
                    enabled = false
                )
            } else {
                subAccounts.forEach { sub ->
                    DropdownMenuItem(
                        text = {
                            if (isSavings) {
                                Text(sub.name)
                            } else {
                                Column {
                                    Text(sub.name)
                                    Text(
                                        sub.investmentSubtype.label(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = { onSelect(sub); expanded = false }
                    )
                }
            }
        }
    }
}

private fun InvestmentSubtype?.label(): String = when (this) {
    InvestmentSubtype.ASSET   -> "Activo"
    InvestmentSubtype.LIQUID  -> "Liquidez"
    InvestmentSubtype.EXPENSE -> "Gasto"
    null                      -> ""
}
