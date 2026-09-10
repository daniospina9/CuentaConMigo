package com.example.cuentaconmigo.features.reports.export

import com.example.cuentaconmigo.features.reports.FinancialReportState
import java.time.LocalDate

/**
 * Traduce [FinancialReportState] a [FinancialReportExportData], o `null` cuando el
 * estado todavía no es exportable.
 *
 * [FinancialReportState] carga campos de uso exclusivo de la UI (`isLoading`, `error`,
 * `generated`) y datos nulables porque recién se completan después de generar el
 * informe, mientras que [FinancialReportExportData] no admite ninguna de esas dos
 * cosas: es la entrada de un traductor puro que asume que el informe ya existe. Este
 * mapper es el único lugar donde se tiende ese puente, así ningún llamador necesita
 * un `?:` defensivo repetido en cada punto de uso.
 */
object FinancialReportExportDataMapper {

    fun from(
        state: FinancialReportState,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): FinancialReportExportData? {
        if (!state.generated) return null
        if (state.isLoading) return null
        val incomeStatement = state.incomeStatement ?: return null
        if (startDate == null) return null
        if (endDate == null) return null

        return FinancialReportExportData(
            startDate = startDate,
            endDate = endDate,
            incomeStatement = incomeStatement,
            expenseByCategory = state.expenseByCategory,
            totalExpense = state.totalExpense,
            transactions = state.transactions,
            categoryNamesById = state.categoryNamesById
        )
    }
}
