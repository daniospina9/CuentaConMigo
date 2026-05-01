package com.example.cuentaconmigo.domain.model

import java.time.LocalDate

data class InvestmentFluctuation(
    val id: Long,
    val userId: Long,
    val destinationAccountId: Long,
    val amount: Long,
    val date: LocalDate,
    val description: String?
)