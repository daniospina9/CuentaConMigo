package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.CreditCardExtract
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

class RegisterExtractUseCase @Inject constructor(
    private val repository: CreditCardRepository,
    private val transactionRepository: TransactionRepository,
    private val destinationAccountRepository: DestinationAccountRepository
) {
    suspend operator fun invoke(extract: CreditCardExtract, userId: Long) {
        val extractId = repository.insertExtract(extract)
        createLinkedTransactions(extract, userId, extractId)
    }

    internal suspend fun createLinkedTransactions(
        extract: CreditCardExtract,
        userId: Long,
        extractId: Long
    ) {
        val interestAccount = getOrCreateInterestAccount(userId)
        val localDate = Instant.ofEpochMilli(extract.cutOffDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        suspend fun insertWithExpense(amount: Long, type: CreditCardTransactionType, description: String) {
            val expenseId = transactionRepository.insert(
                Transaction(
                    id = 0,
                    userId = userId,
                    depositAccountId = null,
                    destinationAccountId = interestAccount.id,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    date = localDate,
                    description = description
                )
            )
            repository.insertTransaction(
                CreditCardTransaction(
                    id = 0,
                    creditCardId = extract.creditCardId,
                    userId = userId,
                    type = type,
                    amount = amount,
                    description = description,
                    date = extract.cutOffDate,
                    destinationAccountId = interestAccount.id,
                    linkedTransactionId = expenseId,
                    installments = 1,
                    extractId = extractId
                )
            )
        }

        if (extract.currentInterest > 0)
            insertWithExpense(extract.currentInterest, CreditCardTransactionType.INTEREST, "Interés corriente (extracto)")
        if (extract.lateInterest > 0)
            insertWithExpense(extract.lateInterest, CreditCardTransactionType.INTEREST, "Interés de mora (extracto)")
        if (extract.otherCharges > 0)
            insertWithExpense(extract.otherCharges, CreditCardTransactionType.FEE, "Otros cargos (extracto)")
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