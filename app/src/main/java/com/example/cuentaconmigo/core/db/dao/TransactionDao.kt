package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cuentaconmigo.core.db.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class AccountTotal(
    val destinationAccountId: Long,
    val destinationAccountName: String,
    val total: Long
)

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity): Long

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
        SELECT da.id   AS destinationAccountId,
               da.name AS destinationAccountName,
               COALESCE(SUM(t.amount), 0) AS total
        FROM destination_accounts da
        LEFT JOIN transactions t
               ON t.destinationAccountId = da.id
              AND t.date BETWEEN :startEpochDay AND :endEpochDay
              AND t.type = 'EXPENSE'
        WHERE da.userId = :userId
          AND da.type IN ('expense', 'savings')
        GROUP BY da.id, da.name
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
}
