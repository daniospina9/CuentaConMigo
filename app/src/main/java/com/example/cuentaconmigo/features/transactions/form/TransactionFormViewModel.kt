package com.example.cuentaconmigo.features.transactions.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TransactionFormState(
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedDepositAccount: DepositAccount? = null,
    val selectedDestinationAccount: DestinationAccount? = null,
    val amountText: String = "",
    val date: LocalDate = LocalDate.now(),
    val description: String = "",
    val depositAccounts: List<DepositAccount> = emptyList(),
    val destinationAccounts: List<DestinationAccount> = emptyList(),
    val amountError: Boolean = false,
    val depositError: Boolean = false,
    val destinationError: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val depositAccountRepository: DepositAccountRepository,
    private val destinationAccountRepository: DestinationAccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val initialType: String = savedStateHandle["type"] ?: "EXPENSE"

    private val _state = MutableStateFlow(
        TransactionFormState(type = if (initialType == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE)
    )
    val state: StateFlow<TransactionFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                depositAccountRepository.getByUser(userId),
                destinationAccountRepository.getByUser(userId)
            ) { deposits, destinations -> deposits to destinations }
                .collect { (deposits, destinations) ->
                    _state.update { it.copy(depositAccounts = deposits, destinationAccounts = destinations) }
                }
        }
    }

    fun setType(type: TransactionType) = _state.update { it.copy(type = type, selectedDestinationAccount = null) }
    fun setDepositAccount(account: DepositAccount) = _state.update { it.copy(selectedDepositAccount = account, depositError = false) }
    fun setDestinationAccount(account: DestinationAccount) = _state.update { it.copy(selectedDestinationAccount = account, destinationError = false) }
    fun setAmount(text: String) = _state.update { it.copy(amountText = text, amountError = false) }
    fun setDate(date: LocalDate) = _state.update { it.copy(date = date) }
    fun setDescription(text: String) = _state.update { it.copy(description = text) }

    fun submit() {
        val s = _state.value
        val amount = s.amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        val hasAmountError = amount <= 0L
        val hasDepositError = s.selectedDepositAccount == null
        val hasDestinationError = s.type == TransactionType.EXPENSE && s.selectedDestinationAccount == null

        if (hasAmountError || hasDepositError || hasDestinationError) {
            _state.update {
                it.copy(amountError = hasAmountError, depositError = hasDepositError, destinationError = hasDestinationError)
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                transactionRepository.insert(
                    Transaction(
                        id = 0,
                        userId = userId,
                        depositAccountId = s.selectedDepositAccount!!.id,
                        destinationAccountId = s.selectedDestinationAccount?.id,
                        type = s.type,
                        amount = amount,
                        date = s.date,
                        description = s.description.ifBlank { null }
                    )
                )
            }
                .onSuccess { _state.update { it.copy(isSaved = true) } }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}
