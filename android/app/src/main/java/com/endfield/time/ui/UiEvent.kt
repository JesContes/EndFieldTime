package com.endfield.time.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** 一次性事件（Toast / 关页等），避免重组后重复消费 */
sealed interface UiEvent {
    data class ShowToast(val text: String) : UiEvent
}

/** 用 lambda 构造 ViewModel 的工厂，避免为每个 VM 写一个 Factory 类 */
fun <T : ViewModel> viewModelFactoryOf(creator: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
    }
