package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class ForceDeleteOrphanedAccountUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val destinationAccountRepository: DestinationAccountRepository
) {
    suspend operator fun invoke(accountId: Long): Result<Unit> = runCatching {
        transactionRepository.deleteAllByAccountOrParentId(accountId)
        destinationAccountRepository.forceDeleteWithChildren(accountId).getOrThrow()
    }
}