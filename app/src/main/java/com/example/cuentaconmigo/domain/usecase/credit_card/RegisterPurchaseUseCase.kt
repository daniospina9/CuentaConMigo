package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import javax.inject.Inject

class RegisterPurchaseUseCase @Inject constructor(
    private val repository: CreditCardRepository
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
    ): Long = repository.insertTransaction(
        CreditCardTransaction(
            id = 0,
            creditCardId = creditCardId,
            userId = userId,
            type = type,
            amount = amount,
            description = description,
            date = date,
            destinationAccountId = destinationAccountId,
            linkedTransactionId = null,
            installments = installments
        )
    )
}
