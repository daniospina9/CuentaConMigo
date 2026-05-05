package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(userId: Long): Flow<List<Transaction>> =
        repository.getByUser(userId)
}