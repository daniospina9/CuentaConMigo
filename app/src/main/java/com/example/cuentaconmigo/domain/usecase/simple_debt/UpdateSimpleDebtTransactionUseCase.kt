package com.example.cuentaconmigo.domain.usecase.simple_debt

import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateSimpleDebtTransactionUseCase @Inject constructor(
    private val debtRepository: SimpleDebtRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(tx: SimpleDebtTransaction) {
        debtRepository.updateTransaction(tx)
        if (tx.linkedTransactionId != null) {
            val linked = transactionRepository.getById(tx.linkedTransactionId)
            if (linked != null) {
                transactionRepository.update(linked.copy(amount = tx.amount))
            }
        }
    }
}