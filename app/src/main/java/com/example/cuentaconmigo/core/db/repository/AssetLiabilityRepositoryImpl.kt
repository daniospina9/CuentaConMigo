package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.AssetLiabilityDao
import com.example.cuentaconmigo.core.db.repository.mappers.toDomain
import com.example.cuentaconmigo.core.db.repository.mappers.toEntity
import com.example.cuentaconmigo.domain.model.AssetLiability
import com.example.cuentaconmigo.domain.repository.AssetLiabilityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetLiabilityRepositoryImpl @Inject constructor(
    private val dao: AssetLiabilityDao
) : AssetLiabilityRepository {

    override fun getBySubAccount(subAccountId: Long): Flow<List<AssetLiability>> =
        dao.getBySubAccount(subAccountId).map { list -> list.map { it.toDomain() } }

    override fun getPendingBySubAccount(subAccountId: Long): Flow<List<AssetLiability>> =
        dao.getPendingBySubAccount(subAccountId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(liability: AssetLiability): Long =
        dao.insert(liability.toEntity())

    override suspend fun update(liability: AssetLiability) =
        dao.update(liability.toEntity())

    override suspend fun delete(liability: AssetLiability) =
        dao.delete(liability.toEntity())
}