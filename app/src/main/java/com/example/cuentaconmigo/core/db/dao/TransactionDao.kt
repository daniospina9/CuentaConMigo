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

    @Delete
    suspend fun delete(transaction: TransactionEntity)

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
}
