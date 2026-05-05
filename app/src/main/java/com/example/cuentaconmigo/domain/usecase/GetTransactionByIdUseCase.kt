package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionByIdUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Transaction? =
        repository.getById(id)
}