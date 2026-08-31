package com.example.cuentaconmigo.domain.model

import java.time.LocalDate

/**
 * Motivo del movimiento. El signo de [SavingsMovement.amount] indica la direccion
 * del dinero, no la intencion: un retiro y un gasto son ambos negativos.
 *
 * - WITHDRAWAL: sale de la subcuenta hacia una cuenta de deposito (lleva groupId).
 * - EXPENSE: gasto pagado directamente desde el ahorro, sin cuenta de deposito.
 * - YIELD: rendimiento abonado por el producto (ej. intereses de un CDT).
 *   Engorda el saldo sin ser un ingreso del informe financiero, igual que las
 *   fluctuaciones de inversion.
 */
enum class SavingsMovementType { WITHDRAWAL, EXPENSE, YIELD }

data class SavingsMovement(
    val id: Long,
    val userId: Long,
    val subAccountId: Long,
    val amount: Long,
    val date: LocalDate,
    val description: String?,
    val groupId: String? = null,
    val type: SavingsMovementType = SavingsMovementType.EXPENSE
)
