package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.DestinationAccountEntity
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount

// AccountType
fun String.toAccountType(): AccountType = when (this) {
    "expense"    -> AccountType.EXPENSE
    "savings"    -> AccountType.SAVINGS
    "investment" -> AccountType.INVESTMENT
    else         -> AccountType.EXPENSE
}

fun AccountType.toDbString(): String = when (this) {
    AccountType.EXPENSE    -> "expense"
    AccountType.SAVINGS    -> "savings"
    AccountType.INVESTMENT -> "investment"
}

// Entity ↔ Domain
fun DestinationAccountEntity.toDomain() = DestinationAccount(
    id = id,
    userId = userId,
    name = name,
    type = type.toAccountType(),
    isDefault = isDefault
)

fun DestinationAccount.toEntity() = DestinationAccountEntity(
    id = id,
    userId = userId,
    name = name,
    type = type.toDbString(),
    isDefault = isDefault
)