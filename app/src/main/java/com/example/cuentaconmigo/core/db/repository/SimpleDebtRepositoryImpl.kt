package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.SimpleDebtDao
import com.example.cuentaconmigo.core.db.dao.SimpleDebtTransactionDao
import com.example.cuentaconmigo.core.db.repository.mappers.toDomain
import com.example.cuentaconmigo.core.db.repository.mappers.toEntity
import com.example.cuentaconmigo.domain.model.SimpleDebt
import com.example.cuentaconmigo.domain.model.SimpleDebtKind
import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleDebtRepositoryImpl @Inject constructor(
    private val debtDao: SimpleDebtDao,
    private val txDao: SimpleDebtTransactionDao
) : SimpleDebtRepository {

    override fun getActive(userId: Long, kind: SimpleDebtKind): Flow<List<SimpleDebt>> =
        debtDao.getActive(userId, kind.name).map { list -> list.map { it.toDomain() } }

    override fun getById(id: Long): Flow<SimpleDebt?> =
        debtDao.getById(id).map { it?.toDomain() }

    override suspend fun create(debt: SimpleDebt): Long =
        debtDao.insert(debt.toEntity())

    override suspend fun update(debt: SimpleDebt) =
        debtDao.update(debt.toEntity())

    override suspend fun delete(debt: SimpleDebt) =
        debtDao.delete(debt.toEntity())

    override fun getTransactions(debtId: Long): Flow<List<SimpleDebtTransaction>> =
        txDao.getByDebt(debtId).map { list -> list.map { it.toDomain() } }

    override fun getCurrentDebt(debtId: Long): Flow<Long> =
        txDao.getCurrentDebt(debtId)

    override suspend fun insertTransaction(tx: SimpleDebtTransaction): Long =
        txDao.insert(tx.toEntity())

    override suspend fun updateTransaction(tx: SimpleDebtTransaction) =
        txDao.update(tx.toEntity())

    override suspend fun deleteTransaction(tx: SimpleDebtTransaction) =
        txDao.delete(tx.toEntity())

    override suspend fun hasTransactions(debtId: Long): Boolean =
        txDao.countByDebt(debtId) > 0

    override suspend fun getTransactionByLinkedId(linkedTransactionId: Long): SimpleDebtTransaction? =
        txDao.getByLinkedTransactionId(linkedTransactionId)?.toDomain()

    override fun getAllLinkedTransactionIds(): Flow<Set<Long>> =
        txDao.getAllLinkedTransactionIds().map { it.toSet() }

    override fun getLinkedTransactionIdsByKind(kind: SimpleDebtKind): Flow<Set<Long>> =
        txDao.getLinkedTransactionIdsByKind(kind.name).map { it.toSet() }
}