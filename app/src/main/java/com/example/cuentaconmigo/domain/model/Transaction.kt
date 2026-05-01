package com.example.cuentaconmigo.domain.model

import java.time.LocalDate

data class Transaction(
    val id: Long,
    val userId: Long,
    val depositAccountId: Long,
    val destinationAccountId: Long?,
    val type: TransactionType,
    val amount: Long,
    val date: LocalDate,
    val description: String?,
    val transferGroupId: String? = null
)