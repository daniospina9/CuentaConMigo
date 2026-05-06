package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.AssetOperationEntity
import com.example.cuentaconmigo.domain.model.AssetOperation
import com.example.cuentaconmigo.domain.model.AssetOperationType
import java.time.LocalDate

fun String.toAssetOperationType(): AssetOperationType = when (this) {
    "INVEST"              -> AssetOperationType.INVEST
    "ASSET_INCOME"        -> AssetOperationType.ASSET_INCOME
    "LIABILITY_PAYMENT"   -> AssetOperationType.LIABILITY_PAYMENT
    "WITHDRAWAL"          -> AssetOperationType.WITHDRAWAL
    else                  -> AssetOperationType.INVEST
}

fun AssetOperationType.toDbString(): String = when (this) {
    AssetOperationType.INVEST             -> "INVEST"
    AssetOperationType.ASSET_INCOME       -> "ASSET_INCOME"
    AssetOperationType.LIABILITY_PAYMENT  -> "LIABILITY_PAYMENT"
    AssetOperationType.WITHDRAWAL         -> "WITHDRAWAL"
}

// Entity ↔ Domain
fun AssetOperationEntity.toDomain() = AssetOperation(
    id = id,
    userId = userId,
    subAccountId = subAccountId,
    type = type.toAssetOperationType(),
    date = LocalDate.ofEpochDay(date),
    balanceEffect = balanceEffect,
    assetValueDelta = assetValueDelta,
    description = description,
    liabilityId = liabilityId,
    withdrawalGroupId = withdrawalGroupId
)

fun AssetOperation.toEntity() = AssetOperationEntity(
    id = id,
    userId = userId,
    subAccountId = subAccountId,
    type = type.toDbString(),
    date = date.toEpochDay(),
    balanceEffect = balanceEffect,
    assetValueDelta = assetValueDelta,
    description = description,
    liabilityId = liabilityId,
    withdrawalGroupId = withdrawalGroupId
)
