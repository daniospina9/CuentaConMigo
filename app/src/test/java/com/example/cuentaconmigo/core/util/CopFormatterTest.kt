package com.example.cuentaconmigo.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CopFormatterTest {

    @Test
    fun `whole amounts render without decimals`() {
        assertEquals("1.500", 150_000L.toAmountInputString())
    }

    @Test
    fun `fractional amounts render two decimals`() {
        assertEquals("1.500,50", 150_050L.toAmountInputString())
        assertEquals("0,05", 5L.toAmountInputString())
    }

    @Test
    fun `round trips through parseToCentavos`() {
        listOf(150_000L, 150_050L, 5L, 1_000_000_00L).forEach {
            assertEquals(it, it.toAmountInputString().parseToCentavos())
        }
    }
}
