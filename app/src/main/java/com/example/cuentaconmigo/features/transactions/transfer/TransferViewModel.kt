package com.example.cuentaconmigo.features.transactions.transfer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TransferState(
    val depositAccounts: List<DepositAccount> = emptyList(),
    val fromAccount: DepositAccount? = null,
    val toAccount: DepositAccount? = null,
    val amountText: String = "",
    val description: String = "",
    val fromError: Boolean = false,
    val toError: Boolean = false,
    val amountError: Boolean = false,
    val sameAccountError: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val depositAccountRepository: DepositAccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    private val _state = MutableStateFlow(TransferState())
    val state: StateFlow<TransferState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            depositAccountRepository.getByUser(userId).collect { accounts ->
                _state.update { it.copy(depositAccounts = accounts) }
            }
        }
    }

    fun setFromAccount(account: DepositAccount) =
        _state.update { it.copy(fromAccount = account, fromError = false, sameAccountError = false) }

    fun setToAccount(account: DepositAccount) =
        _state.update { it.copy(toAccount = account, toError = false, sameAccountError = false) }

    fun setAmount(text: String) =
        _state.update { it.copy(amountText = text, amountError = false) }

    fun setDescription(text: String) =
        _state.update { it.copy(description = text) }

    fun submit() {
        val s = _state.value
        val amount = s.amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L

        val fromError = s.fromAccount == null
        val toError = s.toAccount == null
        val amountError = amount <= 0L
        val sameAccountError = !fromError && !toError && s.fromAccount!!.id == s.toAccount!!.id

        if (fromError || toError || amountError || sameAccountError) {
            _state.update {
                it.copy(
                    fromError = fromError,
                    toError = toError,
                    amountError = amountError,
                    sameAccountError = sameAccountError
                )
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                transactionRepository.insertTransfer(
                    userId = userId,
                    fromAccountId = s.fromAccount!!.id,
                    toAccountId = s.toAccount!!.id,
                    amount = amount,
                    date = LocalDate.now(),
                    description = s.description.ifBlank { null }
                )
            }
                .onSuccess { _state.update { it.copy(isSaved = true) } }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}
