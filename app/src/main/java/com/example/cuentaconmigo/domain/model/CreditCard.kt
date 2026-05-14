package com.example.cuentaconmigo.domain.model

data class CreditCard(
    val id: Long,
    val userId: Long,
    val name: String,
    val lastFourDigits: String?,
    val creditLimit: Long,
    val interestRateAnnual: Double,
    val cutOffDay: Int,
    val paymentDueDay: Int,
    val minPaymentType: MinPaymentType,
    val minPaymentPercent: Double,
    val minPaymentFixed: Long,
    val monthlyFee: Long,
    val isActive: Boolean
)

enum class MinPaymentType { PERCENTAGE, FIXED }
