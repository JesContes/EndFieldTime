package com.endfield.time.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.endfield.time.data.MilestoneRepository
import com.endfield.time.domain.DayCounter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** 读取绑定关系 + 数据，渲染并回写小组件 */
class WidgetUpdater(
    private val context: Context,
    private val repository: MilestoneRepository,
    private val scope: CoroutineScope
) {

    private val appWidgetManager: AppWidgetManager =
        AppWidgetManager.getInstance(context.applicationContext)

    fun refresh(widgetIds: IntArray) {
        if (widgetIds.isEmpty()) return
        scope.launch {
            val today = LocalDate.now()
            widgetIds.forEach { widgetId ->
                val milestoneId = WidgetBindings.boundId(context, widgetId)
                val entity = if (milestoneId > 0) {
                    withContext(Dispatchers.IO) { repository.getById(milestoneId) }
                } else {
                    null
                }
                val ui = entity?.let { DayCounter.toUi(it, today) }
                val views = WidgetRenderer.render(context, widgetId, ui)
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    fun refreshAll() {
        val ids = appWidgetManager.getAppWidgetIds(
            ComponentName(context, MilestoneWidgetProvider::class.java)
        )
        refresh(ids)
    }
}
