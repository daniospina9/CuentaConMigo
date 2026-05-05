package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.AssetOperation
import kotlinx.coroutines.flow.Flow

interface AssetOperationRepository {
    fun getBySubAccount(subAccountId: Long): Flow<List<AssetOperation>>
    fun getAssetValueDeltaSum(subAccountId: Long): Flow<Long>
    fun getBalanceEffectSum(subAccountId: Long): Flow<Long>
    suspend fun insert(op: AssetOperation): Long
    suspend fun delete(op: AssetOperation)
    suspend fun deleteByWithdrawalGroupId(withdrawalGroupId: String)
}