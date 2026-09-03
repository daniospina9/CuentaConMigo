package com.example.cuentaconmigo.core.backup

/**
 * Resultado de intentar restaurar la base de datos completa desde un archivo importado.
 */
sealed interface RestoreResult {
    /** Se reemplazó la base de datos actual. La app debe reiniciar el proceso para que
     *  Hilt vuelva a inyectar [com.example.cuentaconmigo.core.db.AppDatabase] desde cero. */
    data object Success : RestoreResult

    /** El archivo candidato no pasó la validación; no se tocó la base de datos actual. */
    data class ValidationFailed(val validation: BackupValidation) : RestoreResult

    /** Falló el reemplazo del archivo ya validado (I/O a mitad de camino).
     *  [rolledBack] indica si se pudo restaurar la copia de resguardo previa al intento.
     *  En cualquier caso la conexión de Room quedó cerrada: la app debe reiniciar el
     *  proceso para volver a un estado consistente. */
    data class Failed(val rolledBack: Boolean) : RestoreResult
}
