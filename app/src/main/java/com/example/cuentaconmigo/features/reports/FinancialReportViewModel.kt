package com.example.cuentaconmigo.features.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.AccountTotal
import com.example.cuentaconmigo.domain.model.DepositAccountStatement
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FinancialReportState(
    val depositStatements: List<DepositAccountStatement> = emptyList(),
    val expenseByCategory: List<AccountTotal> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val generated: Boolean = false
)

@HiltViewModel
class FinancialReportViewModel @Inject constructor(
    private val depositAccountRepository: DepositAccountRepository,
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()
    val endDate: StateFlow<LocalDate?> = _endDate.asStateFlow()

    private val _state = MutableStateFlow(FinancialReportState())
    val state: StateFlow<FinancialReportState> = _state.asStateFlow()

    fun setStartDate(date: LocalDate) { _startDate.value = date }
    fun setEndDate(date: LocalDate) { _endDate.value = date }

    fun generate() {
        val start = _startDate.value
            ?: run { _state.value = _state.value.copy(error = "Selecciona la fecha de inicio"); return }
        val end = _endDate.value
            ?: run { _state.value = _state.value.copy(error = "Selecciona la fecha de fin"); return }
        if (end < start) {
            _state.value = _state.value.copy(error = "La fecha final debe ser posterior al inicio")
            return
        }

        viewModelScope.launch {
            _state.value = FinancialReportState(isLoading = true)

            val startDay = start.toEpochDay()
            val endDay = end.toEpochDay()

            val accounts = depositAccountRepository.getByUser(userId).first()

            val statements = accounts.map { account ->
                val opening = transactionRepository.getOpeningBalance(account.id, start)
                val income = transactionRepository.getPeriodIncome(account.id, start, end)
                val expense = transactionRepository.getPeriodExpense(account.id, start, end)
                DepositAccountStatement(
                    accountName = account.name,
                    openingBalance = opening,
                    periodIncome = income,
                    periodExpense = expense,
                    closingBalance = opening + income - expense
                )
            }

            val transactions = transactionRepository.getNonTransferTransactions(userId, start, end)

            val expenseTotals = transactionRepository
                .getExpenseTotalsByDestination(userId, startDay, endDay)
                .first()
                .filter { it.total > 0 }

            _state.value = FinancialReportState(
                depositStatements = statements,
                expenseByCategory = expenseTotals,
                transactions = transactions,
                generated = true
            )
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
