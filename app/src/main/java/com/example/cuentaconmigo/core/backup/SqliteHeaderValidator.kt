package com.example.cuentaconmigo.core.backup

/**
 * Parseo puro del header de un archivo SQLite (primeros 100 bytes). No depende de
 * Android ni de Room, así que es testeable con JUnit puro (sin instrumentación).
 *
 * Formato del header de SQLite (ver https://www.sqlite.org/fileformat.html#the_database_header):
 * - bytes 0-15: string ASCII "SQLite format 3" seguido de un byte 0x00.
 * - bytes 60-63: `user_version`, entero de 4 bytes big-endian (Room lo usa como
 *   número de versión del schema).
 */
object SqliteHeaderValidator {

    private const val HEADER_SIZE = 100
    private const val USER_VERSION_OFFSET = 60
    private val MAGIC: IntArray = intArrayOf(
        'S'.code, 'Q'.code, 'L'.code, 'i'.code, 't'.code, 'e'.code, ' '.code, 'f'.code,
        'o'.code, 'r'.code, 'm'.code, 'a'.code, 't'.code, ' '.code, '3'.code, 0x00
    )

    /**
     * Valida el header de un archivo candidato a backup.
     *
     * @param header primeros bytes del archivo (se espera al menos 100, el tamaño del
     *   header de SQLite). Un arreglo más corto se trata como inválido, sin lanzar excepción.
     * @param appSchemaVersion versión de schema que soporta esta build de la app
     *   ([com.example.cuentaconmigo.core.db.AppDatabase.SCHEMA_VERSION]).
     */
    fun validateHeader(header: ByteArray, appSchemaVersion: Int): BackupValidation {
        if (header.size < HEADER_SIZE) return BackupValidation.NotSqlite

        for (i in MAGIC.indices) {
            if ((header[i].toInt() and 0xFF) != MAGIC[i]) return BackupValidation.NotSqlite
        }

        val fileVersion = readUserVersion(header)
        return if (fileVersion > appSchemaVersion) {
            BackupValidation.TooNew(fileVersion = fileVersion, appVersion = appSchemaVersion)
        } else {
            BackupValidation.Valid
        }
    }

    private fun readUserVersion(header: ByteArray): Int {
        val o = USER_VERSION_OFFSET
        return ((header[o].toInt() and 0xFF) shl 24) or
            ((header[o + 1].toInt() and 0xFF) shl 16) or
            ((header[o + 2].toInt() and 0xFF) shl 8) or
            (header[o + 3].toInt() and 0xFF)
    }
}
