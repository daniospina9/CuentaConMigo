package com.example.cuentaconmigo.features.reports.export

import com.example.cuentaconmigo.core.export.XlsxCell
import com.example.cuentaconmigo.core.export.XlsxRow
import com.example.cuentaconmigo.core.export.XlsxSheet
import com.example.cuentaconmigo.core.export.XlsxWorkbook
import com.example.cuentaconmigo.domain.model.CategoryExpenseShare
import com.example.cuentaconmigo.domain.model.IncomeStatement
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType

/**
 * Traduce [FinancialReportExportData] a un [XlsxWorkbook] con una única hoja
 * "Informe" que contiene, en orden, el estado de resultados, los gastos por
 * categoría y el detalle de transacciones del período.
 *
 * Es una función pura: la misma entrada siempre produce la misma salida, sin
 * relojes, sin formateo dependiente de locale ni `String.format` — los tipos
 * de [com.example.cuentaconmigo.core.export.XlsxCell] son quienes cargan el
 * formato final, no este builder.
 */
object FinancialReportWorkbookBuilder {

    private const val SHEET_NAME = "Informe"
    private val EMPTY_ROW = XlsxRow(emptyList())

    fun build(data: FinancialReportExportData): XlsxWorkbook {
        val rows = mutableListOf<XlsxRow>()
        rows += XlsxRow(listOf(XlsxCell.Header("Informe personalizado")))
        rows += XlsxRow(
            listOf(
                XlsxCell.Text("Período"),
                XlsxCell.Date(data.startDate),
                XlsxCell.Date(data.endDate)
            )
        )
        rows += EMPTY_ROW

        rows += incomeStatementRows(data.incomeStatement)
        rows += EMPTY_ROW

        rows += expenseByCategoryRows(data.expenseByCategory, data.totalExpense)
        rows += EMPTY_ROW

        rows += transactionsRows(data.transactions, data.categoryNamesById)

        return XlsxWorkbook(
            sheets = listOf(
                XlsxSheet(name = SHEET_NAME, rows = rows)
            )
        )
    }

    private fun incomeStatementRows(incomeStatement: IncomeStatement): List<XlsxRow> {
        return listOf(
            XlsxRow(listOf(XlsxCell.Header("Estado de resultados"))),
            XlsxRow(listOf(XlsxCell.Header("Concepto"), XlsxCell.Header("Monto"))),
            XlsxRow(
                listOf(
                    XlsxCell.Text("Saldo inicial"),
                    XlsxCell.Currency(incomeStatement.openingBalance)
                )
            ),
            XlsxRow(
                listOf(
                    XlsxCell.Text("+ Ingresos del período"),
                    XlsxCell.Currency(incomeStatement.periodIncome)
                )
            ),
            XlsxRow(
                listOf(
                    XlsxCell.Text("- Gastos del período"),
                    XlsxCell.Currency(incomeStatement.periodExpense)
                )
            ),
            XlsxRow(
                listOf(
                    XlsxCell.Text("Saldo final"),
                    XlsxCell.Currency(incomeStatement.closingBalance)
                )
            )
        )
    }

    private fun expenseByCategoryRows(
        expenseByCategory: List<CategoryExpenseShare>,
        totalExpense: Long
    ): List<XlsxRow> {
        val header = XlsxRow(listOf(XlsxCell.Header("Gastos por categoría")))

        if (expenseByCategory.isEmpty()) {
            return listOf(header, XlsxRow(listOf(XlsxCell.Text("Sin gastos en el período"))))
        }

        val columnHeaders = XlsxRow(
            listOf(
                XlsxCell.Header("Categoría"),
                XlsxCell.Header("Monto"),
                XlsxCell.Header("% del total")
            )
        )
        val categoryRows = expenseByCategory.map { share ->
            XlsxRow(
                listOf(
                    XlsxCell.Text(share.destinationAccountName),
                    XlsxCell.Currency(share.total),
                    XlsxCell.Percent(share.percentage / 100.0)
                )
            )
        }
        val totalRow = XlsxRow(listOf(XlsxCell.Header("Total"), XlsxCell.Currency(totalExpense)))

        return listOf(header, columnHeaders) + categoryRows + totalRow
    }

    private fun transactionsRows(
        transactions: List<Transaction>,
        categoryNamesById: Map<Long, String>
    ): List<XlsxRow> {
        val header = XlsxRow(listOf(XlsxCell.Header("Detalle de transacciones")))

        if (transactions.isEmpty()) {
            return listOf(
                header,
                XlsxRow(listOf(XlsxCell.Text("Sin transacciones en el período seleccionado")))
            )
        }

        val columnHeaders = XlsxRow(
            listOf(
                XlsxCell.Header("Fecha"),
                XlsxCell.Header("Tipo"),
                XlsxCell.Header("Categoría"),
                XlsxCell.Header("Descripción"),
                XlsxCell.Header("Monto")
            )
        )
        val transactionRows = transactions.map { transaction ->
            XlsxRow(
                listOf(
                    XlsxCell.Date(transaction.date),
                    XlsxCell.Text(typeLabel(transaction.type)),
                    categoryCell(transaction, categoryNamesById),
                    descriptionCell(transaction.description),
                    XlsxCell.Currency(transaction.amount)
                )
            )
        }

        return listOf(header, columnHeaders) + transactionRows
    }

    private fun typeLabel(type: TransactionType): String = when (type) {
        TransactionType.INCOME -> "Ingreso"
        TransactionType.EXPENSE -> "Gasto"
        TransactionType.TRANSFER -> "Transferencia"
    }

    private fun categoryCell(
        transaction: Transaction,
        categoryNamesById: Map<Long, String>
    ): XlsxCell {
        if (transaction.type != TransactionType.EXPENSE) return XlsxCell.Empty
        val destinationAccountId = transaction.destinationAccountId ?: return XlsxCell.Empty
        val categoryName = categoryNamesById[destinationAccountId] ?: return XlsxCell.Empty
        return XlsxCell.Text(categoryName)
    }

    private fun descriptionCell(description: String?): XlsxCell {
        if (description.isNullOrBlank()) return XlsxCell.Empty
        return XlsxCell.Text(description)
    }
}
