package com.example.cuentaconmigo.core.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test JUnit puro de la sanitización de nombres de hoja: caracteres prohibidos
 * por Excel, el límite de 31 caracteres, y la desambiguación cuando dos nombres
 * terminan siendo iguales después de sanitizar.
 */
class XlsxSheetNameSanitizerTest {

    @Test
    fun `caracteres prohibidos se reemplazan`() {
        val result = XlsxSheetNameSanitizer.sanitize(listOf("Reporte: Enero/Febrero"))

        assertEquals(listOf("Reporte_ Enero_Febrero"), result)
        // Ninguno de los caracteres prohibidos por Excel debe sobrevivir.
        assertTrue(result.single().none { it in charArrayOf(':', '\\', '/', '?', '*', '[', ']') })
    }

    @Test
    fun `nombre mayor a 31 caracteres se trunca`() {
        val longName = "Un nombre de hoja demasiado largo para Excel"

        val result = XlsxSheetNameSanitizer.sanitize(listOf(longName))

        assertEquals(31, result.single().length)
        assertEquals(longName.take(31), result.single())
    }

    @Test
    fun `dos nombres iguales despues de sanitizar se desambiguan`() {
        val result = XlsxSheetNameSanitizer.sanitize(listOf("Reporte", "Reporte"))

        assertEquals(2, result.toSet().size)
        assertEquals("Reporte", result[0])
        assertTrue(result[1] != "Reporte")
        assertTrue(result[1].length <= 31)
    }

    @Test
    fun `tres nombres iguales generan tres nombres distintos, todos dentro del limite`() {
        val result = XlsxSheetNameSanitizer.sanitize(listOf("Hoja", "Hoja", "Hoja"))

        assertEquals(3, result.toSet().size)
        result.forEach { assertTrue(it.length <= 31) }
    }

    @Test
    fun `nombres ya distintos y validos no se modifican`() {
        val result = XlsxSheetNameSanitizer.sanitize(listOf("Enero", "Febrero", "Marzo"))

        assertEquals(listOf("Enero", "Febrero", "Marzo"), result)
    }

    @Test
    fun `lista vacia devuelve lista vacia`() {
        val result = XlsxSheetNameSanitizer.sanitize(emptyList())

        assertTrue(result.isEmpty())
    }
}
