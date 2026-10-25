package com.endfield.time

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.endfield.time.data.MilestoneRepository
import com.endfield.time.data.local.MilestoneDatabase
import com.endfield.time.data.prefs.SettingsStore
import com.endfield.time.widget.MilestoneWidgetProvider
import com.endfield.time.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    private val appContext: Context = context.applicationContext

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: MilestoneDatabase = Room.databaseBuilder(
        appContext,
        MilestoneDatabase::class.java,
        "milestone.db"
    ).build()

    val settingsStore: SettingsStore = SettingsStore(appContext)

    private val widgetUpdaterLazy = lazy {
        WidgetUpdater(appContext, repository, appScope)
    }

    /** 数据落库后顺带刷新桌面小组件，保证"改完立刻生效" */
    val repository: MilestoneRepository = MilestoneRepository(
        dao = database.milestoneDao(),
        onDataChanged = { widgetUpdaterLazy.value.refreshAll() }
    )

    val widgetUpdater: WidgetUpdater get() = widgetUpdaterLazy.value

    val todayFlow: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())

    fun onAppResumed() {
        val today = LocalDate.now()
        if (todayFlow.value != today) todayFlow.value = today
        // 打开应用时顺带刷新小组件（跨天/改时间后立即纠正）
        MilestoneWidgetProvider.refreshAll(appContext)
    }
}
