package com.example.cuentaconmigo.domain.model

data class ParsedTransaction(
    val type: TransactionType? = null,
    val depositAccountName: String? = null,
    val destinationAccountName: String? = null,
    val amount: Long? = null,
    val description: String? = null
)

fun ParsedTransaction.missingFields(): List<String> = buildList {
    if (type == null) add("type")
    if (depositAccountName.isNullOrBlank()) add("depositAccount")
    if (type == TransactionType.EXPENSE && destinationAccountName.isNullOrBlank()) add("destinationAccount")
    if (amount == null || amount <= 0L) add("amount")
}