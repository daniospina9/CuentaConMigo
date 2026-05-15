package com.example.cuentaconmigo.core.db.dao

import androidx.room.*
import com.example.cuentaconmigo.core.db.entities.CreditCardExtractEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardExtractDao {
    @Insert
    suspend fun insert(extract: CreditCardExtractEntity): Long

    @Update
    suspend fun update(extract: CreditCardExtractEntity)

    @Delete
    suspend fun delete(extract: CreditCardExtractEntity)

    @Query("SELECT * FROM credit_card_extracts WHERE creditCardId = :cardId ORDER BY registeredAt DESC")
    fun getAll(cardId: Long): Flow<List<CreditCardExtractEntity>>
}
