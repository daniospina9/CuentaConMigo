package com.example.cuentaconmigo.domain.model

data class CreditCardTransaction(
    val id: Long,
    val creditCardId: Long,
    val userId: Long,
    val type: CreditCardTransactionType,
    val amount: Long,
    val description: String?,
    val date: Long,
    val destinationAccountId: Long?,
    val linkedTransactionId: Long?,
    val installments: Int = 1
)

enum class CreditCardTransactionType { PURCHASE, PAYMENT, INTEREST, FEE }