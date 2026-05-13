package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCreditCardsUseCase @Inject constructor(
    private val repository: CreditCardRepository
) {
    operator fun invoke(userId: Long): Flow<List<CreditCard>> =
        repository.getActiveCards(userId)
}
