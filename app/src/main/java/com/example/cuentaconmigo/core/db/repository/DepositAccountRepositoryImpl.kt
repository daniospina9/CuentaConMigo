package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.DepositAccountDao
import com.example.cuentaconmigo.core.db.entities.DepositAccountEntity
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepositAccountRepositoryImpl @Inject constructor(
    private val dao: DepositAccountDao
) : DepositAccountRepository {

    override fun getByUser(userId: Long): Flow<List<DepositAccount>> =
        dao.getByUser(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun create(account: DepositAccount): Long =
        dao.insert(account.toEntity())

    override suspend fun update(account: DepositAccount) =
        dao.update(account.toEntity())

    override suspend fun delete(account: DepositAccount): Result<Unit> = runCatching {
        val count = dao.getTransactionCount(account.id)
        require(count == 0) { "No se puede eliminar: la cuenta tiene $count transacciones asociadas" }
        dao.delete(account.toEntity())
    }

    override suspend fun getById(id: Long): DepositAccount? =
        dao.getById(id)?.toDomain()
}

private fun DepositAccountEntity.toDomain() =
    DepositAccount(id = id, userId = userId, name = name)

private fun DepositAccount.toEntity() =
    DepositAccountEntity(id = id, userId = userId, name = name)