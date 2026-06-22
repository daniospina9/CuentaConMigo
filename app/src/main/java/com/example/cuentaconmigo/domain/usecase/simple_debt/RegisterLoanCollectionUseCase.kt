package com.example.cuentaconmigo.domain.usecase.simple_debt

import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.model.SimpleDebtTransactionType
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Registra el cobro/devolución de un préstamo otorgado.
 * Entra como INGRESO a la cuenta de depósito elegida y reduce el saldo que me deben.
 * Vínculo bidireccional con la Transaction (borrado en cascada + bloqueo de edición).
 */
class RegisterLoanCollectionUseCase @Inject constructor(
    private val debtRepository: SimpleDebtRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        debtId: Long,
        debtName: String,
        userId: Long,
        amount: Long,
        depositAccountId: Long,
        description: String?,
        date: Long
    ) {
        val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
        val desc = description?.ifBlank { null } ?: "Cobro préstamo: $debtName"

        val incomeId = transactionRepository.insert(
            Transaction(
                id = 0,
                userId = userId,
                depositAccountId = depositAccountId,
                destinationAccountId = null,
                type = TransactionType.INCOME,
                amount = amount,
                date = localDate,
                description = desc,
                transferGroupId = null
            )
        )

        debtRepository.insertTransaction(
            SimpleDebtTransaction(
                id = 0,
                debtId = debtId,
                userId = userId,
                type = SimpleDebtTransactionType.COLLECTION,
                amount = amount,
                description = desc,
                date = date,
                depositAccountId = depositAccountId,
                linkedTransactionId = incomeId
            )
        )
    }
}
