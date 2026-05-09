package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.DestinationAccount
import kotlinx.coroutines.flow.Flow

interface DestinationAccountRepository {
    fun getByUser(userId: Long): Flow<List<DestinationAccount>>
    suspend fun create(account: DestinationAccount): Long
    suspend fun insertAll(accounts: List<DestinationAccount>)
    suspend fun update(account: DestinationAccount)
    suspend fun delete(account: DestinationAccount): Result<Unit>
    suspend fun getById(id: Long): DestinationAccount?
    suspend fun getInvestmentAccount(userId: Long): DestinationAccount?
    fun getInvestmentAccounts(userId: Long): Flow<List<DestinationAccount>>
    fun getSavingsAccounts(userId: Long): Flow<List<DestinationAccount>>
    fun getSubAccounts(parentAccountId: Long): Flow<List<DestinationAccount>>
    suspend fun hasSubAccounts(accountId: Long): Boolean
    suspend fun forceDelete(accountId: Long): Result<Unit>
    suspend fun forceDeleteWithChildren(accountId: Long): Result<Unit>
}