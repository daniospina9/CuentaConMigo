package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardExtract
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteExtractUseCase @Inject constructor(
    private val repository: CreditCardRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(extract: CreditCardExtract) {
        deleteLinkedExpenses(extract.id)
        repository.deleteTransactionsByExtractId(extract.id)
        repository.deleteExtract(extract)
    }

    internal suspend fun deleteLinkedExpenses(extractId: Long) {
        repository.getTransactionsByExtractId(extractId).forEach { tx ->
            tx.linkedTransactionId?.let { linkedId ->
                transactionRepository.getById(linkedId)?.let { transactionRepository.delete(it) }
            }
        }
    }
}
