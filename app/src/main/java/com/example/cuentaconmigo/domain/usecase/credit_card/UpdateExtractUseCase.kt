package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardExtract
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import javax.inject.Inject

class UpdateExtractUseCase @Inject constructor(
    private val repository: CreditCardRepository,
    private val registerExtractUseCase: RegisterExtractUseCase
) {
    suspend operator fun invoke(extract: CreditCardExtract, userId: Long) {
        repository.deleteTransactionsByExtractId(extract.id)
        registerExtractUseCase.createLinkedTransactions(extract, userId, extract.id)
        repository.updateExtract(extract.copy(isReconciled = false))
    }
}