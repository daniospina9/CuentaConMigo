package com.example.cuentaconmigo.features.accounts.destination

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.AccountType
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DestinationAccountViewModel @Inject constructor(
    private val repository: DestinationAccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    val accounts: StateFlow<List<DestinationAccount>> = repository.getByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun createAccount(name: String, type: AccountType) {
        viewModelScope.launch {
            runCatching {
                repository.create(
                    DestinationAccount(id = 0, userId = userId, name = name.trim(), type = type, isDefault = false)
                )
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun updateAccount(account: DestinationAccount, newName: String) {
        viewModelScope.launch {
            runCatching {
                repository.update(account.copy(name = newName.trim()))
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteAccount(account: DestinationAccount) {
        viewModelScope.launch {
            repository.delete(account)
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}