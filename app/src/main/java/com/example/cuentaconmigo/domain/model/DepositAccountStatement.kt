package com.example.cuentaconmigo.domain.model

data class DepositAccountStatement(
    val accountName: String,
    val openingBalance: Long,
    val periodIncome: Long,
    val periodExpense: Long,
    val closingBalance: Long
)
