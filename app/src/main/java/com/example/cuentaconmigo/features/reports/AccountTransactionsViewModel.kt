package com.example.cuentaconmigo.features.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AccountTransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val destinationAccountId: Long = checkNotNull(savedStateHandle["destinationAccountId"])
    private val startDay: Long = checkNotNull(savedStateHandle["startDay"])
    private val endDay: Long = checkNotNull(savedStateHandle["endDay"])

    val transactions: StateFlow<List<Transaction>> =
        transactionRepository.getByDestinationAccount(destinationAccountId, startDay, endDay)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
