package com.example.cuentaconmigo.features.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import com.example.cuentaconmigo.domain.repository.UserRepository
import com.example.cuentaconmigo.domain.usecase.GetDepositAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class HomePeriod { MONTH, YEAR }

data class AccountWithBalance(val account: DepositAccount, val balance: Long)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDepositAccountsUseCase: GetDepositAccountsUseCase,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    val selectedPeriod = MutableStateFlow(HomePeriod.MONTH)

    val accountsWithBalances: StateFlow<List<AccountWithBalance>> =
        getDepositAccountsUseCase(userId)
            .flatMapLatest { accounts ->
                if (accounts.isEmpty()) flowOf(emptyList())
                else combine(accounts.map { acc ->
                    transactionRepository.getDepositAccountBalance(acc.id)
                        .map { bal -> AccountWithBalance(acc, bal) }
                }) { arr -> arr.toList() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalBalance: StateFlow<Long> = accountsWithBalances
        .map { list -> list.sumOf { it.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val income: StateFlow<Long> = selectedPeriod
        .flatMapLatest { period ->
            val (start, end) = dateRangeFor(period)
            transactionRepository.getUserIncome(userId, start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val expenses: StateFlow<Long> = selectedPeriod
        .flatMapLatest { period ->
            val (start, end) = dateRangeFor(period)
            transactionRepository.getUserExpenses(userId, start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun setPeriod(period: HomePeriod) {
        selectedPeriod.value = period
    }

    init {
        viewModelScope.launch {
            _userName.value = userRepository.getUserById(userId)?.name ?: ""
        }
    }

    private fun dateRangeFor(period: HomePeriod): Pair<Long, Long> {
        val today = LocalDate.now()
        val start = when (period) {
            HomePeriod.MONTH -> today.withDayOfMonth(1)
            HomePeriod.YEAR -> today.withDayOfYear(1)
        }
        return start.toEpochDay() to today.toEpochDay()
    }
}