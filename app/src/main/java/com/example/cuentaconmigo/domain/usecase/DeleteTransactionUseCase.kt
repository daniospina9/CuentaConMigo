package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) =
        repository.delete(transaction)
}