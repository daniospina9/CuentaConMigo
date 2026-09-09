package com.example.cuentaconmigo.core.export

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.Locale
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test JUnit puro de [XlsxWriter]: arma workbooks en memoria, los escribe a un
 * `ByteArrayOutputStream` y verifica el ZIP resultante entrada por entrada.
 * Ningun test de esta clase abre Excel; la validacion es estructural (ZIP +
 * XML bien formado), no visual.
 */
class XlsxWriterTest {

    private val defaultLocaleBeforeTest: Locale = Locale.getDefault()

    @After
    fun restoreDefaultLocale() {
        Locale.setDefault(defaultLocaleBeforeTest)
    }

    private fun write(workbook: XlsxWorkbook): ByteArray {
        val output = ByteArrayOutputStream()
        XlsxWriter().write(workbook, output)
        return output.toByteArray()
    }

    private fun entries(bytes: ByteArray): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                result[entry.name] = zip.readBytes()
                entry = zip.nextEntry
            }
        }
        return result
    }

    private fun assertWellFormedXml(xml: ByteArray) {
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(xml))
    }

    @Test
    fun `el zip contiene todas las entradas esperadas`() {
        val workbook = XlsxWorkbook(
            sheets = listOf(XlsxSheet("Hoja1", listOf(XlsxRow(listOf(XlsxCell.Text("a"))))))
        )

        val zipEntries = entries(write(workbook))

        assertEquals(
            setOf(
                "[Content_Types].xml",
                "_rels/.rels",
                "xl/workbook.xml",
                "xl/_rels/workbook.xml.rels",
                "xl/styles.xml",
                "xl/worksheets/sheet1.xml"
            ),
            zipEntries.keys
        )
    }

    @Test
    fun `cada parte xml emitida es XML bien formado`() {
        val workbook = XlsxWorkbook(
            sheets = listOf(
                XlsxSheet("Hoja1", listOf(XlsxRow(listOf(XlsxCell.Header("Col"), XlsxCell.Text("valor"))))),
                XlsxSheet("Hoja2", listOf(XlsxRow(listOf(XlsxCell.Number(1L)))))
            )
        )

        val zipEntries = entries(write(workbook))

        zipEntries.forEach { (name, bytes) ->
            try {
                assertWellFormedXml(bytes)
            } catch (e: Exception) {
                throw AssertionError("La entrada '$name' no es XML bien formado: ${e.message}", e)
            }
        }
    }

    @Test
    fun `workbook vacio produce xml bien formado sin hojas`() {
        val workbook = XlsxWorkbook(sheets = emptyList())

        val zipEntries = entries(write(workbook))

        assertWellFormedXml(zipEntries.getValue("xl/workbook.xml"))
        assertWellFormedXml(zipEntries.getValue("[Content_Types].xml"))
        assertWellFormedXml(zipEntries.getValue("xl/_rels/workbook.xml.rels"))
        assertFalse(zipEntries.keys.any { it.startsWith("xl/worksheets/") })
    }

    @Test
    fun `hoja vacia produce xml bien formado`() {
        val workbook = XlsxWorkbook(sheets = listOf(XlsxSheet("Vacia", emptyList())))

        val zipEntries = entries(write(workbook))

        assertWellFormedXml(zipEntries.getValue("xl/worksheets/sheet1.xml"))
    }

    @Test
    fun `filas de distinto largo (ragged) producen xml bien formado`() {
        val workbook = XlsxWorkbook(
            sheets = listOf(
                XlsxSheet(
                    "Ragged",
                    listOf(
                        XlsxRow(listOf(XlsxCell.Text("a"), XlsxCell.Text("b"), XlsxCell.Text("c"))),
                        XlsxRow(listOf(XlsxCell.Text("solo una")))
                    )
                )
            )
        )

        val zipEntries = entries(write(workbook))

        assertWellFormedXml(zipEntries.getValue("xl/worksheets/sheet1.xml"))
    }

    @Test
    fun `referencias de celda son correctas incluyendo el caso mayor a 26 columnas`() {
        val cells = (0..27).map { XlsxCell.Text("v$it") } // 28 celdas: hasta la columna AB
        val workbook = XlsxWorkbook(sheets = listOf(XlsxSheet("Hoja1", listOf(XlsxRow(cells)))))

        val sheetXml = String(entries(write(workbook)).getValue("xl/worksheets/sheet1.xml"), Charsets.UTF_8)

        assertTrue(sheetXml.contains("r=\"A1\""))
        assertTrue(sheetXml.contains("r=\"B1\""))
        assertTrue(sheetXml.contains("r=\"Z1\""))
        assertTrue(sheetXml.contains("r=\"AA1\""))
        assertTrue(sheetXml.contains("r=\"AB1\""))
    }

    @Test
    fun `locale es-CO no corrompe numeros decimales, usa punto y no coma`() {
        Locale.setDefault(Locale("es", "CO"))

        val workbook = XlsxWorkbook(
            sheets = listOf(
                XlsxSheet(
                    "Hoja1",
                    listOf(XlsxRow(listOf(XlsxCell.Percent(0.5), XlsxCell.Date(LocalDate.of(1900, 1, 1)))))
                )
            )
        )

        val sheetXml = String(entries(write(workbook)).getValue("xl/worksheets/sheet1.xml"), Charsets.UTF_8)

        assertTrue("se esperaba un separador decimal '.'", sheetXml.contains("<v>0.5</v>"))
        assertTrue("se esperaba el serial de fecha sin alterar por locale", sheetXml.contains("<v>2</v>"))
        assertFalse("no debe aparecer una coma decimal bajo locale es-CO", sheetXml.contains(","))
    }

    @Test
    fun `nombres de categoria con caracteres especiales y de control quedan escapados en el xml`() {
        // \u0000 (NUL) y \u0007 (BEL) deben descartarse, no emitirse tal cual.
        val raw = "Comida & \"bebida\" <casa>\u0000\u0007"
        val workbook = XlsxWorkbook(
            sheets = listOf(XlsxSheet("Hoja1", listOf(XlsxRow(listOf(XlsxCell.Text(raw))))))
        )

        val sheetXml = String(entries(write(workbook)).getValue("xl/worksheets/sheet1.xml"), Charsets.UTF_8)

        assertWellFormedXml(sheetXml.toByteArray(Charsets.UTF_8))
        assertTrue(sheetXml.contains("Comida &amp; &quot;bebida&quot; &lt;casa&gt;"))
        assertFalse(sheetXml.contains('\u0000'))
        assertFalse(sheetXml.contains('\u0007'))
    }


    @Test
    fun `dos hojas que sanitizan al mismo nombre quedan desambiguadas en workbook xml`() {
        val workbook = XlsxWorkbook(
            sheets = listOf(
                XlsxSheet("Reporte", listOf(XlsxRow(listOf(XlsxCell.Text("a"))))),
                XlsxSheet("Reporte", listOf(XlsxRow(listOf(XlsxCell.Text("b")))))
            )
        )

        val workbookXml = String(entries(write(workbook)).getValue("xl/workbook.xml"), Charsets.UTF_8)

        val nameOccurrences = Regex("name=\"Reporte\"").findAll(workbookXml).count()
        assertEquals("solo la primera hoja debe conservar el nombre sin sufijo", 1, nameOccurrences)
    }

    @Test
    fun `celda Empty no emite un tag c pero no rompe la posicion de las siguientes celdas`() {
        val workbook = XlsxWorkbook(
            sheets = listOf(
                XlsxSheet(
                    "Hoja1",
                    listOf(XlsxRow(listOf(XlsxCell.Text("primero"), XlsxCell.Empty, XlsxCell.Text("tercero"))))
                )
            )
        )

        val sheetXml = String(entries(write(workbook)).getValue("xl/worksheets/sheet1.xml"), Charsets.UTF_8)

        assertTrue(sheetXml.contains("r=\"A1\""))
        assertFalse(sheetXml.contains("r=\"B1\""))
        assertTrue(sheetXml.contains("r=\"C1\""))
    }

    @Test
    fun `celdas numericas usan el estilo correcto segun el tipo`() {
        val workbook = XlsxWorkbook(
            sheets = listOf(
                XlsxSheet(
                    "Hoja1",
                    listOf(
                        XlsxRow(
                            listOf(
                                XlsxCell.Number(10L),
                                XlsxCell.Currency(5000L),
                                XlsxCell.Percent(0.25),
                                XlsxCell.Date(LocalDate.of(1900, 1, 1)),
                                XlsxCell.Header("Total")
                            )
                        )
                    )
                )
            )
        )

        val sheetXml = String(entries(write(workbook)).getValue("xl/worksheets/sheet1.xml"), Charsets.UTF_8)

        assertTrue(sheetXml.contains("r=\"A1\" s=\"${XlsxStyles.STYLE_NUMBER}\""))
        assertTrue(sheetXml.contains("r=\"B1\" s=\"${XlsxStyles.STYLE_CURRENCY}\""))
        assertTrue(sheetXml.contains("r=\"C1\" s=\"${XlsxStyles.STYLE_PERCENT}\""))
        assertTrue(sheetXml.contains("r=\"D1\" s=\"${XlsxStyles.STYLE_DATE}\""))
        assertTrue(sheetXml.contains("r=\"E1\" s=\"${XlsxStyles.STYLE_HEADER}\""))
        assertTrue(sheetXml.contains("<v>5000</v>"))
        assertTrue(sheetXml.contains("<v>2</v>")) // serial de 1900-01-01, cruzado con XlsxDateSerialTest
    }
}
