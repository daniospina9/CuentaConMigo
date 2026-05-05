package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import javax.inject.Inject

class DeleteDepositAccountUseCase @Inject constructor(
    private val repository: DepositAccountRepository
) {
    suspend operator fun invoke(account: DepositAccount): Result<Unit> =
        repository.delete(account)
}