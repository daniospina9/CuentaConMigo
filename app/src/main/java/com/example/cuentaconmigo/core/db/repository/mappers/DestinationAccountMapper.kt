package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.DestinationAccountEntity
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.InvestmentSubtype

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

// InvestmentSubtype
fun String?.toInvestmentSubtype(): InvestmentSubtype? = when (this) {
    "asset"   -> InvestmentSubtype.ASSET
    "liquid"  -> InvestmentSubtype.LIQUID
    "expense" -> InvestmentSubtype.EXPENSE
    else      -> null
}

fun InvestmentSubtype?.toSubtypeDbString(): String? = when (this) {
    InvestmentSubtype.ASSET   -> "asset"
    InvestmentSubtype.LIQUID  -> "liquid"
    InvestmentSubtype.EXPENSE -> "expense"
    null                      -> null
}

// Entity ↔ Domain
fun DestinationAccountEntity.toDomain() = DestinationAccount(
    id = id,
    userId = userId,
    name = name,
    type = type.toAccountType(),
    isDefault = isDefault,
    investmentSubtype = investmentSubtype.toInvestmentSubtype(),
    parentAccountId = parentAccountId
)

fun DestinationAccount.toEntity() = DestinationAccountEntity(
    id = id,
    userId = userId,
    name = name,
    type = type.toDbString(),
    isDefault = isDefault,
    investmentSubtype = investmentSubtype.toSubtypeDbString(),
    parentAccountId = parentAccountId
)