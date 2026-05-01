package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<User>>
    suspend fun createUser(name: String): Result<Long>
    suspend fun getUserById(id: Long): User?
    suspend fun deleteUser(user: User)
}
