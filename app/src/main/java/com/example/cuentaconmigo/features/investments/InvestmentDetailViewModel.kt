package com.example.cuentaconmigo.features.investments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.InvestmentSubtype
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.InvestmentFluctuationRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvestmentDetailViewModel @Inject constructor(
    private val destinationAccountRepository: DestinationAccountRepository,
    private val investmentFluctuationRepository: InvestmentFluctuationRepository,
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val accountId: Long = checkNotNull(savedStateHandle["accountId"])

    private val _parentAccount = MutableStateFlow<DestinationAccount?>(null)
    val parentAccount: StateFlow<DestinationAccount?> = _parentAccount.asStateFlow()

    val subAccounts: StateFlow<List<InvestmentAccountSummary>> =
        destinationAccountRepository.getSubAccounts(accountId)
            .flatMapLatest { subs ->
                if (subs.isEmpty()) flowOf(emptyList())
                else combine(
                    subs.map { sub ->
                        val valueFlow = when (sub.investmentSubtype) {
                            InvestmentSubtype.EXPENSE -> flow { emit(transactionRepository.getTotalInvestedInAccount(sub.id)) }
                            InvestmentSubtype.LIQUID -> combine(
                                investmentFluctuationRepository.getBalance(userId, sub.id),
                                transactionRepository.getTotalExpensesForAccountFlow(sub.id)
                            ) { fluctuations, expenses -> fluctuations + expenses }
                            else -> investmentFluctuationRepository.getBalance(userId, sub.id)
                        }
                        valueFlow.map { value -> InvestmentAccountSummary(sub, value) }
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

    fun createSubAccount(name: String, subtype: InvestmentSubtype) {
        viewModelScope.launch {
            runCatching {
                destinationAccountRepository.create(
                    DestinationAccount(
                        id = 0,
                        userId = userId,
                        name = name,
                        type = AccountType.INVESTMENT,
                        isDefault = false,
                        investmentSubtype = subtype,
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