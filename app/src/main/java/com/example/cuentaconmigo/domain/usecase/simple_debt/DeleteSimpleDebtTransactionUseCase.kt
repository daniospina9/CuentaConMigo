package com.example.cuentaconmigo.domain.usecase.simple_debt

import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteSimpleDebtTransactionUseCase @Inject constructor(
    private val debtRepository: SimpleDebtRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(tx: SimpleDebtTransaction) {
        if (tx.linkedTransactionId != null) {
            val linked = transactionRepository.getById(tx.linkedTransactionId)
            if (linked != null) transactionRepository.delete(linked)
        }
        debtRepository.deleteTransaction(tx)
    }
}
