package com.example.cuentaconmigo.features.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.User
import com.example.cuentaconmigo.domain.repository.UserRepository
import com.example.cuentaconmigo.domain.usecase.SeedDefaultAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSelectorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val seedDefaultAccounts: SeedDefaultAccountsUseCase
) : ViewModel() {

    val users: StateFlow<List<User>> = userRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _activeUser = MutableStateFlow<User?>(null)
    val activeUser: StateFlow<User?> = _activeUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectUser(user: User) {
        _activeUser.value = user
    }

    fun createUser(name: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            userRepository.createUser(name)
                .onSuccess { id ->
                    seedDefaultAccounts(id)
                    val created = userRepository.getUserById(id) ?: return@onSuccess
                    _activeUser.value = created
                    onSuccess(created)
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearError() { _errorMessage.value = null }
}
