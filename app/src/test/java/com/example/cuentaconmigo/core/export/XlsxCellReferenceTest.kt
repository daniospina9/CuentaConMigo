package com.example.cuentaconmigo.core.export

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test JUnit puro de la conversión de índice de columna (base 0) a letras de
 * columna estilo Excel, y de la construcción de la referencia de celda (ej. "A1").
 * El caso >26 columnas es el clásico off-by-one de este tipo de conversión.
 */
class XlsxCellReferenceTest {

    @Test
    fun `columna 0 es A`() {
        assertEquals("A", XlsxCellReference.columnLetters(columnIndex = 0))
    }

    @Test
    fun `columna 1 es B`() {
        assertEquals("B", XlsxCellReference.columnLetters(columnIndex = 1))
    }

    @Test
    fun `columna 25 es Z`() {
        assertEquals("Z", XlsxCellReference.columnLetters(columnIndex = 25))
    }

    @Test
    fun `columna 26 es AA, el primer caso de dos letras`() {
        assertEquals("AA", XlsxCellReference.columnLetters(columnIndex = 26))
    }

    @Test
    fun `columna 27 es AB`() {
        assertEquals("AB", XlsxCellReference.columnLetters(columnIndex = 27))
    }

    @Test
    fun `columna 51 es AZ`() {
        assertEquals("AZ", XlsxCellReference.columnLetters(columnIndex = 51))
    }

    @Test
    fun `columna 52 es BA`() {
        assertEquals("BA", XlsxCellReference.columnLetters(columnIndex = 52))
    }

    @Test
    fun `cellRef combina columna y fila 1-based`() {
        assertEquals("A1", XlsxCellReference.cellRef(columnIndex = 0, rowNumber = 1))
        assertEquals("B2", XlsxCellReference.cellRef(columnIndex = 1, rowNumber = 2))
        assertEquals("AA1", XlsxCellReference.cellRef(columnIndex = 26, rowNumber = 1))
        assertEquals("AB10", XlsxCellReference.cellRef(columnIndex = 27, rowNumber = 10))
    }
}
