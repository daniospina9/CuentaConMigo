package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.SavingsMovement
import kotlinx.coroutines.flow.Flow

interface SavingsMovementRepository {
    fun getBySubAccount(subAccountId: Long): Flow<List<SavingsMovement>>
    fun getBalance(subAccountId: Long): Flow<Long>
    suspend fun insert(movement: SavingsMovement): Long
    suspend fun delete(movement: SavingsMovement)
    suspend fun deleteByGroupId(groupId: String)
}