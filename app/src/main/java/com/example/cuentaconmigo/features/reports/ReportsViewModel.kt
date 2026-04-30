package com.example.cuentaconmigo.features.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.ReportState
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.usecase.GetReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class DateFilter { ALL_TIME, THIS_MONTH }

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val destinationAccountRepository: DestinationAccountRepository,
    private val getReport: GetReportUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    private val _dateFilter = MutableStateFlow(DateFilter.ALL_TIME)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    private val _investmentAccountId = MutableStateFlow(-1L)

    val reportState: StateFlow<ReportState> = combine(
        _dateFilter, _investmentAccountId
    ) { filter, accountId -> filter to accountId }
        .flatMapLatest { (filter, accountId) ->
            val today = LocalDate.now()
            val (startDay, endDay) = when (filter) {
                DateFilter.ALL_TIME -> 0L to today.toEpochDay()
                DateFilter.THIS_MONTH -> today.withDayOfMonth(1).toEpochDay() to today.toEpochDay()
            }
            getReport(userId, startDay, endDay, accountId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportState(isLoading = true))

    init {
        viewModelScope.launch {
            _investmentAccountId.value =
                destinationAccountRepository.getInvestmentAccount(userId)?.id ?: -1L
        }
    }

    fun setDateFilter(filter: DateFilter) { _dateFilter.value = filter }
}
