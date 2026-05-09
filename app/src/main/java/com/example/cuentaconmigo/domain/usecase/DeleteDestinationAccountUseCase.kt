package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteDestinationAccountUseCase @Inject constructor(
    private val repository: DestinationAccountRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(account: DestinationAccount): Result<Unit> = runCatching {
        require(!account.isDefault) { "No se pueden eliminar las cuentas por defecto" }
        if (account.parentAccountId == null) {
            require(!repository.hasSubAccounts(account.id)) {
                "No se puede eliminar \"${account.name}\" porque tiene subcuentas. Elimínalas primero."
            }
        }
        require(!transactionRepository.hasTransactions(account.id)) {
            "No se puede eliminar \"${account.name}\" porque tiene transacciones registradas."
        }
        repository.delete(account).getOrThrow()
    }
}