package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.DepositAccount
import kotlinx.coroutines.flow.Flow

interface DepositAccountRepository {
    fun getByUser(userId: Long): Flow<List<DepositAccount>>
    suspend fun create(account: DepositAccount): Long
    suspend fun update(account: DepositAccount)
    suspend fun delete(account: DepositAccount): Result<Unit>
    suspend fun getById(id: Long): DepositAccount?
}
