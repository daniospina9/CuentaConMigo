package com.example.cuentaconmigo.features.savings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.SavingsMovement
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import kotlinx.coroutines.flow.combine
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.SavingsMovementRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SavingsSubAccountDetailViewModel @Inject constructor(
    private val savingsMovementRepository: SavingsMovementRepository,
    private val transactionRepository: TransactionRepository,
    private val destinationAccountRepository: DestinationAccountRepository,
    private val depositAccountRepository: DepositAccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val subAccountId: Long = checkNotNull(savedStateHandle["subAccountId"])

    private val _account = MutableStateFlow<DestinationAccount?>(null)
    val account: StateFlow<DestinationAccount?> = _account.asStateFlow()

    val movements: StateFlow<List<SavingsMovement>> =
        savingsMovementRepository.getBySubAccount(subAccountId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val balance: StateFlow<Long> =
        combine(
            savingsMovementRepository.getBalance(subAccountId),
            transactionRepository.getTotalExpensesForAccountFlow(subAccountId)
        ) { withdrawals, deposits -> deposits + withdrawals }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val depositAccounts: StateFlow<List<DepositAccount>> =
        depositAccountRepository.getByUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _account.value = destinationAccountRepository.getById(subAccountId)
        }
    }

    fun withdraw(amount: Long, depositAccountId: Long, description: String?) {
        viewModelScope.launch {
            runCatching {
                val groupId = UUID.randomUUID().toString()
                transactionRepository.insert(
                    Transaction(
                        id = 0, userId = userId, depositAccountId = depositAccountId,
                        destinationAccountId = null, type = TransactionType.INCOME,
                        amount = amount, date = LocalDate.now(),
                        description = description, transferGroupId = groupId
                    )
                )
                savingsMovementRepository.insert(
                    SavingsMovement(
                        id = 0, userId = userId, subAccountId = subAccountId,
                        amount = -amount, date = LocalDate.now(),
                        description = description, groupId = groupId
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteMovement(movement: SavingsMovement) {
        viewModelScope.launch {
            runCatching {
                movement.groupId?.let { transactionRepository.deleteTransfer(it) }
                savingsMovementRepository.delete(movement)
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}