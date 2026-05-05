package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.AssetLiabilityEntity
import com.example.cuentaconmigo.domain.model.AssetLiability
import java.time.LocalDate

// Entity ↔ Domain
fun AssetLiabilityEntity.toDomain() = AssetLiability(
    id = id,
    userId = userId,
    subAccountId = subAccountId,
    description = description,
    amount = amount,
    createdDate = LocalDate.ofEpochDay(createdDate),
    isPaid = isPaid
)

fun AssetLiability.toEntity() = AssetLiabilityEntity(
    id = id,
    userId = userId,
    subAccountId = subAccountId,
    description = description,
    amount = amount,
    createdDate = createdDate.toEpochDay(),
    isPaid = isPaid
)