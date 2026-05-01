package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.AccountTotal
import com.example.cuentaconmigo.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun getByUser(userId: Long): Flow<List<Transaction>>
    suspend fun insert(transaction: Transaction): Long
    suspend fun delete(transaction: Transaction)
    suspend fun insertTransfer(
        userId: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        date: LocalDate,
        description: String?
    )
    suspend fun deleteTransfer(transferGroupId: String)
    fun getDepositAccountBalance(depositAccountId: Long): Flow<Long>
    fun getExpenseTotalsByDestination(
        userId: Long,
        startDay: Long,
        endDay: Long
    ): Flow<List<AccountTotal>>
    fun getStatementForAccount(
        depositAccountId: Long,
        startDay: Long,
        endDay: Long
    ): Flow<List<Transaction>>

    fun getByDestinationAccount(
        destinationAccountId: Long,
        startDay: Long,
        endDay: Long
    ): Flow<List<Transaction>>

    suspend fun getOpeningBalance(depositAccountId: Long, beforeDay: Long): Long
    suspend fun getPeriodIncome(depositAccountId: Long, startDay: Long, endDay: Long): Long
    suspend fun getPeriodExpense(depositAccountId: Long, startDay: Long, endDay: Long): Long
    suspend fun getNonTransferTransactions(userId: Long, startDay: Long, endDay: Long): List<Transaction>
}
