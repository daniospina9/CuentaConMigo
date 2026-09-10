package com.example.cuentaconmigo.features.reports.export

import com.example.cuentaconmigo.domain.model.CategoryExpenseShare
import com.example.cuentaconmigo.domain.model.IncomeStatement
import com.example.cuentaconmigo.domain.model.Transaction
import java.time.LocalDate

/**
 * Datos de entrada de la exportación del informe personalizado a Excel.
 *
 * Deliberadamente NO es [com.example.cuentaconmigo.features.reports.FinancialReportState]:
 * ese estado carga campos que solo tienen sentido para la UI (`isLoading`, `error`,
 * `generated`), y no tienen ningún rol en un traductor puro hacia un workbook.
 *
 * [incomeStatement] y las fechas son no nulables a propósito: la exportación solo es
 * alcanzable una vez que el informe ya fue generado, así que modelar esos campos como
 * nulables acá sería mentir sobre una precondición que ya se cumple en el punto de uso.
 */
data class FinancialReportExportData(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val incomeStatement: IncomeStatement,
    val expenseByCategory: List<CategoryExpenseShare>,
    val totalExpense: Long,
    val transactions: List<Transaction>,
    val categoryNamesById: Map<Long, String>
)
