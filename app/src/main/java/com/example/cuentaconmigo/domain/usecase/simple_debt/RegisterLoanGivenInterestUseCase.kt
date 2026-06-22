package com.example.cuentaconmigo.domain.usecase.simple_debt

import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.model.SimpleDebtTransactionType
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import javax.inject.Inject

/**
 * Registra un interés sobre un préstamo otorgado.
 * NO mueve dinero real ni genera Transaction en reportes (el interés aún no se ha
 * cobrado); solo incrementa el saldo que me deben y queda en el historial del préstamo.
 * El ingreso real se materializa cuando se registra el cobro.
 */
class RegisterLoanGivenInterestUseCase @Inject constructor(
    private val debtRepository: SimpleDebtRepository
) {
    suspend operator fun invoke(
        debtId: Long,
        userId: Long,
        amount: Long,
        description: String?,
        date: Long
    ) {
        debtRepository.insertTransaction(
            SimpleDebtTransaction(
                id = 0,
                debtId = debtId,
                userId = userId,
                type = SimpleDebtTransactionType.INTEREST,
                amount = amount,
                description = description?.ifBlank { null } ?: "Interés préstamo otorgado",
                date = date,
                depositAccountId = null,
                linkedTransactionId = null
            )
        )
    }
}
