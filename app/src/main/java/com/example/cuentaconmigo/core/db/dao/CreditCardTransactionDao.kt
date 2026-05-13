package com.example.cuentaconmigo.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cuentaconmigo.core.db.entities.CreditCardTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardTransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tx: CreditCardTransactionEntity): Long

    @Delete
    suspend fun delete(tx: CreditCardTransactionEntity)

    @Query("SELECT * FROM credit_card_transactions WHERE creditCardId = :cardId ORDER BY date DESC")
    fun getByCard(cardId: Long): Flow<List<CreditCardTransactionEntity>>

    @Query("""
        SELECT COALESCE(
            SUM(CASE WHEN type = 'PURCHASE' THEN amount ELSE 0 END) -
            SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END), 0)
        FROM credit_card_transactions WHERE creditCardId = :cardId
    """)
    fun getCurrentDebt(cardId: Long): Flow<Long>
}