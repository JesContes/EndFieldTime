package com.endfield.time.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** 轻量偏好：记住上次选中的筛选分类 */
class SettingsStore(private val context: Context) {

    val selectedCategory: Flow<String?> = context.settingsDataStore.data
        .map { it[KEY_SELECTED_CATEGORY] }

    suspend fun setSelectedCategory(category: String) {
        context.settingsDataStore.edit { it[KEY_SELECTED_CATEGORY] = category }
    }

    private companion object {
        val KEY_SELECTED_CATEGORY = stringPreferencesKey("selected_category")
    }
}
