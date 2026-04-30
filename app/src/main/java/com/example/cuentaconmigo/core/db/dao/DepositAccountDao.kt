package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cuentaconmigo.core.db.entities.DepositAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositAccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: DepositAccountEntity): Long

    @Update
    suspend fun update(account: DepositAccountEntity)

    @Delete
    suspend fun delete(account: DepositAccountEntity)

    @Query("SELECT * FROM deposit_accounts WHERE userId = :userId ORDER BY name ASC")
    fun getByUser(userId: Long): Flow<List<DepositAccountEntity>>

    @Query("SELECT * FROM deposit_accounts WHERE id = :id")
    suspend fun getById(id: Long): DepositAccountEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE depositAccountId = :accountId")
    suspend fun getTransactionCount(accountId: Long): Int
}
