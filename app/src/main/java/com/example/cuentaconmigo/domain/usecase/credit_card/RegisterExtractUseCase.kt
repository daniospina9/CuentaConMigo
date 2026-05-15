package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardExtract
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import javax.inject.Inject

class RegisterExtractUseCase @Inject constructor(
    private val repository: CreditCardRepository
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
        fun tx(amount: Long, type: CreditCardTransactionType, description: String) =
            CreditCardTransaction(
                id = 0,
                creditCardId = extract.creditCardId,
                userId = userId,
                type = type,
                amount = amount,
                description = description,
                date = extract.cutOffDate,
                destinationAccountId = null,
                linkedTransactionId = null,
                installments = 1,
                extractId = extractId
            )

        if (extract.currentInterest > 0)
            repository.insertTransaction(tx(extract.currentInterest, CreditCardTransactionType.INTEREST, "Interés corriente (extracto)"))
        if (extract.lateInterest > 0)
            repository.insertTransaction(tx(extract.lateInterest, CreditCardTransactionType.INTEREST, "Interés de mora (extracto)"))
        if (extract.otherCharges > 0)
            repository.insertTransaction(tx(extract.otherCharges, CreditCardTransactionType.FEE, "Otros cargos (extracto)"))
    }
}