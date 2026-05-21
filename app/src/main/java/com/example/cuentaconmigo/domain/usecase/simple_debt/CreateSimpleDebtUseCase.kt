package com.example.cuentaconmigo.domain.usecase.simple_debt

import com.example.cuentaconmigo.domain.model.SimpleDebt
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import javax.inject.Inject

class CreateSimpleDebtUseCase @Inject constructor(
    private val repository: SimpleDebtRepository
) {
    suspend operator fun invoke(debt: SimpleDebt): Long = repository.create(debt)
}