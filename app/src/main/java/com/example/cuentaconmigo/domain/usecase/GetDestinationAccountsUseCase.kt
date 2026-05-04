package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDestinationAccountsUseCase @Inject constructor(
    private val repository: DestinationAccountRepository
) {
    operator fun invoke(userId: Long): Flow<List<DestinationAccount>> =
        repository.getByUser(userId)
}