package com.endfield.time.widget

import android.content.Context

/** 每个桌面小组件实例绑定的里程碑 id */
object WidgetBindings {

    private const val PREFS = "widget_bindings"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun key(widgetId: Int) = "widget_$widgetId"

    fun bind(context: Context, widgetId: Int, milestoneId: Long) {
        prefs(context).edit().putLong(key(widgetId), milestoneId).apply()
    }

    fun boundId(context: Context, widgetId: Int): Long =
        prefs(context).getLong(key(widgetId), -1L)

    fun clear(context: Context, widgetId: Int) {
        prefs(context).edit().remove(key(widgetId)).apply()
    }
}
