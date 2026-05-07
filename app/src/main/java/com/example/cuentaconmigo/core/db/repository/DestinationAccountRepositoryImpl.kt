package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.DestinationAccountDao
import com.example.cuentaconmigo.core.db.repository.mappers.toDomain
import com.example.cuentaconmigo.core.db.repository.mappers.toEntity
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

    override fun getInvestmentAccounts(userId: Long): Flow<List<DestinationAccount>> =
        dao.getInvestmentAccounts(userId).map { list -> list.map { it.toDomain() } }

    override fun getSavingsAccounts(userId: Long): Flow<List<DestinationAccount>> =
        dao.getSavingsAccounts(userId).map { list -> list.map { it.toDomain() } }

    override fun getSubAccounts(parentAccountId: Long): Flow<List<DestinationAccount>> =
        dao.getSubAccounts(parentAccountId).map { list -> list.map { it.toDomain() } }
}