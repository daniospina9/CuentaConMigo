package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cuentaconmigo.core.db.entities.DestinationAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinationAccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: DestinationAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(accounts: List<DestinationAccountEntity>)

    @Update
    suspend fun update(account: DestinationAccountEntity)

    @Delete
    suspend fun delete(account: DestinationAccountEntity)

    @Query("SELECT * FROM destination_accounts WHERE userId = :userId ORDER BY name ASC")
    fun getByUser(userId: Long): Flow<List<DestinationAccountEntity>>

    @Query("SELECT * FROM destination_accounts WHERE id = :id")
    suspend fun getById(id: Long): DestinationAccountEntity?

    @Query("SELECT * FROM destination_accounts WHERE userId = :userId AND type = 'investment' LIMIT 1")
    suspend fun getInvestmentAccount(userId: Long): DestinationAccountEntity?

    @Query("SELECT * FROM destination_accounts WHERE userId = :userId AND type = 'investment'")
    fun getInvestmentAccounts(userId: Long): Flow<List<DestinationAccountEntity>>
}
