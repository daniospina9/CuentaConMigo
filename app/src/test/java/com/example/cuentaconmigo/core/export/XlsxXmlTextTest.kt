package com.example.cuentaconmigo.core.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Test JUnit puro del escapado de texto para SpreadsheetML: entidades XML
 * obligatorias y descarte de caracteres de control ilegales en XML 1.0.
 */
class XlsxXmlTextTest {

    @Test
    fun `escapa ampersand menor que y mayor que`() {
        assertEquals("&amp;&lt;&gt;", XlsxXmlText.escape("&<>"))
    }

    @Test
    fun `escapa comillas dobles y simples`() {
        assertEquals("&quot;&apos;", XlsxXmlText.escape("\"'"))
    }

    @Test
    fun `nombre de categoria con caracteres especiales queda bien escapado`() {
        val raw = "Comida & \"bebida\" <casa>"

        val escaped = XlsxXmlText.escape(raw)

        assertEquals("Comida &amp; &quot;bebida&quot; &lt;casa&gt;", escaped)
    }

    @Test
    fun `caracteres de control ilegales en XML se descartan, no se emiten`() {
        // \u0000 (NUL) y \u0007 (BEL) son caracteres de control no permitidos por
        // XML 1.0 fuera de tab/LF/CR: si se emiten tal cual, corrompen el archivo.
        val raw = "abc\u0000def\u0007ghi"

        val escaped = XlsxXmlText.escape(raw)

        assertEquals("abcdefghi", escaped)
        assertFalse(escaped.contains('\u0000'))
        assertFalse(escaped.contains('\u0007'))
    }

    @Test
    fun `tab salto de linea y retorno de carro se preservan`() {
        val raw = "a\tb\nc\rd"

        val escaped = XlsxXmlText.escape(raw)

        assertEquals("a\tb\nc\rd", escaped)
    }
}
