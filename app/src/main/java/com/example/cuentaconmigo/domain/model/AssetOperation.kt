package com.example.cuentaconmigo.domain.model

import java.time.LocalDate

data class AssetOperation(
    val id: Long,
    val userId: Long,
    val subAccountId: Long,
    val type: AssetOperationType,
    val date: LocalDate,
    val balanceEffect: Long,    // + entra, - sale del balance
    val assetValueDelta: Long,  // cuánto cambia el valor del activo
    val description: String?,
    val liabilityId: Long? = null,
    val withdrawalGroupId: String? = null
)