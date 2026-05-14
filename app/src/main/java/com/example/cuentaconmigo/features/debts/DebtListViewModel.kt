package com.example.cuentaconmigo.features.debts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.MinPaymentType
import com.example.cuentaconmigo.domain.usecase.credit_card.CreateCreditCardUseCase
import com.example.cuentaconmigo.domain.usecase.credit_card.DeleteCreditCardUseCase
import com.example.cuentaconmigo.domain.usecase.credit_card.GetCreditCardsUseCase
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtListViewModel @Inject constructor(
    private val getCreditCardsUseCase: GetCreditCardsUseCase,
    private val createCreditCardUseCase: CreateCreditCardUseCase,
    private val deleteCreditCardUseCase: DeleteCreditCardUseCase,
    private val creditCardRepository: CreditCardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    val cards: StateFlow<List<CreditCard>> =
        getCreditCardsUseCase(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val debtsMap: StateFlow<Map<Long, Long>> =
        cards.flatMapLatest { list ->
            if (list.isEmpty()) flowOf(emptyMap())
            else combine(
                list.map { card ->
                    creditCardRepository.getCurrentDebt(card.id)
                        .map { debt -> card.id to debt }
                }
            ) { pairs -> pairs.toMap() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun createCard(
        name: String,
        lastFourDigits: String?,
        creditLimit: Long,
        interestRateAnnual: Double,
        cutOffDay: Int,
        paymentDueDay: Int,
        minPaymentType: MinPaymentType,
        minPaymentPercent: Double,
        minPaymentFixed: Long,
        monthlyFee: Long = 0L
    ) {
        viewModelScope.launch {
            runCatching {
                createCreditCardUseCase(
                    CreditCard(
                        id = 0,
                        userId = userId,
                        name = name,
                        lastFourDigits = lastFourDigits?.ifBlank { null },
                        creditLimit = creditLimit,
                        interestRateAnnual = interestRateAnnual,
                        cutOffDay = cutOffDay,
                        paymentDueDay = paymentDueDay,
                        minPaymentType = minPaymentType,
                        minPaymentPercent = minPaymentPercent,
                        minPaymentFixed = minPaymentFixed,
                        monthlyFee = monthlyFee,
                        isActive = true
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteCard(card: CreditCard) {
        viewModelScope.launch {
            runCatching { deleteCreditCardUseCase(card) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}