package com.example.cuentaconmigo.features.transactions.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.ParsedTransaction
import com.example.cuentaconmigo.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputScreen(
    onNavigateToForm: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: VoiceInputViewModel = hiltViewModel()
) {
    val state by viewModel.voiceState.collectAsState()
    val context = LocalContext.current

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pendingAction?.invoke()
        pendingAction = null
    }

    fun requestMicThen(action: () -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            action()
        } else {
            pendingAction = action
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(state) {
        if (state is VoiceState.Success) {
            onSuccess()
            viewModel.reset()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Entrada por voz") }) }) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                is VoiceState.Idle -> IdleContent(
                    onMicClick = { requestMicThen { viewModel.startListening() } },
                    onManualClick = onNavigateToForm
                )

                is VoiceState.Listening -> ListeningContent()

                is VoiceState.Parsing -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Procesando: \"${s.transcript}\"")
                }

                is VoiceState.ConfirmPending -> ConfirmContent(
                    parsed = s.parsed,
                    onConfirm = { viewModel.confirmTransaction(s.parsed) },
                    onCancel = { viewModel.reset() }
                )

                is VoiceState.FieldError -> FieldErrorContent(
                    missing = s.missingFields,
                    onRetry = { requestMicThen { viewModel.startListening(partial = s.partial) } },
                    onManual = onNavigateToForm
                )

                is VoiceState.Error -> ErrorContent(
                    message = s.message,
                    onRetry = { viewModel.reset() },
                    onManual = onNavigateToForm
                )

                VoiceState.Success -> {} // handled by LaunchedEffect
            }
        }
    }
}

@Composable
private fun IdleContent(onMicClick: () -> Unit, onManualClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Toca el micrófono y di tu transacción", style = MaterialTheme.typography.bodyLarge)
        Text(
            "Ejemplos:\n" +
            "• \"Gasto 150 mil en mercado, Davivienda\"\n" +
            "• \"Ingresé un millón a Bancolombia\"\n" +
            "• \"Transferí 200 mil de Bancolombia a Nequi\"",
            style = MaterialTheme.typography.bodySmall
        )
        LargeFloatingActionButton(onClick = onMicClick) {
            Icon(Icons.Default.Mic, contentDescription = "Grabar", modifier = Modifier.size(36.dp))
        }
        TextButton(onClick = onManualClick) { Text("Ingresar manualmente") }
    }
}

@Composable
private fun ListeningContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CircularProgressIndicator()
        Text("Escuchando...", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ConfirmContent(parsed: ParsedTransaction, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val typeLabel = when (parsed.type) {
                TransactionType.INCOME   -> "Ingreso"
                TransactionType.EXPENSE  -> "Gasto"
                TransactionType.TRANSFER -> "Transferencia"
                null                     -> "-"
            }
            Text("Confirmar $typeLabel", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()

            if (parsed.type == TransactionType.TRANSFER) {
                ConfirmRow("De", parsed.depositAccountName ?: "-")
                ConfirmRow("A",  parsed.toDepositAccountName ?: "-")
            } else {
                ConfirmRow("Cuenta", parsed.depositAccountName ?: "-")
                parsed.destinationAccountName?.let { ConfirmRow("Categoría", it) }
            }

            ConfirmRow("Monto", parsed.amount?.toCopString() ?: "-")
            parsed.description?.let { ConfirmRow("Descripción", it) }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = onConfirm, modifier = Modifier.weight(1f)) { Text("Confirmar") }
            }
        }
    }
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FieldErrorContent(missing: List<String>, onRetry: () -> Unit, onManual: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Información incompleta", style = MaterialTheme.typography.titleMedium)
        Text("Faltan: ${missing.joinToString()}", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry) { Text("Ingresar información") }
        TextButton(onClick = onManual) { Text("Usar formulario manual") }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onManual: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Error", style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = onRetry) { Text("Intentar de nuevo") }
        TextButton(onClick = onManual) { Text("Usar formulario manual") }
    }
}