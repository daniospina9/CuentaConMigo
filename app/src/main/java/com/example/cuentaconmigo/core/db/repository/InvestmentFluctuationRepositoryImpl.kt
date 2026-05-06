package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.InvestmentFluctuationDao
import com.example.cuentaconmigo.core.db.repository.mappers.toDomain
import com.example.cuentaconmigo.core.db.repository.mappers.toEntity
import com.example.cuentaconmigo.domain.model.InvestmentFluctuation
import com.example.cuentaconmigo.domain.repository.InvestmentFluctuationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestmentFluctuationRepositoryImpl @Inject constructor(
    private val dao: InvestmentFluctuationDao
) : InvestmentFluctuationRepository {

    override fun getByAccount(accountId: Long): Flow<List<InvestmentFluctuation>> =
        dao.getByAccount(accountId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(fluctuation: InvestmentFluctuation): Long =
        dao.insert(fluctuation.toEntity())

    override suspend fun delete(fluctuation: InvestmentFluctuation) =
        dao.delete(fluctuation.toEntity())

    override fun getBalance(userId: Long, accountId: Long): Flow<Long> =
        dao.getBalance(userId, accountId)

    override suspend fun deleteByWithdrawalGroupId(withdrawalGroupId: String) =
        dao.deleteByWithdrawalGroupId(withdrawalGroupId)
}
