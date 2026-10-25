package com.endfield.time.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.endfield.time.MainActivity
import com.endfield.time.R
import com.endfield.time.domain.MilestoneUi

/** 把一条里程碑渲染成 RemoteViews */
object WidgetRenderer {

    private const val PLACEHOLDER_COLOR = 0xFF8FB3C9.toInt()

    fun render(context: Context, widgetId: Int, milestone: MilestoneUi?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_milestone)

        val color = milestone?.let { safeColor(it.colorHex) } ?: PLACEHOLDER_COLOR
        // ImageView + setColorFilter：保留圆角背景的同时换成卡片颜色
        views.setInt(R.id.widget_bg, "setColorFilter", color)

        if (milestone != null) {
            views.setTextViewText(R.id.widget_category, milestone.category)
            views.setTextViewText(R.id.widget_day_label, milestone.dayLabel)
            views.setTextViewText(R.id.widget_day_count, milestone.dayCount.toString())
            views.setTextViewText(R.id.widget_content, milestone.content)
            views.setTextViewText(R.id.widget_date, "目标日期 ${milestone.targetDateText}")
        } else {
            views.setTextViewText(R.id.widget_category, "时记")
            views.setTextViewText(R.id.widget_day_label, "")
            views.setTextViewText(R.id.widget_day_count, "—")
            views.setTextViewText(R.id.widget_content, "点按选择里程碑")
            views.setTextViewText(R.id.widget_date, "长按小组件可重新配置")
        }

        views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context, widgetId))
        return views
    }

    private fun openAppIntent(context: Context, widgetId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun safeColor(hex: String): Int = try {
        Color.parseColor(hex)
    } catch (e: IllegalArgumentException) {
        PLACEHOLDER_COLOR
    }
}
