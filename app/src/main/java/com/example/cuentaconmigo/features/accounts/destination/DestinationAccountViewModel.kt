package com.example.cuentaconmigo.features.accounts.destination

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.usecase.CreateDestinationAccountUseCase
import com.example.cuentaconmigo.domain.usecase.DeleteDestinationAccountUseCase
import com.example.cuentaconmigo.domain.usecase.GetDestinationAccountsUseCase
import com.example.cuentaconmigo.domain.usecase.UpdateDestinationAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DestinationAccountViewModel @Inject constructor(
    private val getDestinationAccountsUseCase: GetDestinationAccountsUseCase,
    private val createDestinationAccountUseCase: CreateDestinationAccountUseCase,
    private val updateDestinationAccountUseCase: UpdateDestinationAccountUseCase,
    private val deleteDestinationAccountUseCase: DeleteDestinationAccountUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    val accounts: StateFlow<List<DestinationAccount>> = getDestinationAccountsUseCase(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun createAccount(name: String, type: AccountType) {
        viewModelScope.launch {
            runCatching {
                createDestinationAccountUseCase(
                    DestinationAccount(id = 0, userId = userId, name = name.trim(), type = type, isDefault = false)
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun updateAccount(account: DestinationAccount, newName: String) {
        viewModelScope.launch {
            runCatching {
                updateDestinationAccountUseCase(account.copy(name = newName.trim()))
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteAccount(account: DestinationAccount) {
        viewModelScope.launch {
            deleteDestinationAccountUseCase(account)
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}