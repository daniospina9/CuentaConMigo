package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cuentaconmigo.core.db.entities.SimpleDebtTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SimpleDebtTransactionDao {
    @Insert
    suspend fun insert(tx: SimpleDebtTransactionEntity): Long

    @Update
    suspend fun update(tx: SimpleDebtTransactionEntity)

    @Delete
    suspend fun delete(tx: SimpleDebtTransactionEntity)

    @Query("SELECT * FROM simple_debt_transactions WHERE debtId = :debtId ORDER BY date DESC")
    fun getByDebt(debtId: Long): Flow<List<SimpleDebtTransactionEntity>>

    @Query("""
        SELECT COALESCE(
            SUM(CASE WHEN type IN ('LOAN_RECEIVED', 'LOAN_GIVEN', 'INTEREST') THEN amount ELSE 0 END) -
            SUM(CASE WHEN type IN ('PAYMENT', 'COLLECTION') THEN amount ELSE 0 END),
        0) FROM simple_debt_transactions WHERE debtId = :debtId
    """)
    fun getCurrentDebt(debtId: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM simple_debt_transactions WHERE debtId = :debtId")
    suspend fun countByDebt(debtId: Long): Int

    @Query("SELECT * FROM simple_debt_transactions WHERE linkedTransactionId = :linkedTransactionId LIMIT 1")
    suspend fun getByLinkedTransactionId(linkedTransactionId: Long): SimpleDebtTransactionEntity?

    @Query("SELECT linkedTransactionId FROM simple_debt_transactions WHERE debtId = :debtId AND linkedTransactionId IS NOT NULL")
    suspend fun getLinkedTransactionIds(debtId: Long): List<Long>

    @Query("SELECT linkedTransactionId FROM simple_debt_transactions WHERE linkedTransactionId IS NOT NULL")
    fun getAllLinkedTransactionIds(): Flow<List<Long>>

    @Query("""
        SELECT t.linkedTransactionId FROM simple_debt_transactions t
        INNER JOIN simple_debts d ON t.debtId = d.id
        WHERE t.linkedTransactionId IS NOT NULL AND d.kind = :kind
    """)
    fun getLinkedTransactionIdsByKind(kind: String): Flow<List<Long>>
}
