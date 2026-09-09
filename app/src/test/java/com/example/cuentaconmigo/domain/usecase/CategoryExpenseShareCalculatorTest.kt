package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.AccountTotal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test JUnit puro (sin Android, sin Robolectric) de [CategoryExpenseShareCalculator].
 * Es un objeto sin dependencias de Android ni corrutinas, así que corre en la JVM.
 */
class CategoryExpenseShareCalculatorTest {

    @Test
    fun `caso normal calcula el porcentaje correcto de cada categoria`() {
        val categories = listOf(
            AccountTotal(destinationAccountId = 1, destinationAccountName = "Comida", total = 300),
            AccountTotal(destinationAccountId = 2, destinationAccountName = "Transporte", total = 100),
            AccountTotal(destinationAccountId = 3, destinationAccountName = "Ocio", total = 600)
        )

        val result = CategoryExpenseShareCalculator.calculate(categories)

        assertEquals(1000L, result.totalExpense)
        assertEquals(30.0, result.shares[0].percentage, 0.0)
        assertEquals(10.0, result.shares[1].percentage, 0.0)
        assertEquals(60.0, result.shares[2].percentage, 0.0)
    }

    @Test
    fun `total en cero hace que todos los porcentajes sean 0_0 sin dividir por cero`() {
        val categories = listOf(
            AccountTotal(destinationAccountId = 1, destinationAccountName = "Comida", total = 0),
            AccountTotal(destinationAccountId = 2, destinationAccountName = "Transporte", total = 0)
        )

        val result = CategoryExpenseShareCalculator.calculate(categories)

        assertEquals(0L, result.totalExpense)
        result.shares.forEach { assertEquals(0.0, it.percentage, 0.0) }
    }

    @Test
    fun `lista vacia devuelve total cero y sin filas`() {
        val result = CategoryExpenseShareCalculator.calculate(emptyList())

        assertEquals(0L, result.totalExpense)
        assertTrue(result.shares.isEmpty())
    }

    @Test
    fun `una sola categoria es 100 por ciento`() {
        val categories = listOf(
            AccountTotal(destinationAccountId = 1, destinationAccountName = "Comida", total = 500)
        )

        val result = CategoryExpenseShareCalculator.calculate(categories)

        assertEquals(500L, result.totalExpense)
        assertEquals(100.0, result.shares.single().percentage, 0.0)
    }

    @Test
    fun `porcentajes con division inexacta se afirman contra el Double real, no contra un ideal`() {
        // Con totales 1, 2 y 4 (total = 7) ningún porcentaje individual cae en un
        // decimal exacto. No se exige que la suma de los porcentajes redondeados dé
        // 100.0 — eso es aceptable — sino que cada fila devuelva exactamente el
        // Double que produce la fórmula, sin redondeos adicionales del calculador.
        val categories = listOf(
            AccountTotal(destinationAccountId = 1, destinationAccountName = "A", total = 1),
            AccountTotal(destinationAccountId = 2, destinationAccountName = "B", total = 2),
            AccountTotal(destinationAccountId = 3, destinationAccountName = "C", total = 4)
        )

        val result = CategoryExpenseShareCalculator.calculate(categories)

        assertEquals(1 * 100.0 / 7, result.shares[0].percentage, 0.0)
        assertEquals(2 * 100.0 / 7, result.shares[1].percentage, 0.0)
        assertEquals(4 * 100.0 / 7, result.shares[2].percentage, 0.0)
    }

    @Test
    fun `el orden de las filas de entrada se preserva en el resultado`() {
        val categories = listOf(
            AccountTotal(destinationAccountId = 3, destinationAccountName = "Ocio", total = 10),
            AccountTotal(destinationAccountId = 1, destinationAccountName = "Comida", total = 20),
            AccountTotal(destinationAccountId = 2, destinationAccountName = "Transporte", total = 30)
        )

        val result = CategoryExpenseShareCalculator.calculate(categories)

        assertEquals(
            listOf(3L, 1L, 2L),
            result.shares.map { it.destinationAccountId }
        )
    }
}
