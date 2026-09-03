package com.example.cuentaconmigo.core.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test JUnit puro (sin Android, sin Robolectric) del parseo del header de SQLite.
 * [SqliteHeaderValidator] no depende de Android ni de Room, así que corre en la JVM.
 */
class SqliteHeaderValidatorTest {

    private val magicBytes: ByteArray =
        "SQLite format 3".toByteArray(Charsets.US_ASCII) + byteArrayOf(0x00)

    /** Header válido de 100 bytes: magic correcto + user_version en el offset 60. */
    private fun headerWithVersion(version: Int): ByteArray {
        val header = ByteArray(100)
        magicBytes.copyInto(header, destinationOffset = 0)
        header[60] = ((version shr 24) and 0xFF).toByte()
        header[61] = ((version shr 16) and 0xFF).toByte()
        header[62] = ((version shr 8) and 0xFF).toByte()
        header[63] = (version and 0xFF).toByte()
        return header
    }

    @Test
    fun `user_version menor a la version de la app es Valid`() {
        val header = headerWithVersion(version = 17)

        val result = SqliteHeaderValidator.validateHeader(header, appSchemaVersion = 18)

        assertEquals(BackupValidation.Valid, result)
    }

    @Test
    fun `user_version igual a la version de la app es Valid`() {
        val header = headerWithVersion(version = 18)

        val result = SqliteHeaderValidator.validateHeader(header, appSchemaVersion = 18)

        assertEquals(BackupValidation.Valid, result)
    }

    @Test
    fun `user_version mayor a la version de la app es TooNew`() {
        val header = headerWithVersion(version = 25)

        val result = SqliteHeaderValidator.validateHeader(header, appSchemaVersion = 18)

        assertEquals(BackupValidation.TooNew(fileVersion = 25, appVersion = 18), result)
    }

    @Test
    fun `bytes que no son el magic de SQLite son NotSqlite`() {
        val header = ByteArray(100) { 0x41 } // "AAAA..." — no es el magic de SQLite

        val result = SqliteHeaderValidator.validateHeader(header, appSchemaVersion = 18)

        assertEquals(BackupValidation.NotSqlite, result)
    }

    @Test
    fun `archivo mas corto que 100 bytes no lanza excepcion y es invalido`() {
        val shortHeader = headerWithVersion(version = 10).copyOfRange(0, 40)

        val result = SqliteHeaderValidator.validateHeader(shortHeader, appSchemaVersion = 18)

        assertEquals(BackupValidation.NotSqlite, result)
        assertTrue(result is BackupValidation.NotSqlite)
    }

    @Test
    fun `arreglo vacio no lanza excepcion y es invalido`() {
        val result = SqliteHeaderValidator.validateHeader(ByteArray(0), appSchemaVersion = 18)

        assertEquals(BackupValidation.NotSqlite, result)
    }
}
