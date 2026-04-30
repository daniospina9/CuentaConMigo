package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.InvestmentFluctuationDao
import com.example.cuentaconmigo.core.db.entities.InvestmentFluctuationEntity
import com.example.cuentaconmigo.domain.model.InvestmentFluctuation
import com.example.cuentaconmigo.domain.repository.InvestmentFluctuationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
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
}

private fun InvestmentFluctuationEntity.toDomain() = InvestmentFluctuation(
    id = id,
    userId = userId,
    destinationAccountId = destinationAccountId,
    amount = amount,
    date = LocalDate.ofEpochDay(date),
    description = description
)

private fun InvestmentFluctuation.toEntity() = InvestmentFluctuationEntity(
    id = id,
    userId = userId,
    destinationAccountId = destinationAccountId,
    amount = amount,
    date = date.toEpochDay(),
    description = description
)
