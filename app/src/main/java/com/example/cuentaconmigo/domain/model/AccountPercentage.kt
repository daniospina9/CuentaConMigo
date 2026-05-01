package com.example.cuentaconmigo.domain.model

data class AccountPercentage(
    val destinationAccountId: Long,
    val destinationAccountName: String,
    val total: Long,
    val percentage: Int
)