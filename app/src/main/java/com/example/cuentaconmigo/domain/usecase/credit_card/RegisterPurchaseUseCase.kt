package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class RegisterPurchaseUseCase @Inject constructor(
    private val repository: CreditCardRepository,
    private val transactionRepository: TransactionRepository,
    private val destinationAccountRepository: DestinationAccountRepository
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
        val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()

        val (linkedTransactionId, effectiveDestinationId) = when {
            type == CreditCardTransactionType.PURCHASE && destinationAccountId != null -> {
                val txId = transactionRepository.insert(
                    Transaction(
                        id = 0, userId = userId,
                        depositAccountId = null,
                        destinationAccountId = destinationAccountId,
                        type = TransactionType.EXPENSE,
                        amount = amount, date = localDate, description = description
                    )
                )
                Pair(txId, destinationAccountId)
            }
            type == CreditCardTransactionType.INTEREST || type == CreditCardTransactionType.FEE -> {
                val interestAccount = getOrCreateInterestAccount(userId)
                val txId = transactionRepository.insert(
                    Transaction(
                        id = 0, userId = userId,
                        depositAccountId = null,
                        destinationAccountId = interestAccount.id,
                        type = TransactionType.EXPENSE,
                        amount = amount, date = localDate, description = description
                    )
                )
                Pair(txId, interestAccount.id)
            }
            else -> Pair(null, destinationAccountId)
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
                destinationAccountId = effectiveDestinationId,
                linkedTransactionId = linkedTransactionId,
                installments = installments
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
