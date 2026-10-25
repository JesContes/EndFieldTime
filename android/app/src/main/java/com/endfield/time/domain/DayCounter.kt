package com.endfield.time.domain

import com.endfield.time.data.local.MilestoneEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class DayType { PASSED, TODAY, FUTURE }

data class DayState(
    val type: DayType,
    val count: Int,
    val label: String
)

/** 上屏用的里程碑模型（含计算结果） */
data class MilestoneUi(
    val id: Long,
    val content: String,
    val category: String,
    val colorHex: String,
    val targetDateText: String,
    val dayType: DayType,
    val dayCount: Int,
    val dayLabel: String,
    val detail: String
) {
    val hasDetail: Boolean get() = detail.isNotBlank()
}

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/** 天数差计算：纯函数，可独立单测 */
object DayCounter {

    fun compute(target: LocalDate, today: LocalDate): DayState {
        val delta = target.toEpochDay() - today.toEpochDay()
        return when {
            delta < 0 -> DayState(DayType.PASSED, (-delta).toInt(), "已过")
            delta == 0L -> DayState(DayType.TODAY, 0, "就是今天")
            else -> DayState(DayType.FUTURE, delta.toInt(), "还有")
        }
    }

    fun toUi(entity: MilestoneEntity, today: LocalDate): MilestoneUi {
        val target = LocalDate.ofEpochDay(entity.targetDate)
        val state = compute(target, today)
        return MilestoneUi(
            id = entity.id,
            content = entity.content,
            category = entity.category,
            colorHex = entity.colorHex,
            targetDateText = target.format(DATE_FORMATTER),
            dayType = state.type,
            dayCount = state.count,
            dayLabel = state.label,
            detail = entity.detail
        )
    }
}
