package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    fun getActiveCards(userId: Long): Flow<List<CreditCard>>
    fun getById(id: Long): Flow<CreditCard?>
    fun getTransactions(cardId: Long): Flow<List<CreditCardTransaction>>
    fun getCurrentDebt(cardId: Long): Flow<Long>
    suspend fun createCard(card: CreditCard): Long
    suspend fun updateCard(card: CreditCard)
    suspend fun deleteCard(card: CreditCard)
    suspend fun insertTransaction(tx: CreditCardTransaction): Long
    suspend fun deleteTransaction(tx: CreditCardTransaction)
    suspend fun updateTransaction(tx: CreditCardTransaction)
    suspend fun getTransactionByLinkedId(linkedTransactionId: Long): CreditCardTransaction?
}