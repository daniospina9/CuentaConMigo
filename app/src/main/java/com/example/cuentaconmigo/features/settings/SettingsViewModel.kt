package com.example.cuentaconmigo.features.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.core.backup.BackupValidation
import com.example.cuentaconmigo.core.backup.DatabaseBackupManager
import com.example.cuentaconmigo.core.backup.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

/** Estado de la operación de backup/restore en curso, mostrado por [SettingsScreen]. */
sealed interface BackupOperationState {
    data object Idle : BackupOperationState
    data object Working : BackupOperationState
    data class ExportReady(val file: File) : BackupOperationState
    data object RestoreCompleted : BackupOperationState
    data class Error(val message: String) : BackupOperationState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupManager: DatabaseBackupManager
) : ViewModel() {

    private val _state = MutableStateFlow<BackupOperationState>(BackupOperationState.Idle)
    val state: StateFlow<BackupOperationState> = _state.asStateFlow()

    fun exportBackup() {
        viewModelScope.launch {
            _state.value = BackupOperationState.Working
            _state.value = runCatching { backupManager.exportBackup() }
                .fold(
                    onSuccess = { BackupOperationState.ExportReady(it) },
                    onFailure = { e ->
                        BackupOperationState.Error(e.message ?: "No se pudo generar la copia de seguridad.")
                    }
                )
        }
    }

    /** Copia el [uri] elegido por el usuario (Storage Access Framework) a un archivo
     *  temporal en caché y dispara la restauración completa desde ahí. */
    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _state.value = BackupOperationState.Working
            val outcome = withContext(Dispatchers.IO) {
                val tempFile = File(context.cacheDir, "import-candidate.db")
                try {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            tempFile.outputStream().use { output -> input.copyTo(output) }
                        } ?: throw IOException("No se pudo abrir el archivo seleccionado.")
                        backupManager.restoreBackup(tempFile)
                    }
                } finally {
                    // Es una copia completa de la base: no dejarla viviendo en caché
                    // después de la operación, salga bien o mal.
                    tempFile.delete()
                }
            }
            _state.value = outcome.fold(
                onSuccess = { result -> toUiState(result) },
                onFailure = { e ->
                    BackupOperationState.Error(e.message ?: "No se pudo leer el archivo seleccionado.")
                }
            )
        }
    }

    private fun toUiState(result: RestoreResult): BackupOperationState = when (result) {
        is RestoreResult.Success -> BackupOperationState.RestoreCompleted
        is RestoreResult.ValidationFailed -> BackupOperationState.Error(validationMessage(result.validation))
        is RestoreResult.Failed -> BackupOperationState.Error(
            if (result.rolledBack) {
                "No se pudo completar la restauración. Se recuperaron tus datos anteriores, " +
                    "pero conviene reiniciar la aplicación."
            } else {
                "Ocurrió un error grave durante la restauración y no se pudo recuperar el estado " +
                    "anterior. Reinicia la aplicación."
            }
        )
    }

    private fun validationMessage(validation: BackupValidation): String = when (validation) {
        is BackupValidation.NotSqlite ->
            "El archivo elegido no es una copia de seguridad válida de CuentaConMigo."
        is BackupValidation.TooNew ->
            "Esta copia de seguridad fue creada con una versión más nueva de la app " +
                "(versión de datos ${validation.fileVersion}, esta app soporta hasta " +
                "${validation.appVersion}). Actualiza la app antes de importarla."
        is BackupValidation.Corrupt ->
            "El archivo está dañado y no se puede restaurar."
        is BackupValidation.Valid ->
            "" // No debería llegar acá: Valid no produce un mensaje de error.
    }

    fun consumeState() {
        _state.value = BackupOperationState.Idle
    }
}
