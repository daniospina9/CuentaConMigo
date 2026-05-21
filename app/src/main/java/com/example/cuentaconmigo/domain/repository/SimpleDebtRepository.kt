package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.SimpleDebt
import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import kotlinx.coroutines.flow.Flow

interface SimpleDebtRepository {
    fun getActive(userId: Long): Flow<List<SimpleDebt>>
    fun getById(id: Long): Flow<SimpleDebt?>
    suspend fun create(debt: SimpleDebt): Long
    suspend fun update(debt: SimpleDebt)
    suspend fun delete(debt: SimpleDebt)

    fun getTransactions(debtId: Long): Flow<List<SimpleDebtTransaction>>
    fun getCurrentDebt(debtId: Long): Flow<Long>
    suspend fun insertTransaction(tx: SimpleDebtTransaction): Long
    suspend fun updateTransaction(tx: SimpleDebtTransaction)
    suspend fun deleteTransaction(tx: SimpleDebtTransaction)
    suspend fun hasTransactions(debtId: Long): Boolean
    suspend fun getTransactionByLinkedId(linkedTransactionId: Long): SimpleDebtTransaction?
}