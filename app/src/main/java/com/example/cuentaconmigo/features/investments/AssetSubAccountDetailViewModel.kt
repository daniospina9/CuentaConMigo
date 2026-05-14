package com.example.cuentaconmigo.features.investments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.AssetLiability
import com.example.cuentaconmigo.domain.model.AssetOperation
import com.example.cuentaconmigo.domain.model.AssetOperationType
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.AssetLiabilityRepository
import com.example.cuentaconmigo.domain.repository.AssetOperationRepository
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import com.example.cuentaconmigo.domain.usecase.GetDepositAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class AssetHistoryItem {
    data class OperationItem(val operation: AssetOperation) : AssetHistoryItem()
    data class DepositItem(val transaction: Transaction) : AssetHistoryItem()
}

@HiltViewModel
class AssetSubAccountDetailViewModel @Inject constructor(
    private val destinationAccountRepository: DestinationAccountRepository,
    private val assetOperationRepository: AssetOperationRepository,
    private val assetLiabilityRepository: AssetLiabilityRepository,
    private val transactionRepository: TransactionRepository,
    private val getDepositAccountsUseCase: GetDepositAccountsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val subAccountId: Long = checkNotNull(savedStateHandle["subAccountId"])

    private val _account = MutableStateFlow<DestinationAccount?>(null)
    val account: StateFlow<DestinationAccount?> = _account.asStateFlow()

    // Valor del activo = assetInitialValue + sum(assetValueDelta)
    val assetValue: StateFlow<Long> = combine(
        _account.filterNotNull(),
        assetOperationRepository.getAssetValueDeltaSum(subAccountId)
    ) { acct, delta -> acct.assetInitialValue + delta }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    // Balance = gastos de depósito hacia esta cuenta + balanceEffect de operaciones
    val accountBalance: StateFlow<Long> = combine(
        transactionRepository.getTotalExpensesForAccountFlow(subAccountId),
        assetOperationRepository.getBalanceEffectSum(subAccountId)
    ) { deposits, ops -> deposits + ops }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val pendingLiabilities: StateFlow<List<AssetLiability>> =
        assetLiabilityRepository.getPendingBySubAccount(subAccountId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Historial combinado: operaciones + transacciones de depósito, ordenado por fecha desc
    val history: StateFlow<List<AssetHistoryItem>> = combine(
        assetOperationRepository.getBySubAccount(subAccountId),
        transactionRepository.getByDestinationAccountAll(subAccountId)
    ) { ops, txs ->
        val opItems = ops.map { AssetHistoryItem.OperationItem(it) }
        val txItems = txs.map { AssetHistoryItem.DepositItem(it) }
        (opItems + txItems).sortedByDescending {
            when (it) {
                is AssetHistoryItem.OperationItem -> it.operation.date
                is AssetHistoryItem.DepositItem -> it.transaction.date
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val depositAccounts: StateFlow<List<DepositAccount>> =
        getDepositAccountsUseCase(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _account.value = destinationAccountRepository.getById(subAccountId)
        }
    }

    fun updateInitialValue(amount: Long) {
        val acct = _account.value ?: return
        viewModelScope.launch {
            runCatching { destinationAccountRepository.update(acct.copy(assetInitialValue = amount)) }
                .onSuccess { _account.value = acct.copy(assetInitialValue = amount) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun invest(totalSpent: Long, assetValueIncrease: Long, description: String?) {
        viewModelScope.launch {
            runCatching {
                assetOperationRepository.insert(
                    AssetOperation(
                        id = 0, userId = userId, subAccountId = subAccountId,
                        type = AssetOperationType.INVEST,
                        date = LocalDate.now(),
                        balanceEffect = -totalSpent,
                        assetValueDelta = assetValueIncrease,
                        description = description?.ifBlank { null }
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun recordAssetIncome(amount: Long, assetValueDelta: Long, description: String?) {
        viewModelScope.launch {
            runCatching {
                assetOperationRepository.insert(
                    AssetOperation(
                        id = 0, userId = userId, subAccountId = subAccountId,
                        type = AssetOperationType.ASSET_INCOME,
                        date = LocalDate.now(),
                        balanceEffect = amount,
                        assetValueDelta = assetValueDelta,
                        description = description?.ifBlank { null }
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun createLiability(description: String, amount: Long) {
        viewModelScope.launch {
            runCatching {
                assetLiabilityRepository.insert(
                    AssetLiability(
                        id = 0, userId = userId, subAccountId = subAccountId,
                        description = description, amount = amount,
                        createdDate = LocalDate.now(), isPaid = false
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun payLiability(liability: AssetLiability) {
        viewModelScope.launch {
            runCatching {
                assetOperationRepository.insert(
                    AssetOperation(
                        id = 0, userId = userId, subAccountId = subAccountId,
                        type = AssetOperationType.LIABILITY_PAYMENT,
                        date = LocalDate.now(),
                        balanceEffect = -liability.amount,
                        assetValueDelta = 0L,
                        description = "Pago: ${liability.description}",
                        liabilityId = liability.id
                    )
                )
                assetLiabilityRepository.update(liability.copy(isPaid = true))
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun withdraw(toDepositAccountId: Long, amount: Long, description: String?) {
        viewModelScope.launch {
            runCatching {
                val groupId = java.util.UUID.randomUUID().toString()
                val accountName = _account.value?.name ?: "Activo"
                val desc = description?.ifBlank { null } ?: "Retiro de $accountName"
                assetOperationRepository.insert(
                    AssetOperation(
                        id = 0, userId = userId, subAccountId = subAccountId,
                        type = AssetOperationType.WITHDRAWAL,
                        date = LocalDate.now(),
                        balanceEffect = -amount,
                        assetValueDelta = 0L,
                        description = desc,
                        withdrawalGroupId = groupId
                    )
                )
                transactionRepository.insert(
                    Transaction(
                        id = 0, userId = userId,
                        depositAccountId = toDepositAccountId,
                        destinationAccountId = null,
                        type = TransactionType.INCOME,
                        amount = amount, date = LocalDate.now(),
                        description = desc, transferGroupId = groupId
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteOperation(op: AssetOperation) {
        viewModelScope.launch {
            runCatching {
                assetOperationRepository.delete(op)
                op.withdrawalGroupId?.let { transactionRepository.deleteTransfer(it) }
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteDeposit(transaction: Transaction) {
        viewModelScope.launch {
            runCatching { transactionRepository.delete(transaction) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteLiability(liability: AssetLiability) {
        viewModelScope.launch {
            runCatching { assetLiabilityRepository.delete(liability) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}