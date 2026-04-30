package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.TransactionDao
import com.example.cuentaconmigo.core.db.entities.TransactionEntity
import com.example.cuentaconmigo.domain.model.AccountTotal
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getByUser(userId: Long): Flow<List<Transaction>> =
        dao.getByUser(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(transaction: Transaction): Long =
        dao.insert(transaction.toEntity())

    override suspend fun delete(transaction: Transaction) =
        dao.delete(transaction.toEntity())

    override fun getDepositAccountBalance(depositAccountId: Long): Flow<Long> =
        dao.getDepositAccountBalance(depositAccountId)

    override fun getExpenseTotalsByDestination(
        userId: Long,
        startDay: Long,
        endDay: Long
    ): Flow<List<AccountTotal>> =
        dao.getExpenseTotalsByDestination(userId, startDay, endDay).map { list ->
            list.map { AccountTotal(it.destinationAccountId, it.destinationAccountName, it.total) }
        }

    override fun getStatementForAccount(
        depositAccountId: Long,
        startDay: Long,
        endDay: Long
    ): Flow<List<Transaction>> =
        dao.getStatementForAccount(depositAccountId, startDay, endDay)
            .map { list -> list.map { it.toDomain() } }
}

private fun TransactionEntity.toDomain() = Transaction(
    id = id,
    userId = userId,
    depositAccountId = depositAccountId,
    destinationAccountId = destinationAccountId,
    type = if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE,
    amount = amount,
    date = LocalDate.ofEpochDay(date),
    description = description
)

private fun Transaction.toEntity() = TransactionEntity(
    id = id,
    userId = userId,
    depositAccountId = depositAccountId,
    destinationAccountId = destinationAccountId,
    type = if (type == TransactionType.INCOME) "INCOME" else "EXPENSE",
    amount = amount,
    date = date.toEpochDay(),
    description = description
)
