package com.example.cuentaconmigo.features.transactions.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.domain.model.DepositAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transferencia entre cuentas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
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

            DepositAccountDropdown(
                label = "Cuenta origen *",
                accounts = state.depositAccounts,
                selected = state.fromAccount,
                isError = state.fromError,
                errorText = "Selecciona la cuenta origen",
                onSelect = { viewModel.setFromAccount(it) }
            )

            DepositAccountDropdown(
                label = "Cuenta destino *",
                accounts = state.depositAccounts,
                selected = state.toAccount,
                isError = state.toError || state.sameAccountError,
                errorText = if (state.sameAccountError) "Origen y destino deben ser diferentes"
                            else "Selecciona la cuenta destino",
                onSelect = { viewModel.setToAccount(it) }
            )

            OutlinedTextField(
                value = state.amountText,
                onValueChange = { viewModel.setAmount(it) },
                label = { Text("Monto (COP) *") },
                isError = state.amountError,
                supportingText = { if (state.amountError) Text("Ingresa un monto válido") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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
            ) {
                Text("Transferir")
            }
        }

        state.errorMessage?.let { msg ->
            LaunchedEffect(msg) { viewModel.clearError() }
            Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepositAccountDropdown(
    label: String,
    accounts: List<DepositAccount>,
    selected: DepositAccount?,
    isError: Boolean,
    errorText: String,
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
            supportingText = { if (isError) Text(errorText) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
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
