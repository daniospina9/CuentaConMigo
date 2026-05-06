package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.InvestmentFluctuationEntity
import com.example.cuentaconmigo.domain.model.InvestmentFluctuation
import java.time.LocalDate

// Entity ↔ Domain
fun InvestmentFluctuationEntity.toDomain() = InvestmentFluctuation(
    id = id,
    userId = userId,
    destinationAccountId = destinationAccountId,
    amount = amount,
    date = LocalDate.ofEpochDay(date),
    description = description,
    withdrawalGroupId = withdrawalGroupId
)

fun InvestmentFluctuation.toEntity() = InvestmentFluctuationEntity(
    id = id,
    userId = userId,
    destinationAccountId = destinationAccountId,
    amount = amount,
    date = date.toEpochDay(),
    description = description,
    withdrawalGroupId = withdrawalGroupId
)