package com.example.cuentaconmigo.core.db.dao

import androidx.room.*
import com.example.cuentaconmigo.core.db.entities.SavingsMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsMovementDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(movement: SavingsMovementEntity): Long

    @Delete
    suspend fun delete(movement: SavingsMovementEntity)

    @Query("SELECT * FROM savings_movements WHERE subAccountId = :subAccountId ORDER BY date DESC")
    fun getBySubAccount(subAccountId: Long): Flow<List<SavingsMovementEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM savings_movements WHERE subAccountId = :subAccountId")
    fun getBalance(subAccountId: Long): Flow<Long>

    @Query("DELETE FROM savings_movements WHERE groupId = :groupId")
    suspend fun deleteByGroupId(groupId: String)
}