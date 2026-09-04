package com.example.cuentaconmigo.features.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.ui.theme.brand
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val updateCheckState by viewModel.updateCheckState.collectAsState()

    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    // SAF: no requiere ningún permiso de almacenamiento. "*/*" porque los .db suelen
    // reportarse como octet-stream y algunos proveedores no los tipan bien.
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    // Apenas el export está listo, dispara el intent para compartirlo y vuelve a Idle.
    LaunchedEffect(state) {
        val current = state
        if (current is BackupOperationState.ExportReady) {
            shareBackupFile(context, current.file)
            viewModel.consumeState()
        }
    }

    val isWorking = state is BackupOperationState.Working
    val isCheckingForUpdates = updateCheckState is UpdateCheckState.Checking
    // Una sola condición de "hay algo en curso": evita que se puedan disparar dos
    // operaciones a la vez (p. ej. exportar mientras se busca una actualización), lo
    // que podría terminar mostrando dos AlertDialog superpuestos.
    val isBusy = isWorking || isCheckingForUpdates

    // MainActivity llama a enableEdgeToEdge(), así que el contenido se dibuja debajo de
    // las barras del sistema salvo que alguien aplique los insets. El Scaffold es quien
    // los resuelve, y es el patrón que usa el resto de las pantallas del proyecto.
    // El header propio se mantiene (mismo lenguaje visual que Home) dentro del área
    // que el Scaffold deja libre.
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            // ── Header — mismo lenguaje visual que el saludo de Home ────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.brand.accountChipContainer)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = onNavigateBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.brand.accountChipIcon,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Ajustes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Copia de seguridad y restauración de tus datos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingsActionCard(
                    icon = Icons.Default.CloudUpload,
                    title = "Exportar copia de seguridad",
                    description = "Genera un archivo con todos tus datos y compártelo por WhatsApp, Gmail, Drive, etc.",
                    enabled = !isBusy,
                    onClick = { viewModel.exportBackup() }
                )

                SettingsActionCard(
                    icon = Icons.Default.CloudDownload,
                    title = "Importar copia de seguridad",
                    description = "Reemplaza todos los datos actuales por los de un archivo de respaldo.",
                    enabled = !isBusy,
                    onClick = { importLauncher.launch(arrayOf("*/*")) }
                )

                LoadingIndicatorRow(visible = isWorking)

                SettingsActionCard(
                    icon = Icons.Default.SystemUpdate,
                    title = "Buscar actualizaciones",
                    description = "Comprueba si hay una versión más nueva de la app disponible.",
                    enabled = !isBusy,
                    onClick = { viewModel.checkForUpdates() }
                )

                LoadingIndicatorRow(visible = isCheckingForUpdates)
            }
        }
    }

    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirm = false
                pendingImportUri = null
            },
            title = { Text("Importar copia de seguridad") },
            text = {
                Text(
                    "Esta acción reemplaza TODOS los datos de TODOS los usuarios en este " +
                        "dispositivo por los del archivo elegido. No se puede deshacer. " +
                        "¿Deseas continuar?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri
                    showImportConfirm = false
                    pendingImportUri = null
                    if (uri != null) viewModel.importBackup(uri)
                }) {
                    Text("Reemplazar datos")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    pendingImportUri = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val currentState = state
    if (currentState is BackupOperationState.RestoreCompleted) {
        AlertDialog(
            onDismissRequest = { /* No se puede cancelar: hay que reiniciar para continuar. */ },
            title = { Text("Restauración completa") },
            text = { Text("Tus datos fueron restaurados. La aplicación se reiniciará ahora.") },
            confirmButton = {
                TextButton(onClick = { restartApp(context) }) {
                    Text("Reiniciar")
                }
            }
        )
    }

    if (currentState is BackupOperationState.Error) {
        ErrorDialog(message = currentState.message, onDismiss = { viewModel.consumeState() })
    }

    val currentUpdateCheckState = updateCheckState
    if (currentUpdateCheckState is UpdateCheckState.UpdateAvailable) {
        val manifest = currentUpdateCheckState.manifest
        AlertDialog(
            onDismissRequest = { viewModel.consumeUpdateCheckState() },
            title = { Text("Nueva versión disponible") },
            text = {
                Column {
                    Text(
                        text = "Versión ${manifest.versionName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (manifest.notes.isNotBlank()) {
                        Text(
                            text = manifest.notes,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.consumeUpdateCheckState()
                    openInBrowser(
                        context = context,
                        url = manifest.apkUrl,
                        onFailure = { viewModel.onDownloadLinkUnavailable() }
                    )
                }) {
                    Text("Descargar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.consumeUpdateCheckState() }) {
                    Text("Ahora no")
                }
            }
        )
    }

    if (currentUpdateCheckState is UpdateCheckState.Error) {
        ErrorDialog(
            message = currentUpdateCheckState.message,
            onDismiss = { viewModel.consumeUpdateCheckState() }
        )
    }

    // Feedback no modal para una confirmación que no requiere acción de la persona
    // usuaria, en línea con el patrón ya usado en el resto de la app (ver
    // TransactionFormScreen): se muestra unos segundos y se limpia sola. Va envuelto
    // en un Box de pantalla completa alineado abajo porque, a diferencia de AlertDialog
    // (una ventana real), Snackbar es un composable de layout más: sin este wrapper
    // dibujaría arriba a la izquierda, superpuesto con el encabezado.
    if (currentUpdateCheckState is UpdateCheckState.UpToDate) {
        LaunchedEffect(currentUpdateCheckState) {
            delay(3_000)
            viewModel.consumeUpdateCheckState()
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(modifier = Modifier.padding(16.dp)) {
                Text("Ya tienes la última versión instalada.")
            }
        }
    }
}

@Composable
private fun LoadingIndicatorRow(visible: Boolean) {
    if (visible) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("No se pudo completar la operación") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.brand.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.brand.accountChipContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.brand.accountChipIcon,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun shareBackupFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir copia de seguridad"))
}

/**
 * A propósito deriva al navegador en vez de descargar/instalar el APK dentro de la
 * app: nunca se pide `REQUEST_INSTALL_PACKAGES` ni se toca PackageInstaller.
 *
 * `url` viene de un servidor remoto (el manifiesto de versión): aunque
 * [com.example.cuentaconmigo.core.update.UpdateManifestParser] ya valida que sea un
 * http(s) URL, el dispositivo puede no tener ninguna app capaz de resolverlo, lo que
 * hace que `startActivity` lance `ActivityNotFoundException`. Se captura acá para que
 * un manifiesto o dispositivo inesperado no tumbe la app entera.
 */
private fun openInBrowser(context: Context, url: String, onFailure: () -> Unit) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        onFailure()
    }
}

private fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}
