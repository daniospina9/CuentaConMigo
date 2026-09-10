package com.example.cuentaconmigo.features.reports.export

import com.example.cuentaconmigo.domain.model.CategoryExpenseShare
import com.example.cuentaconmigo.domain.model.IncomeStatement
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.features.reports.FinancialReportState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Test JUnit puro de [FinancialReportExportDataMapper]: verifica las precondiciones
 * de exportabilidad (retorno `null`) y el mapeo campo a campo del caso feliz.
 */
class FinancialReportExportDataMapperTest {

    private val start = LocalDate.of(2026, 8, 1)
    private val end = LocalDate.of(2026, 8, 31)
    private val incomeStatement = IncomeStatement(
        openingBalance = 100_000L,
        periodIncome = 50_000L,
        periodExpense = 20_000L,
        closingBalance = 130_000L
    )

    private val generatedState = FinancialReportState(
        incomeStatement = incomeStatement,
        generated = true,
        isLoading = false
    )

    @Test
    fun `si el estado no fue generado retorna null`() {
        val state = generatedState.copy(generated = false)

        val result = FinancialReportExportDataMapper.from(state, start, end)

        assertNull(result)
    }

    @Test
    fun `si el estado esta cargando retorna null`() {
        val state = generatedState.copy(isLoading = true)

        val result = FinancialReportExportDataMapper.from(state, start, end)

        assertNull(result)
    }

    @Test
    fun `si el estado de resultados es null retorna null`() {
        val state = generatedState.copy(incomeStatement = null)

        val result = FinancialReportExportDataMapper.from(state, start, end)

        assertNull(result)
    }

    @Test
    fun `si la fecha de inicio es null retorna null`() {
        val result = FinancialReportExportDataMapper.from(generatedState, null, end)

        assertNull(result)
    }

    @Test
    fun `si la fecha de fin es null retorna null`() {
        val result = FinancialReportExportDataMapper.from(generatedState, start, null)

        assertNull(result)
    }

    @Test
    fun `caso feliz copia todos los campos tal cual`() {
        val expenseByCategory = listOf(
            CategoryExpenseShare(
                destinationAccountId = 1L,
                destinationAccountName = "Comida",
                total = 20_000L,
                percentage = 100.0
            )
        )
        val transactions = listOf(
            Transaction(
                id = 1L,
                userId = 1L,
                depositAccountId = 1L,
                destinationAccountId = 1L,
                type = TransactionType.EXPENSE,
                amount = 20_000L,
                date = start,
                description = "Almuerzo"
            )
        )
        val categoryNamesById = mapOf(1L to "Comida")
        val state = generatedState.copy(
            expenseByCategory = expenseByCategory,
            totalExpense = 20_000L,
            transactions = transactions,
            categoryNamesById = categoryNamesById
        )

        val result = FinancialReportExportDataMapper.from(state, start, end)

        assertEquals(
            FinancialReportExportData(
                startDate = start,
                endDate = end,
                incomeStatement = incomeStatement,
                expenseByCategory = expenseByCategory,
                totalExpense = 20_000L,
                transactions = transactions,
                categoryNamesById = categoryNamesById
            ),
            result
        )
    }
}
