package com.example.cuentaconmigo.core.export

import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * [XlsxNumberText] existe para que ningún `Double` llegue al XML en notación
 * científica ni con coma decimal. Los dos casos rompen consumidores del archivo.
 */
class XlsxNumberTextTest {

    private lateinit var previousLocale: Locale

    @Before
    fun forceColombianLocale() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale("es", "CO"))
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(previousLocale)
    }

    @Test
    fun `un valor chico se escribe en decimal plano y no en notacion cientifica`() {
        // Double.toString(0.0001) devuelve "1.0E-4": válido como xs:double pero
        // frágil para los consumidores del archivo.
        assertEquals("0.00010", XlsxNumberText.plain(0.0001))
    }

    @Test
    fun `un valor grande se escribe en decimal plano`() {
        assertEquals("100000000", XlsxNumberText.plain(1.0E8))
    }

    @Test
    fun `el separador decimal es punto aunque el locale use coma`() {
        assertEquals("0.3", XlsxNumberText.plain(0.3))
        assertEquals(',', Locale.getDefault().let { java.text.DecimalFormatSymbols(it).decimalSeparator })
    }

    @Test
    fun `cero se escribe como cero`() {
        assertEquals("0.0", XlsxNumberText.plain(0.0))
    }

    @Test
    fun `los valores no finitos caen a cero en vez de romper el archivo`() {
        assertEquals("0", XlsxNumberText.plain(Double.NaN))
        assertEquals("0", XlsxNumberText.plain(Double.POSITIVE_INFINITY))
        assertEquals("0", XlsxNumberText.plain(Double.NEGATIVE_INFINITY))
    }
}
