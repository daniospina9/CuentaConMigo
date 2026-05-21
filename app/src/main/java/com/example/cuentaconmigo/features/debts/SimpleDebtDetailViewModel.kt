package com.example.cuentaconmigo.features.debts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.SimpleDebt
import com.example.cuentaconmigo.domain.model.SimpleDebtTransaction
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import com.example.cuentaconmigo.domain.usecase.simple_debt.DeleteSimpleDebtTransactionUseCase
import com.example.cuentaconmigo.domain.usecase.simple_debt.GetSimpleDebtDetailUseCase
import com.example.cuentaconmigo.domain.usecase.simple_debt.RegisterLoanReceivedUseCase
import com.example.cuentaconmigo.domain.usecase.simple_debt.RegisterSimpleDebtInterestUseCase
import com.example.cuentaconmigo.domain.usecase.simple_debt.RegisterSimpleDebtPaymentUseCase
import com.example.cuentaconmigo.domain.usecase.simple_debt.UpdateSimpleDebtTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimpleDebtDetailViewModel @Inject constructor(
    private val getDetail: GetSimpleDebtDetailUseCase,
    private val registerLoanReceived: RegisterLoanReceivedUseCase,
    private val registerPayment: RegisterSimpleDebtPaymentUseCase,
    private val registerInterest: RegisterSimpleDebtInterestUseCase,
    private val deleteTransaction: DeleteSimpleDebtTransactionUseCase,
    private val updateTransaction: UpdateSimpleDebtTransactionUseCase,
    private val depositAccountRepository: DepositAccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val debtId: Long = checkNotNull(savedStateHandle["debtId"])

    val debt: StateFlow<SimpleDebt?> =
        getDetail.getDebt(debtId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentDebt: StateFlow<Long> =
        getDetail.getCurrentDebt(debtId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val transactions: StateFlow<List<SimpleDebtTransaction>> =
        getDetail.getTransactions(debtId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val depositAccounts: StateFlow<List<DepositAccount>> =
        depositAccountRepository.getByUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun registerLoan(amount: Long, depositAccountId: Long, description: String?, date: Long) {
        viewModelScope.launch {
            runCatching {
                registerLoanReceived(
                    debtId = debtId,
                    debtName = debt.value?.name ?: "",
                    userId = userId,
                    amount = amount,
                    depositAccountId = depositAccountId,
                    description = description,
                    date = date
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun registerPaymentTx(amount: Long, depositAccountId: Long, description: String?, date: Long) {
        viewModelScope.launch {
            runCatching {
                registerPayment(
                    debtId = debtId,
                    debtName = debt.value?.name ?: "",
                    userId = userId,
                    amount = amount,
                    depositAccountId = depositAccountId,
                    description = description,
                    date = date
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun registerInterestTx(amount: Long, description: String?, date: Long) {
        viewModelScope.launch {
            runCatching {
                registerInterest(
                    debtId = debtId,
                    userId = userId,
                    amount = amount,
                    description = description,
                    date = date
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteTx(tx: SimpleDebtTransaction) {
        viewModelScope.launch {
            runCatching { deleteTransaction(tx) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun updateTx(tx: SimpleDebtTransaction) {
        viewModelScope.launch {
            runCatching { updateTransaction(tx) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}