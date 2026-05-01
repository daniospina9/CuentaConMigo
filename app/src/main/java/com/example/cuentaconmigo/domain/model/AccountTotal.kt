package com.example.cuentaconmigo.domain.model

data class AccountTotal(
    val destinationAccountId: Long,
    val destinationAccountName: String,
    val total: Long
)