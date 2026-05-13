package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.CreditCardEntity
import com.example.cuentaconmigo.core.db.entities.CreditCardTransactionEntity
import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.model.MinPaymentType

fun CreditCardEntity.toDomain() = CreditCard(
    id = id,
    userId = userId,
    name = name,
    lastFourDigits = lastFourDigits,
    creditLimit = creditLimit,
    interestRateAnnual = interestRateAnnual,
    cutOffDay = cutOffDay,
    paymentDueDay = paymentDueDay,
    minPaymentType = MinPaymentType.valueOf(minPaymentType),
    minPaymentPercent = minPaymentPercent,
    minPaymentFixed = minPaymentFixed,
    monthlyFee = monthlyFee,
    isActive = isActive
)

fun CreditCard.toEntity() = CreditCardEntity(
    id = id,
    userId = userId,
    name = name,
    lastFourDigits = lastFourDigits,
    creditLimit = creditLimit,
    interestRateAnnual = interestRateAnnual,
    cutOffDay = cutOffDay,
    paymentDueDay = paymentDueDay,
    minPaymentType = minPaymentType.name,
    minPaymentPercent = minPaymentPercent,
    minPaymentFixed = minPaymentFixed,
    monthlyFee = monthlyFee,
    isActive = isActive
)

fun CreditCardTransactionEntity.toDomain() = CreditCardTransaction(
    id = id,
    creditCardId = creditCardId,
    userId = userId,
    type = CreditCardTransactionType.valueOf(type),
    amount = amount,
    description = description,
    date = date,
    destinationAccountId = destinationAccountId,
    linkedTransactionId = linkedTransactionId,
    installments = installments
)

fun CreditCardTransaction.toEntity() = CreditCardTransactionEntity(
    id = id,
    creditCardId = creditCardId,
    userId = userId,
    type = type.name,
    amount = amount,
    description = description,
    date = date,
    destinationAccountId = destinationAccountId,
    linkedTransactionId = linkedTransactionId,
    installments = installments
)