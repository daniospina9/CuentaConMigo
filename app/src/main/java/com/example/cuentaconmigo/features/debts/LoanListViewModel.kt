package com.example.cuentaconmigo.features.debts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.SimpleDebt
import com.example.cuentaconmigo.domain.model.SimpleDebtKind
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import com.example.cuentaconmigo.domain.usecase.simple_debt.CreateSimpleDebtUseCase
import com.example.cuentaconmigo.domain.usecase.simple_debt.DeleteSimpleDebtUseCase
import com.example.cuentaconmigo.domain.usecase.simple_debt.GetSimpleDebtsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LoanListViewModel @Inject constructor(
    private val getSimpleDebtsUseCase: GetSimpleDebtsUseCase,
    private val createSimpleDebtUseCase: CreateSimpleDebtUseCase,
    private val deleteSimpleDebtUseCase: DeleteSimpleDebtUseCase,
    private val simpleDebtRepository: SimpleDebtRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userId: Long = checkNotNull(savedStateHandle["userId"])

    val loans: StateFlow<List<SimpleDebt>> =
        getSimpleDebtsUseCase(userId, SimpleDebtKind.GIVEN)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val loansMap: StateFlow<Map<Long, Long>> =
        loans.flatMapLatest { list ->
            if (list.isEmpty()) flowOf(emptyMap())
            else combine(
                list.map { loan ->
                    simpleDebtRepository.getCurrentDebt(loan.id)
                        .map { amount -> loan.id to amount }
                }
            ) { pairs -> pairs.toMap() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun createLoan(name: String, description: String?) {
        viewModelScope.launch {
            runCatching {
                createSimpleDebtUseCase(
                    SimpleDebt(
                        id = 0,
                        userId = userId,
                        name = name,
                        description = description?.ifBlank { null },
                        createdAt = System.currentTimeMillis(),
                        isActive = true,
                        kind = SimpleDebtKind.GIVEN
                    )
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteLoan(loan: SimpleDebt) {
        viewModelScope.launch {
            runCatching { deleteSimpleDebtUseCase(loan) }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}
