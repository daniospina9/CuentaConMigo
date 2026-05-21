package com.example.cuentaconmigo.domain.usecase.simple_debt

import com.example.cuentaconmigo.domain.model.SimpleDebt
import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSimpleDebtDetailUseCase @Inject constructor(
    private val repository: SimpleDebtRepository
) {
    fun getDebt(debtId: Long): Flow<SimpleDebt?> = repository.getById(debtId)
    fun getCurrentDebt(debtId: Long): Flow<Long> = repository.getCurrentDebt(debtId)
    fun getTransactions(debtId: Long): Flow<List<SimpleDebtTransaction>> = repository.getTransactions(debtId)
}