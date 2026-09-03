package com.example.cuentaconmigo.core.backup

/**
 * Resultado tipado de validar un archivo candidato a backup de la base de datos.
 *
 * Se valida en tres pasos, en orden: (1) el archivo es realmente una base SQLite,
 * (2) su `user_version` no es más nueva que el schema que soporta esta build de la
 * app (Room no soporta downgrade de versión), y (3) el archivo no está corrupto
 * (`PRAGMA integrity_check`).
 */
sealed interface BackupValidation {
    /** El archivo es un SQLite válido, de una versión soportada, y no está corrupto. */
    data object Valid : BackupValidation

    /** El `user_version` del archivo es mayor a la versión de schema que soporta la app. */
    data class TooNew(val fileVersion: Int, val appVersion: Int) : BackupValidation

    /** Los primeros bytes del archivo no coinciden con el magic header de SQLite. */
    data object NotSqlite : BackupValidation

    /** El archivo es un SQLite de versión soportada pero falló `PRAGMA integrity_check`. */
    data object Corrupt : BackupValidation
}
