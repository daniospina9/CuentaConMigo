package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.SavingsMovementEntity
import com.example.cuentaconmigo.domain.model.SavingsMovement
import java.time.LocalDate

fun SavingsMovementEntity.toDomain() = SavingsMovement(
    id = id,
    userId = userId,
    subAccountId = subAccountId,
    amount = amount,
    date = LocalDate.ofEpochDay(date),
    description = description,
    groupId = groupId
)

fun SavingsMovement.toEntity() = SavingsMovementEntity(
    id = id,
    userId = userId,
    subAccountId = subAccountId,
    amount = amount,
    date = date.toEpochDay(),
    description = description,
    groupId = groupId
)