package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import javax.inject.Inject

class DeleteCreditCardUseCase @Inject constructor(
    private val repository: CreditCardRepository
) {
    // Soft delete: sets isActive = false
    suspend operator fun invoke(card: CreditCard) =
        repository.deleteCard(card.copy(isActive = false))
}
