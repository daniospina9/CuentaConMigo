package com.example.cuentaconmigo.features.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.domain.model.ReportState
import com.example.cuentaconmigo.domain.usecase.GetReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

enum class DateFilter { ALL_TIME, THIS_MONTH }

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getReport: GetReportUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    private val _dateFilter = MutableStateFlow(DateFilter.ALL_TIME)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    val reportState: StateFlow<ReportState> = _dateFilter
        .flatMapLatest { filter ->
            val (startDay, endDay) = rangeFor(filter)
            getReport(userId, startDay, endDay)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportState(isLoading = true))

    fun setDateFilter(filter: DateFilter) { _dateFilter.value = filter }

    fun currentDateRange(): Pair<Long, Long> = rangeFor(_dateFilter.value)

    fun getUserId(): Long = userId

    private fun rangeFor(filter: DateFilter): Pair<Long, Long> {
        val today = LocalDate.now()
        return when (filter) {
            DateFilter.ALL_TIME -> 0L to today.toEpochDay()
            DateFilter.THIS_MONTH -> today.withDayOfMonth(1).toEpochDay() to today.toEpochDay()
        }
    }
}
