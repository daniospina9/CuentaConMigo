package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.TransactionDao
import com.example.cuentaconmigo.core.db.entities.TransactionEntity
import com.example.cuentaconmigo.core.db.repository.mappers.toDomain
import com.example.cuentaconmigo.core.db.repository.mappers.toEntity
import com.example.cuentaconmigo.domain.model.AccountTotal
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
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

    override suspend fun insertTransfer(
        userId: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        date: LocalDate,
        description: String?
    ) {
        val groupId = UUID.randomUUID().toString()
        val epochDay = date.toEpochDay()
        dao.insertAll(
            listOf(
                TransactionEntity(
                    userId = userId,
                    depositAccountId = fromAccountId,
                    type = "EXPENSE",
                    amount = amount,
                    date = epochDay,
                    description = description,
                    transferGroupId = groupId
                ),
                TransactionEntity(
                    userId = userId,
                    depositAccountId = toAccountId,
                    type = "INCOME",
                    amount = amount,
                    date = epochDay,
                    description = description,
                    transferGroupId = groupId
                )
            )
        )
    }

    override suspend fun deleteTransfer(transferGroupId: String) =
        dao.deleteByTransferGroupId(transferGroupId)

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

    override fun getByDestinationAccount(
        destinationAccountId: Long,
        startDay: Long,
        endDay: Long
    ): Flow<List<Transaction>> =
        dao.getByDestinationAccount(destinationAccountId, startDay, endDay)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getOpeningBalance(depositAccountId: Long, beforeDay: LocalDate): Long =
        dao.getOpeningBalance(depositAccountId, beforeDay.toEpochDay())

    override suspend fun getPeriodIncome(depositAccountId: Long, startDay: LocalDate, endDay: LocalDate): Long =
        dao.getPeriodIncome(depositAccountId, startDay.toEpochDay(), endDay.toEpochDay())

    override suspend fun getPeriodExpense(depositAccountId: Long, startDay: LocalDate, endDay: LocalDate): Long =
        dao.getPeriodExpense(depositAccountId, startDay.toEpochDay(), endDay.toEpochDay())

    override suspend fun getNonTransferTransactions(userId: Long, startDay: LocalDate, endDay: LocalDate): List<Transaction> =
        dao.getNonTransferTransactions(userId, startDay.toEpochDay(), endDay.toEpochDay()).map { it.toDomain() }

    override suspend fun update(transaction: Transaction) =
        dao.update(transaction.toEntity())

    override suspend fun getById(id: Long): Transaction? =
        dao.getById(id)?.toDomain()

    override suspend fun getTotalInvestedInAccount(accountId: Long): Long =
        dao.getTotalInvestedInAccount(accountId)

    override fun getByDestinationAccountAll(destinationAccountId: Long): Flow<List<Transaction>> =
        dao.getByDestinationAccountAll(destinationAccountId).map { list -> list.map { it.toDomain() } }

    override fun getByParentInvestmentAccount(parentAccountId: Long, startDay: Long, endDay: Long): Flow<List<Transaction>> =
        dao.getByParentInvestmentAccount(parentAccountId, startDay, endDay).map { list -> list.map { it.toDomain() } }

    override fun getTotalExpensesForAccountFlow(accountId: Long): Flow<Long> =
        dao.getTotalExpensesForAccountFlow(accountId)

    override fun getAllByDepositAccount(depositAccountId: Long): Flow<List<Transaction>> =
        dao.getAllByDepositAccount(depositAccountId).map { list -> list.map { it.toDomain() } }

    override suspend fun hasTransactions(accountId: Long): Boolean =
        dao.countByDestinationAccount(accountId) > 0

    override suspend fun deleteAllByDestinationAccount(accountId: Long) =
        dao.deleteAllByDestinationAccount(accountId)

    override suspend fun deleteAllByAccountOrParentId(accountId: Long) =
        dao.deleteAllByAccountOrParentId(accountId)
}
