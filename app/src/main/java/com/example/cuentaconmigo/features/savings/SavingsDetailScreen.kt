package com.example.cuentaconmigo.features.savings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.DestinationAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubAccount: (subAccountId: Long) -> Unit,
    viewModel: SavingsDetailViewModel = hiltViewModel()
) {
    val parentAccount by viewModel.parentAccount.collectAsState()
    val subAccounts by viewModel.subAccounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<DestinationAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<DestinationAccount?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(parentAccount?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva subcuenta")
            }
        }
    ) { padding ->
        if (subAccounts.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin subcuentas.\nToca + para crear una subcuenta de ahorro.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(subAccounts, key = { it.account.id }) { summary ->
                    SavingsSubAccountCard(
                        summary = summary,
                        onClick = { onNavigateToSubAccount(summary.account.id) },
                        onEdit = { accountToEdit = summary.account },
                        onDelete = { accountToDelete = summary.account }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSavingsSubAccountDialog(
            onConfirm = { name ->
                viewModel.createSubAccount(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    accountToEdit?.let { account ->
        RenameSavingsSubAccountDialog(
            current = account,
            onConfirm = { newName ->
                viewModel.renameSubAccount(account, newName)
                accountToEdit = null
            },
            onDismiss = { accountToEdit = null }
        )
    }

    accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("Eliminar subcuenta") },
            text = { Text("¿Eliminar \"${account.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSubAccount(account); accountToDelete = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavingsSubAccountCard(
    summary: SavingsAccountSummary,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true }
            ),
            headlineContent = { Text(summary.account.name) },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        summary.balance.toCopString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Saldo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            DropdownMenuItem(
                text = { Text("Editar") },
                onClick = { menuExpanded = false; onEdit() }
            )
            DropdownMenuItem(
                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                onClick = { menuExpanded = false; onDelete() }
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun CreateSavingsSubAccountDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva subcuenta") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre (ej: Viaje a Europa)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Crear")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun RenameSavingsSubAccountDialog(
    current: DestinationAccount,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(current.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar subcuenta") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank() && name.trim() != current.name
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}