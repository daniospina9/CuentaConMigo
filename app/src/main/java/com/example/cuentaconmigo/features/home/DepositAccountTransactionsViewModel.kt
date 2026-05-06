package com.example.cuentaconmigo.features.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.repository.AssetOperationRepository
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.InvestmentFluctuationRepository
import com.example.cuentaconmigo.domain.repository.SavingsMovementRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import com.example.cuentaconmigo.domain.usecase.DeleteTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DepositTransactionItem(
    val transaction: Transaction,
    val destinationName: String?,
    val isTransfer: Boolean
)

@HiltViewModel
class DepositAccountTransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val destinationAccountRepository: DestinationAccountRepository,
    private val investmentFluctuationRepository: InvestmentFluctuationRepository,
    private val assetOperationRepository: AssetOperationRepository,
    private val savingsMovementRepository: SavingsMovementRepository,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val depositAccountId: Long = checkNotNull(savedStateHandle["depositAccountId"])

    val items: StateFlow<List<DepositTransactionItem>> = combine(
        transactionRepository.getAllByDepositAccount(depositAccountId),
        destinationAccountRepository.getByUser(userId)
    ) { transactions, accounts ->
        val nameById = accounts.associateBy({ it.id }, { it.name })
        transactions.map { tx ->
            DepositTransactionItem(
                transaction = tx,
                destinationName = tx.destinationAccountId?.let { nameById[it] },
                isTransfer = tx.transferGroupId != null
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var pendingDelete: DepositTransactionItem? = null

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    fun requestDelete(item: DepositTransactionItem) {
        pendingDelete = item
        _showDeleteConfirm.value = true
    }

    fun cancelDelete() {
        pendingDelete = null
        _showDeleteConfirm.value = false
    }

    fun confirmDelete() {
        val item = pendingDelete ?: return
        viewModelScope.launch {
            val groupId = item.transaction.transferGroupId
            if (item.isTransfer && groupId != null) {
                transactionRepository.deleteTransfer(groupId)
                investmentFluctuationRepository.deleteByWithdrawalGroupId(groupId)
                assetOperationRepository.deleteByWithdrawalGroupId(groupId)
                savingsMovementRepository.deleteByGroupId(groupId)
            } else {
                deleteTransactionUseCase(item.transaction)
            }
            pendingDelete = null
            _showDeleteConfirm.value = false
        }
    }
}