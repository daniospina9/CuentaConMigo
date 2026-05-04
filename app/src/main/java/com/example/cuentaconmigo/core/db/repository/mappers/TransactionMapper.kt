package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.TransactionEntity
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import java.time.LocalDate

// TransactionType
fun String.toTransactionType(): TransactionType = when (this) {
    "INCOME"  -> TransactionType.INCOME
    "EXPENSE" -> TransactionType.EXPENSE
    else      -> TransactionType.INCOME
}

fun TransactionType.toDbString(): String = when (this) {
    TransactionType.INCOME   -> "INCOME"
    TransactionType.EXPENSE  -> "EXPENSE"
    TransactionType.TRANSFER -> "INCOME" // transferencias se guardan como INCOME/EXPENSE en pares
}

// LocalDate
fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)
fun LocalDate.toEpochDayLong(): Long = this.toEpochDay()

// Entity ↔ Domain
fun TransactionEntity.toDomain() = Transaction(
    id = id,
    userId = userId,
    depositAccountId = depositAccountId,
    destinationAccountId = destinationAccountId,
    type = type.toTransactionType(),
    amount = amount,
    date = date.toLocalDate(),
    description = description,
    transferGroupId = transferGroupId
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    userId = userId,
    depositAccountId = depositAccountId,
    destinationAccountId = destinationAccountId,
    type = type.toDbString(),
    amount = amount,
    date = date.toEpochDayLong(),
    description = description,
    transferGroupId = transferGroupId
)