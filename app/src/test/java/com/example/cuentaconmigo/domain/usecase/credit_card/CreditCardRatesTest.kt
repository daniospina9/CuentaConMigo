package com.example.cuentaconmigo.domain.usecase.credit_card

import org.junit.Assert.assertEquals
import org.junit.Test

class CreditCardRatesTest {

    @Test
    fun `zero annual rate gives zero monthly rate`() {
        assertEquals(0.0, monthlyEffectiveRate(0.0), 1e-12)
    }

    @Test
    fun `compounding the monthly rate twelve times gives back the annual rate`() {
        val monthly = monthlyEffectiveRate(28.5)
        assertEquals(0.285, Math.pow(1.0 + monthly, 12.0) - 1.0, 1e-9)
    }

    @Test
    fun `matches the known value for 12 percent annual`() {
        assertEquals(0.009488793, monthlyEffectiveRate(12.0), 1e-9)
    }
}
