package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cuentaconmigo.core.db.entities.AssetLiabilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetLiabilityDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(liability: AssetLiabilityEntity): Long

    @Update
    suspend fun update(liability: AssetLiabilityEntity)

    @Delete
    suspend fun delete(liability: AssetLiabilityEntity)

    @Query("SELECT * FROM asset_liabilities WHERE subAccountId = :subAccountId ORDER BY createdDate DESC")
    fun getBySubAccount(subAccountId: Long): Flow<List<AssetLiabilityEntity>>

    @Query("SELECT * FROM asset_liabilities WHERE subAccountId = :subAccountId AND isPaid = 0 ORDER BY createdDate DESC")
    fun getPendingBySubAccount(subAccountId: Long): Flow<List<AssetLiabilityEntity>>
}