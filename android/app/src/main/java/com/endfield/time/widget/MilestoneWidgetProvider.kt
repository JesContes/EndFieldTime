package com.endfield.time.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import com.endfield.time.EndFieldApp

/**
 * 单条里程碑的桌面小组件。
 *
 * - 点按：打开应用；
 * - 添加/重新配置：进入 [WidgetConfigActivity] 选择要显示的里程碑；
 * - 刷新时机：系统周期（30 分钟）、数据变更（Room InvalidationTracker）、重新配置、开机。
 */
class MilestoneWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        containerOf(context).widgetUpdater.refresh(appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        containerOf(context).widgetUpdater.refresh(intArrayOf(appWidgetId))
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetBindings.clear(context, it) }
    }

    private fun containerOf(context: Context) =
        (context.applicationContext as EndFieldApp).container

    companion object {
        /** 供应用内数据变更后主动刷新全部小组件 */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, MilestoneWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                (context.applicationContext as EndFieldApp).container.widgetUpdater.refresh(ids)
            }
        }
    }
}
