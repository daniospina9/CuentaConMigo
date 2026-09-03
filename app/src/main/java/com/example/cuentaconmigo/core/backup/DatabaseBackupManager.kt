package com.example.cuentaconmigo.core.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.cuentaconmigo.core.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backup y restore de la base de datos completa como copia cruda del archivo SQLite
 * (no un export a JSON). La compatibilidad hacia adelante la resuelven las migraciones
 * de Room ya existentes: al abrir un backup viejo, Room corre las migraciones
 * necesarias con normalidad.
 */
@Singleton
class DatabaseBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {

    private val backupsDir: File
        get() = File(context.cacheDir, "backups").apply { mkdirs() }

    /**
     * Directorio separado de [backupsDir] a propósito: la copia de resguardo previa a
     * una restauración es una red de seguridad, no un backup descartable, y no debe
     * quedar expuesta al borrado que hace [exportBackup].
     */
    private val restoreDir: File
        get() = File(context.cacheDir, "restore").apply { mkdirs() }

    private val dbFile: File
        get() = context.getDatabasePath(DB_NAME)

    /**
     * Genera un backup nuevo en `cacheDir/backups`, limpiando los anteriores (es
     * caché, no un archivo del usuario). Devuelve el archivo resultante.
     */
    suspend fun exportBackup(): File = withContext(Dispatchers.IO) {
        // Room trabaja en modo WAL: si copiamos el .db sin forzar un checkpoint,
        // los archivos -wal/-shm pueden tener escrituras no volcadas todavía y el
        // backup sale desactualizado en silencio. TRUNCATE vuelca el WAL al archivo
        // principal y además lo deja en cero, así el .db se basta a sí mismo.
        //
        // La primera columna del resultado es `busy`: si vale 1, el checkpoint NO se
        // completó (había lectores activos sobre la base) y la copia quedaría vieja.
        // Fallar con un error visible es preferible a entregar un backup incompleto
        // que solo se descubre el día que hay que restaurarlo.
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").use { cursor ->
            if (!cursor.moveToFirst() || cursor.getInt(0) != 0) {
                throw IOException(
                    "No se pudo consolidar la base de datos antes de copiarla. " +
                        "Cierra las demás pantallas e intenta de nuevo."
                )
            }
        }

        val dir = backupsDir
        dir.listFiles()?.forEach { it.delete() }

        val timestamp = SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.US).format(Date())
        val backupFile = File(dir, "cuentaconmigo-backup-$timestamp.db")
        dbFile.copyTo(backupFile, overwrite = true)
        backupFile
    }

    /**
     * Valida un archivo candidato a backup, en orden: (1) magic header de SQLite,
     * (2) `user_version` no mayor a la versión de schema soportada, (3) integridad
     * (`PRAGMA integrity_check`) abriendo una copia de solo lectura.
     */
    suspend fun validate(file: File): BackupValidation = withContext(Dispatchers.IO) {
        val header = ByteArray(100)
        // `InputStream.read(ByteArray)` no garantiza llenar el arreglo aunque el archivo
        // tenga bytes de sobra, así que se lee en bucle hasta completar el header o
        // llegar al fin del archivo.
        val bytesRead = try {
            FileInputStream(file).use { input ->
                var total = 0
                while (total < header.size) {
                    val read = input.read(header, total, header.size - total)
                    if (read < 0) break
                    total += read
                }
                total
            }
        } catch (e: IOException) {
            -1
        }
        if (bytesRead < header.size) return@withContext BackupValidation.NotSqlite

        val headerResult = SqliteHeaderValidator.validateHeader(header, AppDatabase.SCHEMA_VERSION)
        if (headerResult !is BackupValidation.Valid) return@withContext headerResult

        checkIntegrity(file)
    }

    private fun checkIntegrity(file: File): BackupValidation {
        var database: SQLiteDatabase? = null
        return try {
            database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
            database.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                if (cursor.moveToFirst() && cursor.getString(0) == "ok") {
                    BackupValidation.Valid
                } else {
                    BackupValidation.Corrupt
                }
            }
        } catch (e: Exception) {
            BackupValidation.Corrupt
        } finally {
            database?.close()
        }
    }

    /**
     * Restaura la base de datos completa desde [sourceFile]. Reemplaza TODOS los
     * datos de TODOS los usuarios; la confirmación explícita es responsabilidad de la UI.
     *
     * Antes de pisar la base actual guarda una copia de resguardo en caché para poder
     * volver atrás si algo falla a mitad de camino. En caso de éxito o de fallo con
     * rollback, la conexión de Room queda cerrada: el llamador debe reiniciar el
     * proceso para que Hilt vuelva a inyectar [AppDatabase] desde cero.
     */
    suspend fun restoreBackup(sourceFile: File): RestoreResult = withContext(Dispatchers.IO) {
        val validation = validate(sourceFile)
        if (validation !is BackupValidation.Valid) {
            return@withContext RestoreResult.ValidationFailed(validation)
        }

        val preRestoreCopy = File(restoreDir, "pre-restore.db")
        try {
            dbFile.copyTo(preRestoreCopy, overwrite = true)

            db.close()

            // Si quedan -wal/-shm viejos, SQLite mezcla el WAL anterior con la base
            // nueva y corrompe todo.
            deleteWalShmFiles()
            sourceFile.copyTo(dbFile, overwrite = true)
            RestoreResult.Success
        } catch (e: Exception) {
            val rolledBack = try {
                preRestoreCopy.copyTo(dbFile, overwrite = true)
                deleteWalShmFiles()
                true
            } catch (rollbackError: Exception) {
                false
            }
            RestoreResult.Failed(rolledBack = rolledBack)
        }
    }

    private fun deleteWalShmFiles() {
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()
    }

    companion object {
        private const val DB_NAME = "cuentaconmigo.db"
    }
}
