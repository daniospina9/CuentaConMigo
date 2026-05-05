package com.example.cuentaconmigo.core.db.repository

import com.example.cuentaconmigo.core.db.dao.UserDao
import com.example.cuentaconmigo.core.db.entities.UserEntity
import com.example.cuentaconmigo.core.db.repository.mappers.toDomain
import com.example.cuentaconmigo.domain.model.User
import com.example.cuentaconmigo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getAllUsers(): Flow<List<User>> =
        userDao.getAllUsers().map { list -> list.map { it.toDomain() } }

    override suspend fun createUser(name: String): Result<Long> = runCatching {
        val existing = userDao.getUserByName(name)
        require(existing == null) { "Ya existe un usuario con ese nombre" }
        userDao.insert(UserEntity(name = name.trim()))
    }

    override suspend fun getUserById(id: Long): User? =
        userDao.getUserById(id)?.toDomain()

    override suspend fun deleteUser(user: User) =
        userDao.delete(UserEntity(id = user.id, name = user.name))
}
