package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteCreditCardTransactionUseCase @Inject constructor(
    private val creditCardRepository: CreditCardRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(tx: CreditCardTransaction) {
        if (tx.type == CreditCardTransactionType.PAYMENT && tx.linkedTransactionId != null) {
            val linked = transactionRepository.getById(tx.linkedTransactionId)
            if (linked != null) transactionRepository.delete(linked)
        }
        creditCardRepository.deleteTransaction(tx)
    }
}
