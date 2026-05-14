package com.example.cuentaconmigo.features.transactions.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.core.util.parseToCentavos
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.usecase.GetDepositAccountsUseCase
import com.example.cuentaconmigo.domain.usecase.GetDestinationAccountsUseCase
import com.example.cuentaconmigo.domain.usecase.GetTransactionByIdUseCase
import com.example.cuentaconmigo.domain.usecase.InsertTransactionUseCase
import com.example.cuentaconmigo.domain.usecase.InsertTransferUseCase
import com.example.cuentaconmigo.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TransactionFormState(
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedDepositAccount: DepositAccount? = null,
    val selectedDestinationAccount: DestinationAccount? = null,
    val selectedSubAccount: DestinationAccount? = null,
    val amountText: String = "",
    val date: LocalDate = LocalDate.now(),
    val description: String = "",
    val depositAccounts: List<DepositAccount> = emptyList(),
    val destinationAccounts: List<DestinationAccount> = emptyList(),
    val subAccounts: List<DestinationAccount> = emptyList(),
    val toDepositAccount: DepositAccount? = null,
    val amountError: Boolean = false,
    val depositError: Boolean = false,
    val destinationError: Boolean = false,
    val subAccountError: Boolean = false,
    val toDepositError: Boolean = false,
    val sameAccountError: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val destinationIsInvestment: Boolean
        get() = selectedDestinationAccount?.type == AccountType.INVESTMENT

    val destinationNeedsSubAccount: Boolean
        get() = selectedDestinationAccount?.type == AccountType.INVESTMENT ||
                selectedDestinationAccount?.type == AccountType.SAVINGS

    val effectiveDestinationId: Long?
        get() = if (destinationNeedsSubAccount) selectedSubAccount?.id
                else selectedDestinationAccount?.id
}

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    private val insertTransactionUseCase: InsertTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val insertTransferUseCase: InsertTransferUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getDepositAccountsUseCase: GetDepositAccountsUseCase,
    private val getDestinationAccountsUseCase: GetDestinationAccountsUseCase,
    private val destinationAccountRepository: DestinationAccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val initialType: String = savedStateHandle["type"] ?: "EXPENSE"
    private val transactionId: Long = savedStateHandle["transactionId"] ?: 0L
    val isEditMode: Boolean = transactionId > 0L

    private val _state = MutableStateFlow(
        TransactionFormState(type = if (initialType == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE)
    )
    val state: StateFlow<TransactionFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getDepositAccountsUseCase(userId),
                getDestinationAccountsUseCase(userId)
            ) { deposits, destinations -> deposits to destinations }
                .collect { (deposits, destinations) ->
                    _state.update { it.copy(depositAccounts = deposits, destinationAccounts = destinations) }

                    if (isEditMode && deposits.isNotEmpty()) {
                        val existing = _state.value
                        if (existing.selectedDepositAccount == null && existing.amountText.isEmpty()) {
                            val tx = getTransactionByIdUseCase(transactionId) ?: return@collect
                            val depositAccount = deposits.find { it.id == tx.depositAccountId }
                            // destination may be a sub-account not in the top-level list
                            val destinationAccount = destinations.find { it.id == tx.destinationAccountId }
                                ?: tx.destinationAccountId?.let { destinationAccountRepository.getById(it) }
                            val parentAccount = if (destinationAccount?.parentAccountId != null)
                                destinations.find { it.id == destinationAccount.parentAccountId }
                            else null

                            _state.update {
                                it.copy(
                                    type = tx.type,
                                    selectedDepositAccount = depositAccount,
                                    selectedDestinationAccount = parentAccount ?: destinationAccount,
                                    selectedSubAccount = if (parentAccount != null) destinationAccount else null,
                                    amountText = tx.amount.toString(),
                                    date = tx.date,
                                    description = tx.description ?: ""
                                )
                            }
                            // If edit mode and investment account, load sub-accounts
                            if (parentAccount != null) {
                                loadSubAccounts(parentAccount.id)
                            }
                        }
                    }
                }
        }
    }

    private fun loadSubAccounts(parentId: Long) {
        viewModelScope.launch {
            destinationAccountRepository.getSubAccounts(parentId)
                .collect { subs -> _state.update { it.copy(subAccounts = subs) } }
        }
    }

    fun setType(type: TransactionType) =
        _state.update { it.copy(
            type = type,
            selectedDestinationAccount = null, selectedSubAccount = null, subAccounts = emptyList(),
            toDepositAccount = null, toDepositError = false, sameAccountError = false
        ) }

    fun setToDepositAccount(account: DepositAccount) =
        _state.update { it.copy(toDepositAccount = account, toDepositError = false, sameAccountError = false) }

    fun setDepositAccount(account: DepositAccount) =
        _state.update { it.copy(selectedDepositAccount = account, depositError = false) }

    fun setDestinationAccount(account: DestinationAccount) {
        _state.update {
            it.copy(
                selectedDestinationAccount = account,
                selectedSubAccount = null,
                subAccounts = emptyList(),
                destinationError = false,
                subAccountError = false
            )
        }
        if (account.type == AccountType.INVESTMENT || account.type == AccountType.SAVINGS) loadSubAccounts(account.id)
    }

    fun setSubAccount(account: DestinationAccount) =
        _state.update { it.copy(selectedSubAccount = account, subAccountError = false) }

    fun setAmount(text: String) = _state.update { it.copy(amountText = text, amountError = false) }
    fun setDate(date: LocalDate) = _state.update { it.copy(date = date) }
    fun setDescription(text: String) = _state.update { it.copy(description = text) }

    fun submit() {
        val s = _state.value
        val amount = s.amountText.parseToCentavos() ?: 0L
        val hasAmountError = amount <= 0L
        val hasDepositError = s.selectedDepositAccount == null

        if (s.type == TransactionType.TRANSFER) {
            val hasSameAccount = s.selectedDepositAccount != null && s.toDepositAccount != null &&
                    s.selectedDepositAccount.id == s.toDepositAccount.id
            val hasToError = s.toDepositAccount == null
            if (hasAmountError || hasDepositError || hasToError || hasSameAccount) {
                _state.update { it.copy(
                    amountError = hasAmountError,
                    depositError = hasDepositError,
                    toDepositError = hasToError,
                    sameAccountError = hasSameAccount
                )}
                return
            }
            viewModelScope.launch {
                runCatching {
                    insertTransferUseCase(
                        userId = userId,
                        fromAccountId = s.selectedDepositAccount!!.id,
                        toAccountId = s.toDepositAccount!!.id,
                        amount = amount,
                        date = s.date,
                        description = s.description.ifBlank { null }
                    )
                }
                    .onSuccess { _state.update { it.copy(isSaved = true) } }
                    .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
            }
            return
        }

        val hasDestinationError = s.type == TransactionType.EXPENSE && s.selectedDestinationAccount == null
        val hasSubAccountError = s.type == TransactionType.EXPENSE && s.destinationNeedsSubAccount && s.selectedSubAccount == null

        if (hasAmountError || hasDepositError || hasDestinationError || hasSubAccountError) {
            _state.update {
                it.copy(
                    amountError = hasAmountError,
                    depositError = hasDepositError,
                    destinationError = hasDestinationError,
                    subAccountError = hasSubAccountError
                )
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                val tx = Transaction(
                    id = if (isEditMode) transactionId else 0,
                    userId = userId,
                    depositAccountId = s.selectedDepositAccount!!.id,
                    destinationAccountId = s.effectiveDestinationId,
                    type = s.type,
                    amount = amount,
                    date = s.date,
                    description = s.description.ifBlank { null },
                    transferGroupId = null
                )
                if (isEditMode) updateTransactionUseCase(tx) else insertTransactionUseCase(tx)
            }
                .onSuccess { _state.update { it.copy(isSaved = true) } }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}