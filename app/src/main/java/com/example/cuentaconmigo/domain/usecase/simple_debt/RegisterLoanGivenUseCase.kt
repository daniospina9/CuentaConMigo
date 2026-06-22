package com.example.cuentaconmigo.domain.usecase.simple_debt

import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.model.SimpleDebtTransactionType
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Registra el desembolso de un préstamo otorgado (dinero que yo presto).
 * Sale como GASTO de la cuenta de depósito elegida, categorizado en la cuenta
 * destino "Préstamos Otorgados" (creada automáticamente si no existe, igual que
 * "Pago de Intereses"). Sube el saldo que me deben. Queda vinculado de forma
 * bidireccional con la Transaction para heredar borrado en cascada y bloqueo de edición.
 */
class RegisterLoanGivenUseCase @Inject constructor(
    private val debtRepository: SimpleDebtRepository,
    private val transactionRepository: TransactionRepository,
    private val destinationAccountRepository: DestinationAccountRepository
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
        val loansAccount = getOrCreateLoansGivenAccount(userId)
        val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
        val desc = description?.ifBlank { null } ?: "Préstamo otorgado: $debtName"

        val expenseId = transactionRepository.insert(
            Transaction(
                id = 0,
                userId = userId,
                depositAccountId = depositAccountId,
                destinationAccountId = loansAccount.id,
                type = TransactionType.EXPENSE,
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
                type = SimpleDebtTransactionType.LOAN_GIVEN,
                amount = amount,
                description = desc,
                date = date,
                depositAccountId = depositAccountId,
                linkedTransactionId = expenseId
            )
        )
    }

    private suspend fun getOrCreateLoansGivenAccount(userId: Long): DestinationAccount {
        val existing = destinationAccountRepository.getByName(userId, "Préstamos Otorgados")
        if (existing != null) return existing
        val newId = destinationAccountRepository.create(
            DestinationAccount(0, userId, "Préstamos Otorgados", AccountType.EXPENSE, isDefault = true)
        )
        return destinationAccountRepository.getById(newId)!!
    }
}
