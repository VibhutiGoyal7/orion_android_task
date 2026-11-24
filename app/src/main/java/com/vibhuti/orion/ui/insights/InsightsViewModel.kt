package com.vibhuti.orion.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vibhuti.orion.OrionApp
import com.vibhuti.orion.data.repository.DaySummary
import com.vibhuti.orion.data.repository.InsightsRepository
import com.vibhuti.orion.data.repository.NightInsight
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface InsightsUiState {
    data object Loading : InsightsUiState
    data class Error(val message: String) : InsightsUiState
    data class Success(
        val days: List<DaySummary>,
        val selectedDate: LocalDate,
        val night: NightState,
    ) : InsightsUiState
}

/** Content for the selected day, loaded independently of the date strip. */
sealed interface NightState {
    /** [previous] keeps the last shown night on screen (dimmed) while the next one loads. */
    data class Loading(val previous: NightInsight? = null) : NightState
    data class Error(val message: String) : NightState
    data class Loaded(val insight: NightInsight) : NightState
}

class InsightsViewModel(
    private val repository: InsightsRepository,
    private val clock: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private var nightJob: Job? = null

    /** Captured once per [load] so the strip and every night request agree, even across midnight. */
    private var today: LocalDate = LocalDate.MIN

    init { load() }

    fun load() {
        nightJob?.cancel()
        _uiState.value = InsightsUiState.Loading
        viewModelScope.launch {
            val now = clock().also { today = it }
            repository.getDays(now).fold(
                onSuccess = { days ->
                    val selected = days.lastOrNull { it.score != null }?.date ?: now
                    _uiState.value = InsightsUiState.Success(days, selected, NightState.Loading())
                    loadNight(selected)
                },
                onFailure = { _uiState.value = InsightsUiState.Error(it.message ?: "Couldn't load your insights") },
            )
        }
    }

    /** Ignores days without data (future / untracked) and re-selecting the current day. */
    fun selectDate(date: LocalDate) {
        val state = _uiState.value as? InsightsUiState.Success ?: return
        if (date == state.selectedDate) return
        if (state.days.none { it.date == date && it.score != null }) return
        _uiState.value = state.copy(selectedDate = date, night = NightState.Loading(state.night.shownInsight()))
        loadNight(date)
    }

    fun retryNight() {
        val state = _uiState.value as? InsightsUiState.Success ?: return
        _uiState.value = state.copy(night = NightState.Loading(state.night.shownInsight()))
        loadNight(state.selectedDate)
    }

    private fun loadNight(date: LocalDate) {
        nightJob?.cancel()
        nightJob = viewModelScope.launch {
            val night = repository.getNight(date, today).fold(
                onSuccess = { NightState.Loaded(it) },
                onFailure = { NightState.Error(it.message ?: "Couldn't load this night") },
            )
            _uiState.update { state ->
                if (state is InsightsUiState.Success && state.selectedDate == date) state.copy(night = night) else state
            }
        }
    }

    private fun NightState.shownInsight(): NightInsight? = when (this) {
        is NightState.Loaded -> insight
        is NightState.Loading -> previous
        is NightState.Error -> null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OrionApp
                InsightsViewModel(app.container.insightsRepository)
            }
        }
    }
}
