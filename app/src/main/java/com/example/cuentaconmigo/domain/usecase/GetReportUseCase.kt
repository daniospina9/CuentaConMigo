package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.AccountPercentage
import com.example.cuentaconmigo.domain.model.ReportState
import com.example.cuentaconmigo.domain.repository.InvestmentFluctuationRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.roundToInt

class GetReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val investmentFluctuationRepository: InvestmentFluctuationRepository
) {
    operator fun invoke(
        userId: Long,
        startDay: Long,
        endDay: Long,
        investmentAccountId: Long
    ): Flow<ReportState> = combine(
        transactionRepository.getExpenseTotalsByDestination(userId, startDay, endDay),
        investmentFluctuationRepository.getBalance(userId, investmentAccountId)
    ) { totals, investmentBalance ->
        val grandTotal = totals.sumOf { it.total }
        ReportState(
            expenseTotals = totals,
            expensePercentages = totals.map { t ->
                AccountPercentage(
                    destinationAccountId = t.destinationAccountId,
                    destinationAccountName = t.destinationAccountName,
                    total = t.total,
                    percentage = if (grandTotal > 0L)
                        (t.total.toFloat() / grandTotal.toFloat() * 100).roundToInt()
                    else 0
                )
            },
            investmentBalance = investmentBalance
        )
    }
}
