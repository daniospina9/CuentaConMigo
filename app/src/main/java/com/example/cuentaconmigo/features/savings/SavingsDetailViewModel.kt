package com.example.cuentaconmigo.features.savings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.SavingsMovementRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavingsDetailViewModel @Inject constructor(
    private val destinationAccountRepository: DestinationAccountRepository,
    private val savingsMovementRepository: SavingsMovementRepository,
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val accountId: Long = checkNotNull(savedStateHandle["accountId"])

    private val _parentAccount = MutableStateFlow<DestinationAccount?>(null)
    val parentAccount: StateFlow<DestinationAccount?> = _parentAccount.asStateFlow()

    val subAccounts: StateFlow<List<SavingsAccountSummary>> =
        destinationAccountRepository.getSubAccounts(accountId)
            .flatMapLatest { subs ->
                if (subs.isEmpty()) flowOf(emptyList())
                else combine(
                    subs.map { sub ->
                        combine(
                            savingsMovementRepository.getBalance(sub.id),
                            transactionRepository.getTotalExpensesForAccountFlow(sub.id)
                        ) { withdrawals, deposits -> SavingsAccountSummary(sub, deposits + withdrawals) }
                    }
                ) { summaries -> summaries.toList() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _parentAccount.value = destinationAccountRepository.getById(accountId)
        }
    }

    fun createSubAccount(name: String) {
        viewModelScope.launch {
            runCatching {
                destinationAccountRepository.create(
                    DestinationAccount(
                        id = 0,
                        userId = userId,
                        name = name,
                        type = AccountType.SAVINGS,
                        isDefault = false,
                        parentAccountId = accountId
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun renameSubAccount(account: DestinationAccount, newName: String) {
        viewModelScope.launch {
            runCatching { destinationAccountRepository.update(account.copy(name = newName)) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteSubAccount(account: DestinationAccount) {
        viewModelScope.launch {
            destinationAccountRepository.delete(account)
                .onFailure { _errorMessage.value = it.message ?: "No se pudo eliminar la subcuenta" }
        }
    }

    fun clearError() { _errorMessage.value = null }
}