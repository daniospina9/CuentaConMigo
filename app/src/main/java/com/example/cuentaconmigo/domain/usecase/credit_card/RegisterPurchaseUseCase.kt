package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class RegisterPurchaseUseCase @Inject constructor(
    private val repository: CreditCardRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        creditCardId: Long,
        userId: Long,
        amount: Long,
        description: String?,
        destinationAccountId: Long?,
        date: Long,
        type: CreditCardTransactionType = CreditCardTransactionType.PURCHASE,
        installments: Int = 1
    ): Long {
        // For PURCHASE with a destinationAccountId: create a linked Transaction(EXPENSE)
        val linkedTransactionId: Long? = if (
            type == CreditCardTransactionType.PURCHASE && destinationAccountId != null
        ) {
            val localDate = Instant.ofEpochMilli(date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            transactionRepository.insert(
                Transaction(
                    id = 0,
                    userId = userId,
                    depositAccountId = null,
                    destinationAccountId = destinationAccountId,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    date = localDate,
                    description = description
                )
            )
        } else {
            null
        }

        return repository.insertTransaction(
            CreditCardTransaction(
                id = 0,
                creditCardId = creditCardId,
                userId = userId,
                type = type,
                amount = amount,
                description = description,
                date = date,
                destinationAccountId = destinationAccountId,
                linkedTransactionId = linkedTransactionId,
                installments = installments
            )
        )
    }
}
