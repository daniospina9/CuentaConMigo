package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransferUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transferGroupId: String) =
        repository.deleteTransfer(transferGroupId)
}