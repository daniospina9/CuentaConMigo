package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.SavingsMovementDao
import com.example.cuentaconmigo.core.db.repository.mappers.toDomain
import com.example.cuentaconmigo.core.db.repository.mappers.toEntity
import com.example.cuentaconmigo.domain.model.SavingsMovement
import com.example.cuentaconmigo.domain.repository.SavingsMovementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsMovementRepositoryImpl @Inject constructor(
    private val dao: SavingsMovementDao
) : SavingsMovementRepository {

    override fun getBySubAccount(subAccountId: Long): Flow<List<SavingsMovement>> =
        dao.getBySubAccount(subAccountId).map { list -> list.map { it.toDomain() } }

    override fun getBalance(subAccountId: Long): Flow<Long> =
        dao.getBalance(subAccountId)

    override suspend fun insert(movement: SavingsMovement): Long =
        dao.insert(movement.toEntity())

    override suspend fun delete(movement: SavingsMovement) =
        dao.delete(movement.toEntity())

    override suspend fun deleteByGroupId(groupId: String) =
        dao.deleteByGroupId(groupId)
}