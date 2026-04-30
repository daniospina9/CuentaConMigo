package com.example.cuentaconmigo.features.accounts.deposit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.domain.model.DepositAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositAccountListScreen(
    onNavigateBack: () -> Unit,
    viewModel: DepositAccountViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<DepositAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<DepositAccount?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuentas de Depósito") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Edit, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar cuenta")
            }
        }
    ) { padding ->
        if (accounts.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("No hay cuentas de depósito. Crea una con el botón +") }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(accounts) { account ->
                    ListItem(
                        headlineContent = { Text(account.name) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { accountToEdit = account }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { accountToDelete = account }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showCreateDialog) {
            AccountNameDialog(
                title = "Nueva cuenta",
                onConfirm = { name -> viewModel.createAccount(name); showCreateDialog = false },
                onDismiss = { showCreateDialog = false }
            )
        }

        accountToEdit?.let { account ->
            AccountNameDialog(
                title = "Editar cuenta",
                initialName = account.name,
                onConfirm = { name -> viewModel.updateAccount(account, name); accountToEdit = null },
                onDismiss = { accountToEdit = null }
            )
        }

        accountToDelete?.let { account ->
            AlertDialog(
                onDismissRequest = { accountToDelete = null },
                title = { Text("Eliminar cuenta") },
                text = { Text("¿Eliminar \"${account.name}\"? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteAccount(account); accountToDelete = null }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { accountToDelete = null }) { Text("Cancelar") }
                }
            )
        }

        errorMessage?.let { msg ->
            LaunchedEffect(msg) { viewModel.clearError() }
            Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
        }
    }
}

@Composable
private fun AccountNameDialog(
    title: String,
    initialName: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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