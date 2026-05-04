package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import javax.inject.Inject

class DeleteDestinationAccountUseCase @Inject constructor(
    private val repository: DestinationAccountRepository
) {
    suspend operator fun invoke(account: DestinationAccount): Result<Unit> =
        repository.delete(account)
}