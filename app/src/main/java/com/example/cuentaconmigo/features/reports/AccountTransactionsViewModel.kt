package com.example.cuentaconmigo.features.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import com.example.cuentaconmigo.domain.usecase.DeleteTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountTransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val destinationAccountId: Long = checkNotNull(savedStateHandle["destinationAccountId"])
    private val startDay: Long = checkNotNull(savedStateHandle["startDay"])
    private val endDay: Long = checkNotNull(savedStateHandle["endDay"])

    val transactions: StateFlow<List<Transaction>> =
        transactionRepository.getByDestinationAccount(destinationAccountId, startDay, endDay)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var pendingDelete: Transaction? = null

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    fun requestDelete(tx: Transaction) {
        pendingDelete = tx
        _showDeleteConfirm.value = true
    }

    fun cancelDelete() {
        pendingDelete = null
        _showDeleteConfirm.value = false
    }

    fun confirmDelete() {
        val tx = pendingDelete ?: return
        viewModelScope.launch {
            deleteTransactionUseCase(tx)
            pendingDelete = null
            _showDeleteConfirm.value = false
        }
    }
}
