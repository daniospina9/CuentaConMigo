package com.example.cuentaconmigo.features.accounts.deposit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepositAccountViewModel @Inject constructor(
    private val repository: DepositAccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    val accounts: StateFlow<List<DepositAccount>> = repository.getByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun createAccount(name: String) {
        viewModelScope.launch {
            runCatching {
                repository.create(DepositAccount(id = 0, userId = userId, name = name.trim()))
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun updateAccount(account: DepositAccount, newName: String) {
        viewModelScope.launch {
            runCatching {
                repository.update(account.copy(name = newName.trim()))
            }.onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteAccount(account: DepositAccount) {
        viewModelScope.launch {
            repository.delete(account)
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}