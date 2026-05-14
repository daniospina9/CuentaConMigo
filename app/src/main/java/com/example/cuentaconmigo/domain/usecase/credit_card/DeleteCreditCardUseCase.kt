package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import javax.inject.Inject

class DeleteCreditCardUseCase @Inject constructor(
    private val repository: CreditCardRepository
) {
    suspend operator fun invoke(card: CreditCard) {
        if (repository.hasTransactions(card.id))
            throw IllegalStateException("No se puede eliminar una tarjeta con movimientos registrados.")
        repository.deleteCard(card.copy(isActive = false))
    }
}
