package com.example.cuentaconmigo.domain.model

data class ParsedTransaction(
    val type: TransactionType? = null,
    val depositAccountName: String? = null,
    val toDepositAccountName: String? = null,   // solo para TRANSFER
    val destinationAccountName: String? = null, // solo para EXPENSE
    val amount: Long? = null,
    val description: String? = null
)

fun ParsedTransaction.missingFields(): List<String> = buildList {
    if (type == null) add("tipo")
    if (depositAccountName.isNullOrBlank()) add("cuenta origen")
    when (type) {
        TransactionType.EXPENSE  -> if (destinationAccountName.isNullOrBlank()) add("categoría de gasto")
        TransactionType.TRANSFER -> if (toDepositAccountName.isNullOrBlank()) add("cuenta destino")
        else -> Unit
    }
    if (amount == null || amount <= 0L) add("monto")
}