package com.endfield.time.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endfield.time.data.MilestoneRepository
import com.endfield.time.domain.DayCounter
import com.endfield.time.domain.MilestoneUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DetailUiState(
    val milestone: MilestoneUi? = null,
    val loading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModel(
    private val id: Long,
    repository: MilestoneRepository,
    todayFlow: kotlinx.coroutines.flow.MutableStateFlow<LocalDate>
) : ViewModel() {

    val ui: StateFlow<DetailUiState> =
        combine(repository.observe(MilestoneRepository.ALL_CATEGORY), todayFlow) { list, today ->
            DetailUiState(
                milestone = list.firstOrNull { it.id == id }?.let { DayCounter.toUi(it, today) },
                loading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState()
        )
}
