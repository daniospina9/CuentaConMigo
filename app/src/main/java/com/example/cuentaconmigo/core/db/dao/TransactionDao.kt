package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cuentaconmigo.core.db.entities.TransactionEntity
import com.example.cuentaconmigo.domain.model.AccountTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE transferGroupId = :transferGroupId")
    suspend fun deleteByTransferGroupId(transferGroupId: String)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getByUser(userId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME'  THEN amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0)
        FROM transactions
        WHERE depositAccountId = :depositAccountId
    """)
    fun getDepositAccountBalance(depositAccountId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(da.parentAccountId, da.id) AS destinationAccountId,
               COALESCE(parent.name, da.name)      AS destinationAccountName,
               COALESCE(SUM(t.amount), 0)          AS total
        FROM destination_accounts da
        LEFT JOIN destination_accounts parent ON parent.id = da.parentAccountId
        LEFT JOIN transactions t
               ON t.destinationAccountId = da.id
              AND t.date BETWEEN :startEpochDay AND :endEpochDay
              AND t.type = 'EXPENSE'
        WHERE da.userId = :userId
          AND (
              (da.type IN ('expense', 'savings', 'investment') AND da.parentAccountId IS NULL)
              OR (da.type IN ('investment', 'savings') AND da.parentAccountId IS NOT NULL)
          )
        GROUP BY COALESCE(da.parentAccountId, da.id), COALESCE(parent.name, da.name)
        ORDER BY total DESC
    """)
    fun getExpenseTotalsByDestination(
        userId: Long,
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<AccountTotal>>

    @Query("""
        SELECT * FROM transactions
        WHERE depositAccountId = :depositAccountId
          AND date BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY date DESC
    """)
    fun getStatementForAccount(
        depositAccountId: Long,
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE destinationAccountId = :destinationAccountId
          AND date BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY date DESC
    """)
    fun getByDestinationAccount(
        destinationAccountId: Long,
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0)
        FROM transactions
        WHERE depositAccountId = :depositAccountId
          AND date < :beforeEpochDay
          AND transferGroupId IS NULL
    """)
    suspend fun getOpeningBalance(depositAccountId: Long, beforeEpochDay: Long): Long

    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE depositAccountId = :depositAccountId
          AND date BETWEEN :startEpochDay AND :endEpochDay
          AND type = 'INCOME'
          AND transferGroupId IS NULL
    """)
    suspend fun getPeriodIncome(depositAccountId: Long, startEpochDay: Long, endEpochDay: Long): Long

    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE depositAccountId = :depositAccountId
          AND date BETWEEN :startEpochDay AND :endEpochDay
          AND type = 'EXPENSE'
          AND transferGroupId IS NULL
    """)
    suspend fun getPeriodExpense(depositAccountId: Long, startEpochDay: Long, endEpochDay: Long): Long

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
          AND date BETWEEN :startEpochDay AND :endEpochDay
          AND transferGroupId IS NULL
        ORDER BY date DESC
    """)
    suspend fun getNonTransferTransactions(
        userId: Long,
        startEpochDay: Long,
        endEpochDay: Long
    ): List<TransactionEntity>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE destinationAccountId = :accountId AND type = 'EXPENSE'")
    suspend fun getTotalInvestedInAccount(accountId: Long): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE destinationAccountId = :accountId AND type = 'EXPENSE'")
    fun getTotalExpensesForAccountFlow(accountId: Long): Flow<Long>

    @Query("SELECT * FROM transactions WHERE destinationAccountId = :destinationAccountId ORDER BY date DESC")
    fun getByDestinationAccountAll(destinationAccountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE depositAccountId = :depositAccountId ORDER BY date DESC")
    fun getAllByDepositAccount(depositAccountId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT t.* FROM transactions t
        INNER JOIN destination_accounts da ON da.id = t.destinationAccountId
        WHERE da.parentAccountId = :parentAccountId
          AND t.date BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY t.date DESC
    """)
    fun getByParentInvestmentAccount(
        parentAccountId: Long,
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TransactionEntity>>
}
