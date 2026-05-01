package com.example.cuentaconmigo.domain.model

data class ReportState(
    val expenseTotals: List<AccountTotal> = emptyList(),
    val expensePercentages: List<AccountPercentage> = emptyList(),
    val isLoading: Boolean = false
)