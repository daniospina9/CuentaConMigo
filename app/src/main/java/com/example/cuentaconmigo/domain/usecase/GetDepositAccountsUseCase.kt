package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDepositAccountsUseCase @Inject constructor(
    private val repository: DepositAccountRepository
) {
    operator fun invoke(userId: Long): Flow<List<DepositAccount>> =
        repository.getByUser(userId)
}