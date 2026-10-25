package com.endfield.time.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endfield.time.data.MilestoneRepository
import com.endfield.time.data.prefs.SettingsStore
import com.endfield.time.domain.DayCounter
import com.endfield.time.domain.MilestoneUi
import com.endfield.time.ui.UiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val categories: List<String> = MilestoneRepository.DEFAULT_CATEGORIES,
    val counts: Map<String, Int> = emptyMap(),
    val selectedCategory: String = MilestoneRepository.ALL_CATEGORY,
    val milestones: List<MilestoneUi> = emptyList(),
    val totalCount: Int = 0
) {
    val isEmpty: Boolean get() = milestones.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: MilestoneRepository,
    private val settingsStore: SettingsStore,
    private val todayFlow: MutableStateFlow<LocalDate>
) : ViewModel() {

    private val filterFlow = MutableStateFlow(MilestoneRepository.ALL_CATEGORY)

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private val countsFlow = repository.observeCategoryCounts()

    val ui: StateFlow<HomeUiState> =
        filterFlow
            .flatMapLatest { category ->
                combine(
                    repository.observe(category),
                    repository.observeCategories(),
                    countsFlow,
                    todayFlow
                ) { list, usedCategories, counts, today ->
                    val categoryList = (MilestoneRepository.DEFAULT_CATEGORIES + usedCategories).distinct()
                    HomeUiState(
                        categories = categoryList,
                        counts = counts.associate { it.category to it.count },
                        selectedCategory = category,
                        milestones = list.map { DayCounter.toUi(it, today) },
                        totalCount = list.size
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState()
            )

    init {
        viewModelScope.launch {
            val saved = settingsStore.selectedCategory.first()
            if (!saved.isNullOrBlank() && saved != filterFlow.value) {
                filterFlow.value = saved
            }
        }
    }

    /** 选择分类；传「全部」即重置过滤 */
    fun selectCategory(category: String) {
        if (category == filterFlow.value) return
        filterFlow.value = category
        viewModelScope.launch { settingsStore.setSelectedCategory(category) }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            val affected = repository.deleteById(id)
            _events.tryEmit(
                UiEvent.ShowToast(if (affected > 0) "已删除" else "删除失败，记录可能已不存在")
            )
        }
    }

    fun onResume() {
        val today = LocalDate.now()
        if (todayFlow.value != today) todayFlow.value = today
    }
}

/** 供筛选面板展示用的分类计数 */
data class CategoryEntry(val name: String, val count: Int)

fun HomeUiState.categoryEntries(): List<CategoryEntry> =
    categories.map { CategoryEntry(it, counts[it] ?: 0) }
