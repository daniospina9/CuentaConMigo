package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.repository.TransactionRepository
import java.time.LocalDate
import javax.inject.Inject

class InsertTransferUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        userId: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        date: LocalDate,
        description: String? = null
    ) = repository.insertTransfer(userId, fromAccountId, toAccountId, amount, date, description)
}