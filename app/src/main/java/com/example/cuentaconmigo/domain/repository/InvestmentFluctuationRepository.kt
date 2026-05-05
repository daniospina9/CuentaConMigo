package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.InvestmentFluctuation
import kotlinx.coroutines.flow.Flow

interface InvestmentFluctuationRepository {
    fun getByAccount(accountId: Long): Flow<List<InvestmentFluctuation>>
    suspend fun insert(fluctuation: InvestmentFluctuation): Long
    suspend fun delete(fluctuation: InvestmentFluctuation)
    fun getBalance(userId: Long, accountId: Long): Flow<Long>
    suspend fun deleteByWithdrawalGroupId(withdrawalGroupId: String)
}
