package com.example.cuentaconmigo.domain.model

import java.time.LocalDate

data class AssetLiability(
    val id: Long,
    val userId: Long,
    val subAccountId: Long,
    val description: String,
    val amount: Long,
    val createdDate: LocalDate,
    val isPaid: Boolean = false
)