package com.example.cuentaconmigo.features.accounts.destination

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationAccountListScreen(
    onNavigateBack: () -> Unit,
    viewModel: DestinationAccountViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<DestinationAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<DestinationAccount?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cuentas de Destino") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        if (accounts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Sin cuentas de destino")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(accounts) { account ->
                    ListItem(
                        headlineContent = { Text(account.name) },
                        supportingContent = { Text(account.type.label()) },
                        leadingContent = {
                            if (account.isDefault)
                                Icon(Icons.Default.Lock, contentDescription = "Por defecto")
                        },
                        trailingContent = {
                            if (!account.isDefault) {
                                Row {
                                    IconButton(onClick = { accountToEdit = account }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { accountToDelete = account }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showCreateDialog) {
            CreateDestinationDialog(
                onConfirm = { name, type ->
                    viewModel.createAccount(name, type)
                    showCreateDialog = false
                },
                onDismiss = { showCreateDialog = false }
            )
        }

        accountToEdit?.let { account ->
            EditNameDialog(
                initialName = account.name,
                onConfirm = { name -> viewModel.updateAccount(account, name); accountToEdit = null },
                onDismiss = { accountToEdit = null }
            )
        }

        accountToDelete?.let { account ->
            AlertDialog(
                onDismissRequest = { accountToDelete = null },
                title = { Text("Eliminar cuenta") },
                text = { Text("¿Eliminar \"${account.name}\"?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteAccount(account); accountToDelete = null }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = { TextButton(onClick = { accountToDelete = null }) { Text("Cancelar") } }
            )
        }

        errorMessage?.let { msg ->
            LaunchedEffect(msg) { delay(3_000); viewModel.clearError() }
            Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
        }
    }
}

@Composable
private fun CreateDestinationDialog(onConfirm: (String, AccountType) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.EXPENSE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva cuenta de destino") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Text("Tipo:", style = MaterialTheme.typography.labelMedium)
                AccountType.entries.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Text(type.label())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), selectedType) },
                enabled = name.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun EditNameDialog(initialName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar nombre") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun AccountType.label(): String = when (this) {
    AccountType.EXPENSE    -> "Gastos"
    AccountType.SAVINGS    -> "Ahorros"
    AccountType.INVESTMENT -> "Inversiones"
}