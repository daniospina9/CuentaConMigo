package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import javax.inject.Inject

class SeedDefaultAccountsUseCase @Inject constructor(
    private val destinationAccountRepository: DestinationAccountRepository
) {
    suspend operator fun invoke(userId: Long) {
        val defaults = listOf(
            DestinationAccount(0, userId, "Necesidades Básicas",               AccountType.EXPENSE,    isDefault = true),
            DestinationAccount(0, userId, "Juegos y Diversión",                AccountType.EXPENSE,    isDefault = true),
            DestinationAccount(0, userId, "Ahorros a Largo Plazo para gastar", AccountType.SAVINGS,    isDefault = true),
            DestinationAccount(0, userId, "Donativos",                         AccountType.EXPENSE,    isDefault = true),
            DestinationAccount(0, userId, "Inversiones",                       AccountType.INVESTMENT, isDefault = true)
        )
        destinationAccountRepository.insertAll(defaults)
    }
}
