package com.endfield.time

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.endfield.time.data.MilestoneRepository
import com.endfield.time.data.local.MilestoneDatabase
import com.endfield.time.data.prefs.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class EndFieldApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/**
 * 极简手动依赖注入（设计文档 §7 允许的备选方案）。
 * todayFlow 用于「回到前台跨天重算天数」(FR-009)。
 */
class AppContainer(context: Context) {

    private val database: MilestoneDatabase = Room.databaseBuilder(
        context.applicationContext,
        MilestoneDatabase::class.java,
        "milestone.db"
    ).build()

    val settingsStore: SettingsStore = SettingsStore(context.applicationContext)

    val repository: MilestoneRepository = MilestoneRepository(database.milestoneDao())

    val todayFlow: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())

    fun onAppResumed() {
        val today = LocalDate.now()
        if (todayFlow.value != today) todayFlow.value = today
    }
}
