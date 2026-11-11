package com.example.cuentaconmigo.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecimalInputTest {

    @Test
    fun `parses dot decimal`() = assertEquals(28.5, "28.5".parseNonNegativeDecimal()!!, 0.0)

    @Test
    fun `parses comma decimal`() = assertEquals(28.5, "28,5".parseNonNegativeDecimal()!!, 0.0)

    @Test
    fun `parses zero`() = assertEquals(0.0, "0".parseNonNegativeDecimal()!!, 0.0)

    @Test
    fun `parses whitespace padded`() = assertEquals(5.0, "  5 ".parseNonNegativeDecimal()!!, 0.0)

    @Test
    fun `blank is null`() {
        assertNull("".parseNonNegativeDecimal())
        assertNull("   ".parseNonNegativeDecimal())
    }

    @Test
    fun `invalid text is null`() {
        assertNull("28.5%".parseNonNegativeDecimal())
        assertNull("2.8.5".parseNonNegativeDecimal())
        assertNull("abc".parseNonNegativeDecimal())
        assertNull("28.5f".parseNonNegativeDecimal())
        assertNull("1e5".parseNonNegativeDecimal())
    }

    @Test
    fun `negative is null`() = assertNull("-1".parseNonNegativeDecimal())

    @Test
    fun `non finite is null`() {
        assertNull("NaN".parseNonNegativeDecimal())
        assertNull("Infinity".parseNonNegativeDecimal())
    }
}
