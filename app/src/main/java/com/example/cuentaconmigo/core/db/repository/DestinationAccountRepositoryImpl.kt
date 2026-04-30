package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.DestinationAccountDao
import com.example.cuentaconmigo.core.db.entities.DestinationAccountEntity
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DestinationAccountRepositoryImpl @Inject constructor(
    private val dao: DestinationAccountDao
) : DestinationAccountRepository {

    override fun getByUser(userId: Long): Flow<List<DestinationAccount>> =
        dao.getByUser(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun create(account: DestinationAccount): Long =
        dao.insert(account.toEntity())

    override suspend fun insertAll(accounts: List<DestinationAccount>) =
        dao.insertAll(accounts.map { it.toEntity() })

    override suspend fun update(account: DestinationAccount) =
        dao.update(account.toEntity())

    override suspend fun delete(account: DestinationAccount): Result<Unit> = runCatching {
        require(!account.isDefault) { "No se pueden eliminar las cuentas de destino por defecto" }
        dao.delete(account.toEntity())
    }

    override suspend fun getById(id: Long): DestinationAccount? =
        dao.getById(id)?.toDomain()

    override suspend fun getInvestmentAccount(userId: Long): DestinationAccount? =
        dao.getInvestmentAccount(userId)?.toDomain()
}

private fun DestinationAccountEntity.toDomain() = DestinationAccount(
    id = id,
    userId = userId,
    name = name,
    type = type.toAccountType(),
    isDefault = isDefault
)

private fun DestinationAccount.toEntity() = DestinationAccountEntity(
    id = id,
    userId = userId,
    name = name,
    type = type.toEntityString(),
    isDefault = isDefault
)

private fun String.toAccountType(): AccountType = when (this) {
    "expense"    -> AccountType.EXPENSE
    "savings"    -> AccountType.SAVINGS
    "investment" -> AccountType.INVESTMENT
    else         -> throw IllegalArgumentException("Tipo de cuenta desconocido: $this")
}

private fun AccountType.toEntityString(): String = when (this) {
    AccountType.EXPENSE    -> "expense"
    AccountType.SAVINGS    -> "savings"
    AccountType.INVESTMENT -> "investment"
}