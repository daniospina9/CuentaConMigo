package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class CreditCardDetail(
    val card: CreditCard?,
    val currentDebt: Long,
    val transactions: List<CreditCardTransaction>
)

class GetCreditCardDetailUseCase @Inject constructor(
    private val repository: CreditCardRepository
) {
    fun getCard(cardId: Long): Flow<CreditCard?> = repository.getById(cardId)

    fun getCurrentDebt(cardId: Long): Flow<Long> = repository.getCurrentDebt(cardId)

    fun getTransactions(cardId: Long): Flow<List<CreditCardTransaction>> =
        repository.getTransactions(cardId)
}
