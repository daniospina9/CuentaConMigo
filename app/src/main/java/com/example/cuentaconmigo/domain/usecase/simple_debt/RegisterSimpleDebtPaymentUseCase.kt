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

class RegisterSimpleDebtPaymentUseCase @Inject constructor(
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

        val expenseId = transactionRepository.insert(
            Transaction(
                id = 0,
                userId = userId,
                depositAccountId = depositAccountId,
                destinationAccountId = null,
                type = TransactionType.EXPENSE,
                amount = amount,
                date = localDate,
                description = description?.ifBlank { null } ?: "Pago préstamo: $debtName",
                transferGroupId = null
            )
        )

        debtRepository.insertTransaction(
            SimpleDebtTransaction(
                id = 0,
                debtId = debtId,
                userId = userId,
                type = SimpleDebtTransactionType.PAYMENT,
                amount = amount,
                description = description?.ifBlank { null } ?: "Pago préstamo: $debtName",
                date = date,
                depositAccountId = depositAccountId,
                linkedTransactionId = expenseId
            )
        )
    }
}