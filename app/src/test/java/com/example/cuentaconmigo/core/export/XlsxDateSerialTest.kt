package com.example.cuentaconmigo.core.export

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test JUnit puro del cálculo del número de serie de fecha de Excel: días desde
 * 1899-12-30 (offset que ya absorbe el bug histórico del año bisiesto 1900).
 */
class XlsxDateSerialTest {

    @Test
    fun `1900-01-01 es el serial 2, el par conocido documentado por Excel`() {
        val serial = XlsxDateSerial.toExcelSerial(LocalDate.of(1900, 1, 1))

        assertEquals(2L, serial)
    }

    @Test
    fun `2024-01-01 calculado con aritmetica de epoch day`() {
        val expected =
            LocalDate.of(2024, 1, 1).toEpochDay() - LocalDate.of(1899, 12, 30).toEpochDay()

        val serial = XlsxDateSerial.toExcelSerial(LocalDate.of(2024, 1, 1))

        assertEquals(expected, serial)
    }
}
