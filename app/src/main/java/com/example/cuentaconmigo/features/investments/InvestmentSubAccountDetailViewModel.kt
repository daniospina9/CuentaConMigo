package com.example.cuentaconmigo.features.investments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.InvestmentFluctuation
import com.example.cuentaconmigo.domain.model.InvestmentSubtype
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.InvestmentFluctuationRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import com.example.cuentaconmigo.domain.usecase.DeleteTransactionUseCase
import com.example.cuentaconmigo.domain.usecase.GetDepositAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class LiquidEntry {
    abstract val date: LocalDate
    data class Fluctuation(val source: InvestmentFluctuation) : LiquidEntry() {
        override val date get() = source.date
    }
    data class Deposit(val source: Transaction) : LiquidEntry() {
        override val date get() = source.date
    }
}

@HiltViewModel
class InvestmentSubAccountDetailViewModel @Inject constructor(
    private val destinationAccountRepository: DestinationAccountRepository,
    private val investmentFluctuationRepository: InvestmentFluctuationRepository,
    private val transactionRepository: TransactionRepository,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getDepositAccountsUseCase: GetDepositAccountsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val subAccountId: Long = checkNotNull(savedStateHandle["subAccountId"])

    private val _account = MutableStateFlow<DestinationAccount?>(null)
    val account: StateFlow<DestinationAccount?> = _account.asStateFlow()

    val fluctuations: StateFlow<List<InvestmentFluctuation>> = _account
        .flatMapLatest { acct ->
            if (acct != null && acct.investmentSubtype != InvestmentSubtype.EXPENSE)
                investmentFluctuationRepository.getByAccount(acct.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val balance: StateFlow<Long> = _account
        .flatMapLatest { acct ->
            when (acct?.investmentSubtype) {
                InvestmentSubtype.LIQUID -> combine(
                    investmentFluctuationRepository.getBalance(userId, acct.id),
                    transactionRepository.getTotalExpensesForAccountFlow(acct.id)
                ) { fluctuations, expenses -> fluctuations + expenses }
                InvestmentSubtype.ASSET -> investmentFluctuationRepository.getBalance(userId, acct.id)
                else -> flowOf(0L)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalInvested: StateFlow<Long> = _account
        .flatMapLatest { acct ->
            if (acct != null) flow { emit(transactionRepository.getTotalInvestedInAccount(acct.id)) }
            else flowOf(0L)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val unrealizedGain: StateFlow<Long> = combine(balance, totalInvested) { bal, inv -> bal - inv }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val allEntries: StateFlow<List<LiquidEntry>> = _account
        .flatMapLatest { acct ->
            if (acct?.investmentSubtype == InvestmentSubtype.LIQUID) {
                combine(
                    investmentFluctuationRepository.getByAccount(acct.id),
                    transactionRepository.getByDestinationAccountAll(acct.id)
                ) { fluctuations, deposits ->
                    val flucItems = fluctuations.map { LiquidEntry.Fluctuation(it) }
                    val depItems = deposits.map { LiquidEntry.Deposit(it) }
                    (flucItems + depItems).sortedByDescending { it.date }
                }
            } else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val transactions: StateFlow<List<Transaction>> = _account
        .flatMapLatest { acct ->
            if (acct?.investmentSubtype == InvestmentSubtype.EXPENSE)
                transactionRepository.getByDestinationAccountAll(acct.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun addFluctuation(amount: Long, description: String?) {
        viewModelScope.launch {
            runCatching {
                investmentFluctuationRepository.insert(
                    InvestmentFluctuation(
                        id = 0,
                        userId = userId,
                        destinationAccountId = subAccountId,
                        amount = amount,
                        date = LocalDate.now(),
                        description = description?.ifBlank { null }
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteFluctuation(fluctuation: InvestmentFluctuation) {
        viewModelScope.launch {
            runCatching {
                investmentFluctuationRepository.delete(fluctuation)
                fluctuation.withdrawalGroupId?.let { transactionRepository.deleteTransfer(it) }
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteDeposit(transaction: Transaction) {
        viewModelScope.launch {
            runCatching { deleteTransactionUseCase(transaction) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun withdraw(toDepositAccountId: Long, amount: Long, description: String?) {
        viewModelScope.launch {
            runCatching {
                val groupId = java.util.UUID.randomUUID().toString()
                val accountName = _account.value?.name ?: "Inversión"
                val desc = description?.ifBlank { null } ?: "Retiro de $accountName"
                investmentFluctuationRepository.insert(
                    InvestmentFluctuation(
                        id = 0,
                        userId = userId,
                        destinationAccountId = subAccountId,
                        amount = -amount,
                        date = LocalDate.now(),
                        description = desc,
                        withdrawalGroupId = groupId
                    )
                )
                transactionRepository.insert(
                    Transaction(
                        id = 0,
                        userId = userId,
                        depositAccountId = toDepositAccountId,
                        destinationAccountId = null,
                        type = TransactionType.INCOME,
                        amount = amount,
                        date = LocalDate.now(),
                        description = desc,
                        transferGroupId = groupId
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}
