package com.example.cuentaconmigo.features.investments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.InvestmentFluctuation
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.InvestmentFluctuationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val destinationAccountRepository: DestinationAccountRepository,
    private val investmentFluctuationRepository: InvestmentFluctuationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    private val _investmentAccountId = MutableStateFlow<Long?>(null)

    val fluctuations: StateFlow<List<InvestmentFluctuation>> = _investmentAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(emptyList())
            else investmentFluctuationRepository.getByAccount(accountId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val balance: StateFlow<Long> = _investmentAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(0L)
            else investmentFluctuationRepository.getBalance(userId, accountId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _investmentAccountId.value = destinationAccountRepository.getInvestmentAccount(userId)?.id
        }
    }

    fun addFluctuation(amount: Long, description: String?) {
        val accountId = _investmentAccountId.value ?: return
        viewModelScope.launch {
            runCatching {
                investmentFluctuationRepository.insert(
                    InvestmentFluctuation(
                        id = 0,
                        userId = userId,
                        destinationAccountId = accountId,
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
            runCatching { investmentFluctuationRepository.delete(fluctuation) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}
