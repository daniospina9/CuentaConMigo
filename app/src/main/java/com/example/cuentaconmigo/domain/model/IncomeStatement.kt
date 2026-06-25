package com.example.cuentaconmigo.domain.model

/**
 * Consolidated income statement (Estado de resultados) across all deposit
 * accounts for a given period. Aggregates the opening balance, period income
 * and expense, and the resulting closing balance into a single summary.
 */
data class IncomeStatement(
    val openingBalance: Long,
    val periodIncome: Long,
    val periodExpense: Long,
    val closingBalance: Long
)
