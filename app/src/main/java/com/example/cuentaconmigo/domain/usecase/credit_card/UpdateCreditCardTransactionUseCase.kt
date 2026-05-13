package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateCreditCardTransactionUseCase @Inject constructor(
    private val creditCardRepository: CreditCardRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(tx: CreditCardTransaction) {
        creditCardRepository.updateTransaction(tx)
        if (tx.type == CreditCardTransactionType.PAYMENT && tx.linkedTransactionId != null) {
            val linked = transactionRepository.getById(tx.linkedTransactionId)
            if (linked != null) transactionRepository.update(linked.copy(amount = tx.amount))
        }
    }
}
