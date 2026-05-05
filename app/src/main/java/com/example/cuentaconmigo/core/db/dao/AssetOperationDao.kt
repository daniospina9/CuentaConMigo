package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cuentaconmigo.core.db.entities.AssetOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetOperationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(op: AssetOperationEntity): Long

    @Delete
    suspend fun delete(op: AssetOperationEntity)

    @Query("SELECT * FROM asset_operations WHERE subAccountId = :subAccountId ORDER BY date DESC")
    fun getBySubAccount(subAccountId: Long): Flow<List<AssetOperationEntity>>

    @Query("SELECT COALESCE(SUM(assetValueDelta), 0) FROM asset_operations WHERE subAccountId = :subAccountId")
    fun getAssetValueDeltaSum(subAccountId: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(balanceEffect), 0) FROM asset_operations WHERE subAccountId = :subAccountId")
    fun getBalanceEffectSum(subAccountId: Long): Flow<Long>

    @Query("DELETE FROM asset_operations WHERE withdrawalGroupId = :withdrawalGroupId")
    suspend fun deleteByWithdrawalGroupId(withdrawalGroupId: String)
}