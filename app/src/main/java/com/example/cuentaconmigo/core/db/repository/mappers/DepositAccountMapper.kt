package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.DepositAccountEntity
import com.example.cuentaconmigo.domain.model.DepositAccount

// Entity ↔ Domain
fun DepositAccountEntity.toDomain() =
    DepositAccount(id = id, userId = userId, name = name)

fun DepositAccount.toEntity() =
    DepositAccountEntity(id = id, userId = userId, name = name)