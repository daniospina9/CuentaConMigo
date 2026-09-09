package com.example.cuentaconmigo.core.export

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test JUnit puro del modelo de datos del writer (`XlsxCell`, `XlsxRow`,
 * `XlsxSheet`, `XlsxWorkbook`): son data/sealed classes, así que lo único que
 * hay que garantizar es que cada variante se construye con el dato correcto y
 * que la igualdad estructural funciona (la usan varios asserts del writer).
 */
class XlsxModelTest {

    @Test
    fun `cada variante de XlsxCell expone su valor`() {
        assertEquals("hola", (XlsxCell.Text("hola") as XlsxCell.Text).value)
        assertEquals(10L, (XlsxCell.Number(10L) as XlsxCell.Number).value)
        assertEquals(5000L, (XlsxCell.Currency(5000L) as XlsxCell.Currency).value)
        assertEquals(0.3, (XlsxCell.Percent(0.3) as XlsxCell.Percent).value, 0.0)
        assertEquals(
            LocalDate.of(2024, 1, 1),
            (XlsxCell.Date(LocalDate.of(2024, 1, 1)) as XlsxCell.Date).value
        )
        assertEquals("Total", (XlsxCell.Header("Total") as XlsxCell.Header).value)
    }

    @Test
    fun `XlsxRow XlsxSheet y XlsxWorkbook preservan el orden de construccion`() {
        val row = XlsxRow(listOf(XlsxCell.Header("A"), XlsxCell.Text("b")))
        val sheet = XlsxSheet(name = "Hoja1", rows = listOf(row))
        val workbook = XlsxWorkbook(sheets = listOf(sheet))

        assertEquals(1, workbook.sheets.size)
        assertEquals("Hoja1", workbook.sheets.single().name)
        assertEquals(2, workbook.sheets.single().rows.single().cells.size)
    }

    @Test
    fun `dos workbooks con el mismo contenido son iguales estructuralmente`() {
        val a = XlsxWorkbook(listOf(XlsxSheet("Hoja1", listOf(XlsxRow(listOf(XlsxCell.Empty))))))
        val b = XlsxWorkbook(listOf(XlsxSheet("Hoja1", listOf(XlsxRow(listOf(XlsxCell.Empty))))))

        assertEquals(a, b)
    }
}
