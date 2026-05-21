package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cuentaconmigo.core.db.entities.SimpleDebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SimpleDebtDao {
    @Insert
    suspend fun insert(debt: SimpleDebtEntity): Long

    @Update
    suspend fun update(debt: SimpleDebtEntity)

    @Delete
    suspend fun delete(debt: SimpleDebtEntity)

    @Query("SELECT * FROM simple_debts WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActive(userId: Long): Flow<List<SimpleDebtEntity>>

    @Query("SELECT * FROM simple_debts WHERE id = :id")
    fun getById(id: Long): Flow<SimpleDebtEntity?>
}
