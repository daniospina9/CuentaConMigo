package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardExtract
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import javax.inject.Inject

class ReconcileExtractUseCase @Inject constructor(
    private val repository: CreditCardRepository
) {
    suspend operator fun invoke(extract: CreditCardExtract) =
        repository.updateExtract(extract.copy(isReconciled = true))
}