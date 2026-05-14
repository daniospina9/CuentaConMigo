package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import javax.inject.Inject

class CreateCreditCardUseCase @Inject constructor(
    private val repository: CreditCardRepository
) {
    suspend operator fun invoke(card: CreditCard): Long =
        repository.createCard(card)
}