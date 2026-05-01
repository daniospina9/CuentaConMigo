package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.AccountPercentage
import com.example.cuentaconmigo.domain.model.ReportState
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.roundToInt

class GetReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        userId: Long,
        startDay: Long,
        endDay: Long
    ): Flow<ReportState> =
        transactionRepository.getExpenseTotalsByDestination(userId, startDay, endDay)
            .map { totals ->
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
                    }
                )
            }
}
