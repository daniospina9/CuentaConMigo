package com.example.cuentaconmigo.core.export

import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test JUnit puro de `xl/styles.xml`. Excel es estricto con este archivo: los
 * quiebres típicos de un xlsx armado a mano son (1) no declarar `fills` con
 * `none` y `gray125` como las dos primeras entradas, y (2) usar un `numFmtId`
 * reservado (< 164) para un formato custom.
 */
class XlsxStylesTest {

    private fun parse(xml: String) =
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))

    @Test
    fun `styles xml es XML bien formado`() {
        val document = parse(XlsxStyles.xml())

        assertEquals("styleSheet", document.documentElement.tagName)
    }

    @Test
    fun `las dos primeras fills son none y gray125, en ese orden`() {
        val document = parse(XlsxStyles.xml())

        val fills = document.getElementsByTagName("fill")
        assertTrue("se esperaban al menos 2 fills", fills.length >= 2)

        val firstPattern = (fills.item(0) as org.w3c.dom.Element)
            .getElementsByTagName("patternFill").item(0) as org.w3c.dom.Element
        val secondPattern = (fills.item(1) as org.w3c.dom.Element)
            .getElementsByTagName("patternFill").item(0) as org.w3c.dom.Element

        assertEquals("none", firstPattern.getAttribute("patternType"))
        assertEquals("gray125", secondPattern.getAttribute("patternType"))
    }

    @Test
    fun `los numFmtId custom son mayores o iguales a 164`() {
        val document = parse(XlsxStyles.xml())

        val numFmts = document.getElementsByTagName("numFmt")
        assertTrue("se esperaba al menos un numFmt custom", numFmts.length > 0)
        for (i in 0 until numFmts.length) {
            val id = (numFmts.item(i) as org.w3c.dom.Element).getAttribute("numFmtId").toInt()
            assertTrue("numFmtId $id debe ser >= 164", id >= 164)
        }
    }

    @Test
    fun `el orden de las secciones respeta el schema de SpreadsheetML`() {
        val document = parse(XlsxStyles.xml())

        val childNames = document.documentElement.childNodes.let { nodes ->
            (0 until nodes.length)
                .map { nodes.item(it) }
                .filterIsInstance<org.w3c.dom.Element>()
                .map { it.tagName }
        }

        assertEquals(
            listOf("numFmts", "fonts", "fills", "borders", "cellStyleXfs", "cellXfs", "cellStyles"),
            childNames
        )
    }

    @Test
    fun `hay una cellXfs por cada indice de estilo declarado`() {
        val document = parse(XlsxStyles.xml())

        val cellXfsParent = document.getElementsByTagName("cellXfs").item(0) as org.w3c.dom.Element
        val xfs = cellXfsParent.getElementsByTagName("xf")

        val maxDeclaredIndex = maxOf(
            XlsxStyles.STYLE_GENERAL,
            XlsxStyles.STYLE_NUMBER,
            XlsxStyles.STYLE_CURRENCY,
            XlsxStyles.STYLE_PERCENT,
            XlsxStyles.STYLE_DATE,
            XlsxStyles.STYLE_HEADER
        )

        assertTrue(xfs.length >= maxDeclaredIndex + 1)
    }

    @Test
    fun `el estilo de Header usa un fontId distinto al general, para quedar en negrita`() {
        val document = parse(XlsxStyles.xml())

        val cellXfsParent = document.getElementsByTagName("cellXfs").item(0) as org.w3c.dom.Element
        val xfs = cellXfsParent.getElementsByTagName("xf")

        val generalFontId = (xfs.item(XlsxStyles.STYLE_GENERAL) as org.w3c.dom.Element).getAttribute("fontId")
        val headerFontId = (xfs.item(XlsxStyles.STYLE_HEADER) as org.w3c.dom.Element).getAttribute("fontId")

        assertTrue(generalFontId != headerFontId)
    }

    @Test
    fun `declara el estilo por defecto Normal, que Excel espera encontrar`() {
        // Sin <cellStyles> el libro no declara el estilo "Normal" (builtinId 0).
        // openpyxl avisa "Workbook contains no default style" y Excel puede
        // ofrecer "reparar" el archivo. Va DESPUES de <cellXfs> por el schema.
        val document = parse(XlsxStyles.xml())

        val cellStyles = document.getElementsByTagName("cellStyles")
        assertTrue(cellStyles.length == 1)

        val style = (cellStyles.item(0) as org.w3c.dom.Element)
            .getElementsByTagName("cellStyle").item(0) as org.w3c.dom.Element
        assertEquals("Normal", style.getAttribute("name"))
        assertEquals("0", style.getAttribute("xfId"))
        assertEquals("0", style.getAttribute("builtinId"))
    }
}
