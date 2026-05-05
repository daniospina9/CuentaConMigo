package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.AssetOperationDao
import com.example.cuentaconmigo.core.db.repository.mappers.toDomain
import com.example.cuentaconmigo.core.db.repository.mappers.toEntity
import com.example.cuentaconmigo.domain.model.AssetOperation
import com.example.cuentaconmigo.domain.repository.AssetOperationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetOperationRepositoryImpl @Inject constructor(
    private val dao: AssetOperationDao
) : AssetOperationRepository {

    override fun getBySubAccount(subAccountId: Long): Flow<List<AssetOperation>> =
        dao.getBySubAccount(subAccountId).map { list -> list.map { it.toDomain() } }

    override fun getAssetValueDeltaSum(subAccountId: Long): Flow<Long> =
        dao.getAssetValueDeltaSum(subAccountId)

    override fun getBalanceEffectSum(subAccountId: Long): Flow<Long> =
        dao.getBalanceEffectSum(subAccountId)

    override suspend fun insert(op: AssetOperation): Long =
        dao.insert(op.toEntity())

    override suspend fun delete(op: AssetOperation) =
        dao.delete(op.toEntity())

    override suspend fun deleteByWithdrawalGroupId(withdrawalGroupId: String) =
        dao.deleteByWithdrawalGroupId(withdrawalGroupId)
}