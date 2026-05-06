package com.example.cuentaconmigo.domain.model

import java.time.LocalDate

data class SavingsMovement(
    val id: Long,
    val userId: Long,
    val subAccountId: Long,
    val amount: Long,
    val date: LocalDate,
    val description: String?,
    val groupId: String? = null
)