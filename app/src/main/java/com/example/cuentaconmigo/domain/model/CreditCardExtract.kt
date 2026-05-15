package com.example.cuentaconmigo.domain.model

data class CreditCardExtract(
    val id: Long = 0,
    val creditCardId: Long,
    val billingAmount: Long,
    val currentInterest: Long,
    val lateInterest: Long,
    val otherCharges: Long,
    val paymentsAndCredits: Long,
    val totalBankBalance: Long,
    val minimumPayment: Long,
    val uncollectedInterest: Long,
    val isReconciled: Boolean = false,
    val registeredAt: Long = System.currentTimeMillis()
)

data class ExtractReconciliation(
    val extract: CreditCardExtract,
    val appDebt: Long,
    val diff: Long,           // totalBankBalance - appDebt (positivo = app le falta registrar algo)
    val hasDifference: Boolean
)