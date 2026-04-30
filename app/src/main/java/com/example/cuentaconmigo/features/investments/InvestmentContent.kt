package com.example.cuentaconmigo.features.investments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.core.util.toSignedCopString
import com.example.cuentaconmigo.domain.model.InvestmentFluctuation
import java.time.format.DateTimeFormatter

@Composable
fun InvestmentContent(viewModel: InvestmentViewModel = hiltViewModel()) {
    val balance by viewModel.balance.collectAsState()
    val fluctuations by viewModel.fluctuations.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var fluctuationToDelete by remember { mutableStateOf<InvestmentFluctuation?>(null) }

    Column(Modifier.fillMaxSize()) {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Balance de inversiones", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    balance.toSignedCopString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (balance >= 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Movimientos", style = MaterialTheme.typography.labelLarge)
            FilledTonalButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Registrar")
            }
        }

        if (fluctuations.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin movimientos registrados", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(fluctuations, key = { it.id }) { fluctuation ->
                    FluctuationItem(
                        fluctuation = fluctuation,
                        onDelete = { fluctuationToDelete = fluctuation }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddFluctuationDialog(
            onConfirm = { amount, description ->
                viewModel.addFluctuation(amount, description)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    fluctuationToDelete?.let { fl ->
        AlertDialog(
            onDismissRequest = { fluctuationToDelete = null },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Eliminar este movimiento de ${fl.amount.toSignedCopString()}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFluctuation(fl)
                    fluctuationToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { fluctuationToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    errorMessage?.let { msg ->
        LaunchedEffect(msg) { viewModel.clearError() }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
    }
}

@Composable
private fun FluctuationItem(fluctuation: InvestmentFluctuation, onDelete: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    ListItem(
        headlineContent = { Text(fluctuation.amount.toSignedCopString()) },
        supportingContent = {
            Column {
                Text(fluctuation.date.format(formatter), style = MaterialTheme.typography.bodySmall)
                fluctuation.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    )
}

@Composable
private fun AddFluctuationDialog(
    onConfirm: (Long, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var isGain by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val isValid = amountText.filter { it.isDigit() }.toLongOrNull()?.let { it > 0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar movimiento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isGain,
                        onClick = { isGain = true },
                        label = { Text("Ganancia") }
                    )
                    FilterChip(
                        selected = !isGain,
                        onClick = { isGain = false },
                        label = { Text("Pérdida") }
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto (COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                onClick = {
                    val raw = amountText.filter { it.isDigit() }.toLong()
                    onConfirm(if (isGain) raw else -raw, description.ifBlank { null })
                },
                enabled = isValid
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
