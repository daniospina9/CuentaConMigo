package com.example.cuentaconmigo.features.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import com.example.cuentaconmigo.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountWithBalance(val account: DepositAccount, val balance: Long)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val depositAccountRepository: DepositAccountRepository,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    val accountsWithBalances: StateFlow<List<AccountWithBalance>> =
        depositAccountRepository.getByUser(userId)
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

    init {
        viewModelScope.launch {
            _userName.value = userRepository.getUserById(userId)?.name ?: ""
        }
    }
}
