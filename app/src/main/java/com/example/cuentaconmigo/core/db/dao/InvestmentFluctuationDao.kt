package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cuentaconmigo.core.db.entities.InvestmentFluctuationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentFluctuationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(fluctuation: InvestmentFluctuationEntity): Long

    @Delete
    suspend fun delete(fluctuation: InvestmentFluctuationEntity)

    @Query("""
        SELECT * FROM investment_fluctuations
        WHERE destinationAccountId = :accountId
        ORDER BY date DESC
    """)
    fun getByAccount(accountId: Long): Flow<List<InvestmentFluctuationEntity>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM investment_fluctuations
        WHERE userId = :userId AND destinationAccountId = :accountId
    """)
    fun getBalance(userId: Long, accountId: Long): Flow<Long>

    @Query("DELETE FROM investment_fluctuations WHERE withdrawalGroupId = :withdrawalGroupId")
    suspend fun deleteByWithdrawalGroupId(withdrawalGroupId: String)
}
