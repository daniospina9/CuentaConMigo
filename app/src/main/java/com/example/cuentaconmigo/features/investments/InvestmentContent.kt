package com.example.cuentaconmigo.features.investments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.domain.model.DestinationAccount

@Composable
fun InvestmentContent(
    onNavigateToDetail: (accountId: Long) -> Unit,
    viewModel: InvestmentViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva cuenta de inversión")
            }
        }
    ) { padding ->
        if (accounts.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin cuentas de inversión.\nToca + para crear una.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(accounts, key = { it.id }) { account ->
                    InvestmentAccountRow(account = account, onClick = { onNavigateToDetail(account.id) })
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateInvestmentAccountDialog(
            onConfirm = { name ->
                viewModel.createAccount(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    errorMessage?.let { msg ->
        LaunchedEffect(msg) { viewModel.clearError() }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
    }
}

@Composable
private fun InvestmentAccountRow(account: DestinationAccount, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(account.name) },
        supportingContent = { Text("Toca para ver subcuentas", style = MaterialTheme.typography.bodySmall) }
    )
    HorizontalDivider()
}

@Composable
private fun CreateInvestmentAccountDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva cuenta de inversión") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre (ej: Portafolio Bancolombia)") },
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