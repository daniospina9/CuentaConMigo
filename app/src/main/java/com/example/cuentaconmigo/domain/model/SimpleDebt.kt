package com.example.cuentaconmigo.domain.model

/**
 * Discrimina el tipo de cuenta dentro del módulo:
 * - RECEIVED: "Préstamo recibido" (dinero que me prestan a mí, es una deuda).
 * - GIVEN: "Préstamo otorgado" (dinero que yo presto, es una cuenta por cobrar).
 */
enum class SimpleDebtKind { RECEIVED, GIVEN }

data class SimpleDebt(
    val id: Long,
    val userId: Long,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val isActive: Boolean,
    val kind: SimpleDebtKind = SimpleDebtKind.RECEIVED
)

data class SimpleDebtTransaction(
    val id: Long,
    val debtId: Long,
    val userId: Long,
    val type: SimpleDebtTransactionType,
    val amount: Long,
    val description: String?,
    val date: Long,
    val depositAccountId: Long?,
    val linkedTransactionId: Long?
)

enum class SimpleDebtTransactionType {
    // Préstamo recibido
    LOAN_RECEIVED, PAYMENT,
    // Préstamo otorgado
    LOAN_GIVEN, COLLECTION,
    // Compartido (sube el saldo en ambos)
    INTEREST
}
