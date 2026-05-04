package com.example.cuentaconmigo.features.accounts.deposit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.usecase.CreateDepositAccountUseCase
import com.example.cuentaconmigo.domain.usecase.DeleteDepositAccountUseCase
import com.example.cuentaconmigo.domain.usecase.GetDepositAccountsUseCase
import com.example.cuentaconmigo.domain.usecase.UpdateDepositAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepositAccountViewModel @Inject constructor(
    private val getDepositAccountsUseCase: GetDepositAccountsUseCase,
    private val createDepositAccountUseCase: CreateDepositAccountUseCase,
    private val updateDepositAccountUseCase: UpdateDepositAccountUseCase,
    private val deleteDepositAccountUseCase: DeleteDepositAccountUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    val accounts: StateFlow<List<DepositAccount>> = getDepositAccountsUseCase(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun createAccount(name: String) {
        viewModelScope.launch {
            runCatching {
                createDepositAccountUseCase(DepositAccount(id = 0, userId = userId, name = name.trim()))
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun updateAccount(account: DepositAccount, newName: String) {
        viewModelScope.launch {
            runCatching {
                updateDepositAccountUseCase(account.copy(name = newName.trim()))
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteAccount(account: DepositAccount) {
        viewModelScope.launch {
            deleteDepositAccountUseCase(account)
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}