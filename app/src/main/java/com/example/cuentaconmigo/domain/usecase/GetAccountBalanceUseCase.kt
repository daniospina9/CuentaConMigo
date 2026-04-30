package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.repository.InvestmentFluctuationRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountBalanceUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val investmentFluctuationRepository: InvestmentFluctuationRepository
) {
    fun forDepositAccount(depositAccountId: Long): Flow<Long> =
        transactionRepository.getDepositAccountBalance(depositAccountId)

    fun forInvestmentAccount(userId: Long, accountId: Long): Flow<Long> =
        investmentFluctuationRepository.getBalance(userId, accountId)
}