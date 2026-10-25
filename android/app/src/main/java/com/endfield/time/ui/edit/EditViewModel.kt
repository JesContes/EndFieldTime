package com.endfield.time.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endfield.time.data.MilestoneRepository
import com.endfield.time.data.local.MilestoneEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class EditUiState(
    val isEdit: Boolean = false,
    val loading: Boolean = false,
    val content: String = "",
    val category: String = "",
    val detail: String = "",
    val dateMillis: Long? = null,
    val colorHex: String = MilestoneRepository.PRESET_COLORS.first(),
    val categories: List<String> = MilestoneRepository.DEFAULT_CATEGORIES,
    val saved: Boolean = false
)

sealed interface EditEvent {
    data class Toast(val text: String) : EditEvent
}

class EditViewModel(
    private val milestoneId: Long?,
    private val repository: MilestoneRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(
        EditUiState(
            isEdit = milestoneId != null,
            loading = milestoneId != null,
            dateMillis = todayMillis()
        )
    )
    val ui: StateFlow<EditUiState> = _ui.asStateFlow()

    private val _events = MutableSharedFlow<EditEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<EditEvent> = _events.asSharedFlow()

    private var originalCreatedAt: Long = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            repository.observeCategories().collect { used ->
                val all = (MilestoneRepository.DEFAULT_CATEGORIES + used).distinct()
                _ui.update { it.copy(categories = all) }
            }
        }
        if (milestoneId != null) {
            viewModelScope.launch {
                val entity = repository.getById(milestoneId)
                if (entity == null) {
                    _events.tryEmit(EditEvent.Toast("记录已不存在"))
                    _ui.update { it.copy(loading = false, saved = true) }
                } else {
                    originalCreatedAt = entity.createdAt
                    _ui.update {
                        it.copy(
                            loading = false,
                            content = entity.content,
                            category = entity.category,
                            detail = entity.detail,
                            dateMillis = epochDayToMillis(entity.targetDate),
                            colorHex = entity.colorHex
                        )
                    }
                }
            }
        }
    }

    fun onContentChange(value: String) = _ui.update { it.copy(content = value.take(CONTENT_MAX)) }

    fun onCategoryChange(value: String) = _ui.update { it.copy(category = value) }

    fun onDetailChange(value: String) = _ui.update { it.copy(detail = value.take(DETAIL_MAX)) }

    fun onDateChange(millis: Long?) = _ui.update { it.copy(dateMillis = millis) }

    fun onColorChange(hex: String) = _ui.update { it.copy(colorHex = hex) }

    fun save() {
        val state = _ui.value
        val content = state.content.trim()
        val category = state.category.trim()
        val detail = state.detail.trim()
        val dateMillis = state.dateMillis

        when {
            content.isEmpty() -> return toast("内容不能为空")
            content.length > CONTENT_MAX -> return toast("内容不能超过 $CONTENT_MAX 字")
            category.isEmpty() -> return toast("请选择分类")
            category.length > CATEGORY_MAX -> return toast("分类名需为 1~$CATEGORY_MAX 字")
            dateMillis == null -> return toast("请选择目标日期")
            detail.length > DETAIL_MAX -> return toast("内容详情不能超过 $DETAIL_MAX 字")
        }

        viewModelScope.launch {
            val entity = MilestoneEntity(
                id = milestoneId ?: 0L,
                content = content,
                category = category,
                targetDate = millisToEpochDay(dateMillis!!),
                colorHex = state.colorHex,
                detail = detail,
                createdAt = originalCreatedAt,
                sortOrder = 0
            )
            try {
                if (milestoneId == null) {
                    repository.insert(entity)
                    _events.tryEmit(EditEvent.Toast("保存成功"))
                } else {
                    val affected = repository.update(entity)
                    if (affected == 0) {
                        _events.tryEmit(EditEvent.Toast("记录已不存在"))
                    } else {
                        _events.tryEmit(EditEvent.Toast("已保存"))
                    }
                }
                _ui.update { it.copy(saved = true) }
            } catch (e: Exception) {
                _events.tryEmit(EditEvent.Toast("保存失败，请重试"))
            }
        }
    }

    private fun toast(text: String) {
        _events.tryEmit(EditEvent.Toast(text))
    }

    companion object {
        const val CONTENT_MAX = 50
        const val CATEGORY_MAX = 10
        const val DETAIL_MAX = 1000

        fun todayMillis(): Long = LocalDate.now()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        fun millisToEpochDay(millis: Long): Long =
            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()

        fun epochDayToMillis(epochDay: Long): Long = LocalDate.ofEpochDay(epochDay)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }
}
