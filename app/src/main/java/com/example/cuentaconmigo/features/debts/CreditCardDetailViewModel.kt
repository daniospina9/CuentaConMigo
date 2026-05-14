package com.example.cuentaconmigo.features.debts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.MinPaymentType
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import kotlinx.coroutines.Job
import com.example.cuentaconmigo.domain.usecase.credit_card.DeleteCreditCardTransactionUseCase
import com.example.cuentaconmigo.domain.usecase.credit_card.GetCreditCardDetailUseCase
import com.example.cuentaconmigo.domain.usecase.credit_card.RegisterPaymentUseCase
import com.example.cuentaconmigo.domain.usecase.credit_card.RegisterPurchaseUseCase
import com.example.cuentaconmigo.domain.usecase.credit_card.UpdateCreditCardTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditCardDetailViewModel @Inject constructor(
    private val getCreditCardDetailUseCase: GetCreditCardDetailUseCase,
    private val registerPurchaseUseCase: RegisterPurchaseUseCase,
    private val registerPaymentUseCase: RegisterPaymentUseCase,
    private val deleteCreditCardTransactionUseCase: DeleteCreditCardTransactionUseCase,
    private val updateCreditCardTransactionUseCase: UpdateCreditCardTransactionUseCase,
    private val depositAccountRepository: DepositAccountRepository,
    private val destinationAccountRepository: DestinationAccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val cardId: Long = checkNotNull(savedStateHandle["creditCardId"])

    val card: StateFlow<CreditCard?> =
        getCreditCardDetailUseCase.getCard(cardId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentDebt: StateFlow<Long> =
        getCreditCardDetailUseCase.getCurrentDebt(cardId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val transactions: StateFlow<List<CreditCardTransaction>> =
        getCreditCardDetailUseCase.getTransactions(cardId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val depositAccounts: StateFlow<List<DepositAccount>> =
        depositAccountRepository.getByUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val destinationAccounts: StateFlow<List<DestinationAccount>> =
        destinationAccountRepository.getByUser(userId)
            .map { list -> list.filter { it.type != AccountType.SAVINGS } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var subAccountJob: Job? = null
    private val _purchaseSubAccounts = MutableStateFlow<List<DestinationAccount>>(emptyList())
    val purchaseSubAccounts: StateFlow<List<DestinationAccount>> = _purchaseSubAccounts.asStateFlow()

    fun setPurchaseParent(account: DestinationAccount?) {
        subAccountJob?.cancel()
        _purchaseSubAccounts.value = emptyList()
        if (account?.type == AccountType.INVESTMENT) {
            subAccountJob = viewModelScope.launch {
                destinationAccountRepository.getSubAccounts(account.id).collect {
                    _purchaseSubAccounts.value = it
                }
            }
        }
    }

    val availableCredit: Long
        get() = (card.value?.creditLimit ?: 0L) - currentDebt.value

    val tem: Double
        get() = card.value?.let {
            Math.pow(1.0 + it.interestRateAnnual / 100.0, 1.0 / 12.0) - 1.0
        } ?: 0.0

    val minPaymentAmount: Long
        get() {
            val c = card.value ?: return 0L
            val interest = (currentDebt.value * tem).toLong()
            return when (c.minPaymentType) {
                MinPaymentType.PERCENTAGE -> maxOf(interest, (currentDebt.value * c.minPaymentPercent / 100.0).toLong())
                MinPaymentType.FIXED -> maxOf(interest, c.minPaymentFixed)
            }
        }

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun registerPurchase(
        amount: Long,
        description: String?,
        destinationAccountId: Long?,
        date: Long,
        installments: Int = 1
    ) {
        viewModelScope.launch {
            runCatching {
                registerPurchaseUseCase(
                    creditCardId = cardId,
                    userId = userId,
                    amount = amount,
                    description = description,
                    destinationAccountId = destinationAccountId,
                    date = date,
                    installments = installments
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun registerCharge(
        amount: Long,
        description: String,
        type: CreditCardTransactionType,
        date: Long
    ) {
        viewModelScope.launch {
            runCatching {
                registerPurchaseUseCase(
                    creditCardId = cardId,
                    userId = userId,
                    amount = amount,
                    description = description,
                    destinationAccountId = null,
                    date = date,
                    type = type,
                    installments = 1
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun registerPayment(
        amount: Long,
        depositAccountId: Long,
        date: Long
    ) {
        viewModelScope.launch {
            runCatching {
                registerPaymentUseCase(
                    creditCardId = cardId,
                    cardName = card.value?.name ?: "Tarjeta",
                    userId = userId,
                    amount = amount,
                    depositAccountId = depositAccountId,
                    date = date
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteTransaction(tx: CreditCardTransaction) {
        viewModelScope.launch {
            runCatching { deleteCreditCardTransactionUseCase(tx) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun updateTransaction(tx: CreditCardTransaction) {
        viewModelScope.launch {
            runCatching { updateCreditCardTransactionUseCase(tx) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}
