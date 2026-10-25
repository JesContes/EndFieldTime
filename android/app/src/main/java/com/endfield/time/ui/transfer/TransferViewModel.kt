package com.endfield.time.ui.transfer

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endfield.time.data.MilestoneRepository
import com.endfield.time.data.transfer.ExportCodec
import com.endfield.time.data.transfer.ImportPreview
import com.endfield.time.data.transfer.TransferFiles
import com.endfield.time.ui.UiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TransferUiState(
    val pending: ImportPreview? = null,
    /** 待导入记录中，已存在于本机的条数（合并时会跳过） */
    val existingInPending: Int = 0,
    val busy: Boolean = false
)

class TransferViewModel(
    private val repository: MilestoneRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransferUiState())
    val state: StateFlow<TransferUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ---------------- 导出 ----------------

    fun export(resolver: ContentResolver, uri: Uri) {
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val message = withContext(Dispatchers.IO) {
                try {
                    val records = repository.getAll()
                    if (records.isEmpty()) {
                        return@withContext "还没有记录可以导出"
                    }
                    val json = ExportCodec.encode(records)
                    val gzip = TransferFiles.isGzipName(TransferFiles.displayName(resolver, uri))
                    val bytes = TransferFiles.writeText(resolver, uri, json, gzip)
                    "已导出 ${records.size} 条记录 · ${TransferFiles.formatSize(bytes)}"
                } catch (e: Exception) {
                    "导出失败：${e.message ?: "未知错误"}"
                }
            }
            _state.update { it.copy(busy = false) }
            _events.tryEmit(UiEvent.ShowToast(message))
        }
    }

    // ---------------- 导入 ----------------

    fun prepareImport(resolver: ContentResolver, uri: Uri) {
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val text = TransferFiles.readText(resolver, uri)
                    val preview = ExportCodec.decode(text)
                    val existingKeys = repository.getAll()
                        .map { repository.identityKeyOf(it) }
                        .toHashSet()
                    val duplicated = preview.records.count {
                        repository.identityKeyOf(it) in existingKeys
                    }
                    preview to duplicated
                }
            }
            _state.update { it.copy(busy = false) }
            result.onSuccess { (preview, duplicated) ->
                if (preview.count == 0) {
                    _events.tryEmit(UiEvent.ShowToast("文件中没有可导入的记录"))
                } else {
                    _state.update { it.copy(pending = preview, existingInPending = duplicated) }
                }
            }.onFailure { e ->
                _events.tryEmit(UiEvent.ShowToast("导入失败：${e.message ?: "文件格式不正确"}"))
            }
        }
    }

    /** replace=true 覆盖导入（清空本地）；false 合并导入（按内容+分类+日期去重） */
    fun confirmImport(replace: Boolean) {
        val preview = _state.value.pending ?: return
        _state.update { it.copy(pending = null, busy = true) }
        viewModelScope.launch {
            val message = withContext(Dispatchers.IO) {
                try {
                    if (replace) {
                        repository.deleteAll()
                        val inserted = repository.insertAll(preview.records)
                        "已覆盖导入 $inserted 条记录"
                    } else {
                        val existingKeys = repository.getAll()
                            .map { repository.identityKeyOf(it) }
                            .toHashSet()
                        val fresh = preview.records.filter {
                            repository.identityKeyOf(it) !in existingKeys
                        }
                        val inserted = repository.insertAll(fresh)
                        val duplicates = preview.count - inserted
                        buildString {
                            append("导入 $inserted 条")
                            if (duplicates > 0) append("，跳过 $duplicates 条重复")
                            if (preview.skipped > 0) append("，忽略 ${preview.skipped} 条无效数据")
                        }
                    }
                } catch (e: Exception) {
                    "导入失败：${e.message ?: "数据库写入异常"}"
                }
            }
            _state.update { it.copy(busy = false) }
            _events.tryEmit(UiEvent.ShowToast(message))
        }
    }

    fun dismissImport() {
        _state.update { it.copy(pending = null, existingInPending = 0) }
    }
}
