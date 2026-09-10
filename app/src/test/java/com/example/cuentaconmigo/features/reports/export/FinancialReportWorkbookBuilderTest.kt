package com.example.cuentaconmigo.features.reports.export

import com.example.cuentaconmigo.core.export.XlsxCell
import com.example.cuentaconmigo.core.export.XlsxRow
import com.example.cuentaconmigo.domain.model.CategoryExpenseShare
import com.example.cuentaconmigo.domain.model.IncomeStatement
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test JUnit puro de [FinancialReportWorkbookBuilder]: verifica que la traducción de
 * [FinancialReportExportData] a [com.example.cuentaconmigo.core.export.XlsxWorkbook]
 * respeta el orden, formato y reglas de negocio exactos del informe personalizado.
 */
class FinancialReportWorkbookBuilderTest {

    private val start = LocalDate.of(2024, 1, 1)
    private val end = LocalDate.of(2024, 1, 31)

    private fun minimalData(
        incomeStatement: IncomeStatement = IncomeStatement(
            openingBalance = 100_000L,
            periodIncome = 50_000L,
            periodExpense = 20_000L,
            closingBalance = 130_000L
        ),
        expenseByCategory: List<CategoryExpenseShare> = emptyList(),
        totalExpense: Long = 0L,
        transactions: List<Transaction> = emptyList(),
        categoryNamesById: Map<Long, String> = emptyMap()
    ) = FinancialReportExportData(
        startDate = start,
        endDate = end,
        incomeStatement = incomeStatement,
        expenseByCategory = expenseByCategory,
        totalExpense = totalExpense,
        transactions = transactions,
        categoryNamesById = categoryNamesById
    )

    @Test
    fun `el workbook tiene exactamente una hoja llamada Informe`() {
        val workbook = FinancialReportWorkbookBuilder.build(minimalData())

        assertEquals(1, workbook.sheets.size)
        assertEquals("Informe", workbook.sheets.single().name)
    }

    @Test
    fun `las primeras dos filas son el titulo y el periodo`() {
        val rows = FinancialReportWorkbookBuilder.build(minimalData()).sheets.single().rows

        assertEquals(XlsxCell.Header("Informe personalizado"), rows[0].cells.single())
        assertEquals(
            listOf(XlsxCell.Text("Período"), XlsxCell.Date(start), XlsxCell.Date(end)),
            rows[1].cells
        )
    }

    @Test
    fun `el bloque de estado de resultados tiene las etiquetas y montos correctos en orden`() {
        val incomeStatement = IncomeStatement(
            openingBalance = 100_000L,
            periodIncome = 50_000L,
            periodExpense = 20_000L,
            closingBalance = 130_000L
        )
        val rows = FinancialReportWorkbookBuilder.build(minimalData(incomeStatement = incomeStatement))
            .sheets.single().rows

        assertEquals(XlsxRow(emptyList()), rows[2])
        assertEquals(XlsxCell.Header("Estado de resultados"), rows[3].cells.single())
        assertEquals(
            listOf(XlsxCell.Header("Concepto"), XlsxCell.Header("Monto")),
            rows[4].cells
        )
        assertEquals(
            listOf(XlsxCell.Text("Saldo inicial"), XlsxCell.Currency(100_000L)),
            rows[5].cells
        )
        assertEquals(
            listOf(XlsxCell.Text("+ Ingresos del período"), XlsxCell.Currency(50_000L)),
            rows[6].cells
        )
        assertEquals(
            listOf(XlsxCell.Text("- Gastos del período"), XlsxCell.Currency(20_000L)),
            rows[7].cells
        )
        assertEquals(
            listOf(XlsxCell.Text("Saldo final"), XlsxCell.Currency(130_000L)),
            rows[8].cells
        )
    }

    @Test
    fun `sin gastos en el periodo se emite un unico mensaje sin encabezado ni total`() {
        val rows = FinancialReportWorkbookBuilder.build(
            minimalData(expenseByCategory = emptyList())
        ).sheets.single().rows

        assertEquals(XlsxRow(emptyList()), rows[9])
        assertEquals(XlsxCell.Header("Gastos por categoría"), rows[10].cells.single())
        assertEquals(XlsxCell.Text("Sin gastos en el período"), rows[11].cells.single())
        assertEquals(XlsxCell.Header("Detalle de transacciones"), rows[13].cells.single())
    }

    @Test
    fun `el porcentaje de gasto por categoria se divide por 100 para quedar como fraccion`() {
        val share = CategoryExpenseShare(
            destinationAccountId = 1L,
            destinationAccountName = "Comida",
            total = 32_100L,
            percentage = 32.1
        )
        val rows = FinancialReportWorkbookBuilder.build(
            minimalData(expenseByCategory = listOf(share), totalExpense = 32_100L)
        ).sheets.single().rows

        val categoryRow = rows[12]
        val percentCell = categoryRow.cells[2] as XlsxCell.Percent
        assertEquals(0.321, percentCell.value, 0.0001)
    }

    @Test
    fun `las filas de categoria preservan el orden de entrada y la fila Total suma el total`() {
        val shares = listOf(
            CategoryExpenseShare(1L, "Comida", 30_000L, 60.0),
            CategoryExpenseShare(2L, "Transporte", 20_000L, 40.0)
        )
        val rows = FinancialReportWorkbookBuilder.build(
            minimalData(expenseByCategory = shares, totalExpense = 50_000L)
        ).sheets.single().rows

        assertEquals(XlsxCell.Header("Gastos por categoría"), rows[10].cells.single())
        assertEquals(
            listOf(XlsxCell.Header("Categoría"), XlsxCell.Header("Monto"), XlsxCell.Header("% del total")),
            rows[11].cells
        )
        assertEquals(
            listOf(XlsxCell.Text("Comida"), XlsxCell.Currency(30_000L)),
            rows[12].cells.subList(0, 2)
        )
        assertEquals(
            listOf(XlsxCell.Text("Transporte"), XlsxCell.Currency(20_000L)),
            rows[13].cells.subList(0, 2)
        )
        assertEquals(
            listOf(XlsxCell.Header("Total"), XlsxCell.Currency(50_000L)),
            rows[14].cells
        )
    }

    @Test
    fun `sin transacciones en el periodo se emite un unico mensaje sin encabezado`() {
        val rows = FinancialReportWorkbookBuilder.build(
            minimalData(transactions = emptyList())
        ).sheets.single().rows

        assertEquals(XlsxCell.Header("Detalle de transacciones"), rows[13].cells.single())
        assertEquals(
            XlsxCell.Text("Sin transacciones en el período seleccionado"),
            rows[14].cells.single()
        )
    }

    private fun transaction(
        type: TransactionType,
        amount: Long = 10_000L,
        date: LocalDate = start,
        destinationAccountId: Long? = null,
        description: String? = null
    ) = Transaction(
        id = 1L,
        userId = 1L,
        depositAccountId = 1L,
        destinationAccountId = destinationAccountId,
        type = type,
        amount = amount,
        date = date,
        description = description
    )

    @Test
    fun `la fila de encabezado y las etiquetas de tipo de transaccion son correctas`() {
        val transactions = listOf(
            transaction(TransactionType.INCOME),
            transaction(TransactionType.EXPENSE),
            transaction(TransactionType.TRANSFER)
        )
        val rows = FinancialReportWorkbookBuilder.build(
            minimalData(transactions = transactions)
        ).sheets.single().rows

        assertEquals(
            listOf(
                XlsxCell.Header("Fecha"),
                XlsxCell.Header("Tipo"),
                XlsxCell.Header("Categoría"),
                XlsxCell.Header("Descripción"),
                XlsxCell.Header("Monto")
            ),
            rows[14].cells
        )
        assertEquals("Ingreso", (rows[15].cells[1] as XlsxCell.Text).value)
        assertEquals("Gasto", (rows[16].cells[1] as XlsxCell.Text).value)
        assertEquals("Transferencia", (rows[17].cells[1] as XlsxCell.Text).value)
    }

    @Test
    fun `la celda de categoria es vacia salvo para gastos con categoria resuelta`() {
        val transactions = listOf(
            transaction(TransactionType.INCOME, destinationAccountId = 1L),
            transaction(TransactionType.TRANSFER, destinationAccountId = 1L),
            transaction(TransactionType.EXPENSE, destinationAccountId = 99L)
        )
        val rows = FinancialReportWorkbookBuilder.build(
            minimalData(transactions = transactions, categoryNamesById = mapOf(1L to "Comida"))
        ).sheets.single().rows

        assertEquals(XlsxCell.Empty, rows[15].cells[2])
        assertEquals(XlsxCell.Empty, rows[16].cells[2])
        assertEquals(XlsxCell.Empty, rows[17].cells[2])
    }

    @Test
    fun `la celda de descripcion es vacia para descripcion nula o en blanco`() {
        val transactions = listOf(
            transaction(TransactionType.INCOME, description = null),
            transaction(TransactionType.INCOME, description = "   ")
        )
        val rows = FinancialReportWorkbookBuilder.build(
            minimalData(transactions = transactions)
        ).sheets.single().rows

        assertEquals(XlsxCell.Empty, rows[15].cells[3])
        assertEquals(XlsxCell.Empty, rows[16].cells[3])
    }

    @Test
    fun `las filas vacias separadoras estan exactamente entre las secciones`() {
        val rows = FinancialReportWorkbookBuilder.build(minimalData()).sheets.single().rows

        assertEquals(XlsxRow(emptyList()), rows[2])
        assertEquals(XlsxRow(emptyList()), rows[9])
        assertEquals(XlsxRow(emptyList()), rows[12])
    }
}
