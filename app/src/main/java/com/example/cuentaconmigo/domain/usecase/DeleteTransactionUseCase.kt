package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        val linkedCcTx = creditCardRepository.getTransactionByLinkedId(transaction.id)
        if (linkedCcTx != null) creditCardRepository.deleteTransaction(linkedCcTx)
        repository.delete(transaction)
    }
}