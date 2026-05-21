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

class RegisterSimpleDebtInterestUseCase @Inject constructor(
    private val debtRepository: SimpleDebtRepository,
    private val transactionRepository: TransactionRepository,
    private val destinationAccountRepository: DestinationAccountRepository
) {
    suspend operator fun invoke(
        debtId: Long,
        userId: Long,
        amount: Long,
        description: String?,
        date: Long
    ) {
        val interestAccount = getOrCreateInterestAccount(userId)
        val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
        val desc = description?.ifBlank { null } ?: "Interés préstamo"

        val expenseId = transactionRepository.insert(
            Transaction(
                id = 0,
                userId = userId,
                depositAccountId = null,
                destinationAccountId = interestAccount.id,
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
                type = SimpleDebtTransactionType.INTEREST,
                amount = amount,
                description = desc,
                date = date,
                depositAccountId = null,
                linkedTransactionId = expenseId
            )
        )
    }

    private suspend fun getOrCreateInterestAccount(userId: Long): DestinationAccount {
        val existing = destinationAccountRepository.getByName(userId, "Pago de Intereses")
        if (existing != null) return existing
        val newId = destinationAccountRepository.create(
            DestinationAccount(0, userId, "Pago de Intereses", AccountType.EXPENSE, isDefault = true)
        )
        return destinationAccountRepository.getById(newId)!!
    }
}