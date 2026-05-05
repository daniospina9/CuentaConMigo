package com.example.cuentaconmigo.domain.model

data class DestinationAccount(
    val id: Long,
    val userId: Long,
    val name: String,
    val type: AccountType,
    val isDefault: Boolean,
    val investmentSubtype: InvestmentSubtype? = null,
    val parentAccountId: Long? = null,
    val assetInitialValue: Long = 0
)