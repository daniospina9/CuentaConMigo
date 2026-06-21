package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.SimpleDebtEntity
import com.example.cuentaconmigo.core.db.entities.SimpleDebtTransactionEntity
import com.example.cuentaconmigo.domain.model.SimpleDebt
import com.example.cuentaconmigo.domain.model.SimpleDebtKind
import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.model.SimpleDebtTransactionType

fun SimpleDebtEntity.toDomain() = SimpleDebt(
    id = id,
    userId = userId,
    name = name,
    description = description,
    createdAt = createdAt,
    isActive = isActive,
    kind = runCatching { SimpleDebtKind.valueOf(kind) }.getOrDefault(SimpleDebtKind.RECEIVED)
)

fun SimpleDebt.toEntity() = SimpleDebtEntity(
    id = id,
    userId = userId,
    name = name,
    description = description,
    createdAt = createdAt,
    isActive = isActive,
    kind = kind.name
)

fun SimpleDebtTransactionEntity.toDomain() = SimpleDebtTransaction(
    id = id,
    debtId = debtId,
    userId = userId,
    type = SimpleDebtTransactionType.valueOf(type),
    amount = amount,
    description = description,
    date = date,
    depositAccountId = depositAccountId,
    linkedTransactionId = linkedTransactionId
)

fun SimpleDebtTransaction.toEntity() = SimpleDebtTransactionEntity(
    id = id,
    debtId = debtId,
    userId = userId,
    type = type.name,
    amount = amount,
    description = description,
    date = date,
    depositAccountId = depositAccountId,
    linkedTransactionId = linkedTransactionId
)