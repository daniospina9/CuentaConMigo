package com.example.cuentaconmigo.domain.model

data class SimpleDebt(
    val id: Long,
    val userId: Long,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val isActive: Boolean
)

data class SimpleDebtTransaction(
    val id: Long,
    val debtId: Long,
    val userId: Long,
    val type: SimpleDebtTransactionType,
    val amount: Long,
    val description: String?,
    val date: Long,
    val depositAccountId: Long?,
    val linkedTransactionId: Long?
)

enum class SimpleDebtTransactionType { LOAN_RECEIVED, PAYMENT, INTEREST }