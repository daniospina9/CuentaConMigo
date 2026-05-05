package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.AssetLiability
import kotlinx.coroutines.flow.Flow

interface AssetLiabilityRepository {
    fun getBySubAccount(subAccountId: Long): Flow<List<AssetLiability>>
    fun getPendingBySubAccount(subAccountId: Long): Flow<List<AssetLiability>>
    suspend fun insert(liability: AssetLiability): Long
    suspend fun update(liability: AssetLiability)
    suspend fun delete(liability: AssetLiability)
}