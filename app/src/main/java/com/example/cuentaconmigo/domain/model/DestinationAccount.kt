package com.example.cuentaconmigo.domain.model

data class DestinationAccount(
    val id: Long,
    val userId: Long,
    val name: String,
    val type: AccountType,
    val isDefault: Boolean
)