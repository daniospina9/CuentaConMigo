package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.CreditCardDao
import com.example.cuentaconmigo.core.db.dao.CreditCardExtractDao
import com.example.cuentaconmigo.core.db.dao.CreditCardTransactionDao
import com.example.cuentaconmigo.core.db.repository.mappers.toDomain
import com.example.cuentaconmigo.core.db.repository.mappers.toEntity
import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.CreditCardExtract
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditCardRepositoryImpl @Inject constructor(
    private val cardDao: CreditCardDao,
    private val txDao: CreditCardTransactionDao,
    private val extractDao: CreditCardExtractDao
) : CreditCardRepository {

    override fun getActiveCards(userId: Long): Flow<List<CreditCard>> =
        cardDao.getActiveCards(userId).map { list -> list.map { it.toDomain() } }

    override fun getById(id: Long): Flow<CreditCard?> =
        cardDao.getById(id).map { it?.toDomain() }

    override fun getTransactions(cardId: Long): Flow<List<CreditCardTransaction>> =
        txDao.getByCard(cardId).map { list -> list.map { it.toDomain() } }

    override fun getCurrentDebt(cardId: Long): Flow<Long> =
        txDao.getCurrentDebt(cardId)

    override suspend fun createCard(card: CreditCard): Long =
        cardDao.insert(card.toEntity())

    override suspend fun updateCard(card: CreditCard) =
        cardDao.update(card.toEntity())

    override suspend fun deleteCard(card: CreditCard) =
        cardDao.update(card.toEntity())

    override suspend fun insertTransaction(tx: CreditCardTransaction): Long =
        txDao.insert(tx.toEntity())

    override suspend fun deleteTransaction(tx: CreditCardTransaction) =
        txDao.delete(tx.toEntity())

    override suspend fun updateTransaction(tx: CreditCardTransaction) =
        txDao.update(tx.toEntity())

    override suspend fun getTransactionByLinkedId(linkedTransactionId: Long): CreditCardTransaction? =
        txDao.getByLinkedTransactionId(linkedTransactionId)?.toDomain()

    override suspend fun hasTransactions(cardId: Long): Boolean =
        txDao.countByCard(cardId) > 0

    override fun getExtracts(cardId: Long): Flow<List<CreditCardExtract>> =
        extractDao.getAll(cardId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertExtract(extract: CreditCardExtract): Long =
        extractDao.insert(extract.toEntity())

    override suspend fun updateExtract(extract: CreditCardExtract) =
        extractDao.update(extract.toEntity())

    override suspend fun deleteExtract(extract: CreditCardExtract) =
        extractDao.delete(extract.toEntity())

    override suspend fun deleteTransactionsByExtractId(extractId: Long) =
        txDao.deleteByExtractId(extractId)
}