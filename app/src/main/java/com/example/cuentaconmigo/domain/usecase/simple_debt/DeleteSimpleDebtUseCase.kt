package com.example.cuentaconmigo.domain.usecase.simple_debt

import com.example.cuentaconmigo.domain.model.SimpleDebt
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import javax.inject.Inject

class DeleteSimpleDebtUseCase @Inject constructor(
    private val repository: SimpleDebtRepository
) {
    suspend operator fun invoke(debt: SimpleDebt) {
        if (repository.hasTransactions(debt.id))
            throw IllegalStateException("No se puede eliminar \"${debt.name}\" porque tiene movimientos registrados.")
        repository.delete(debt.copy(isActive = false))
    }
}