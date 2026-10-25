package com.endfield.time.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 里程碑表实体。
 * targetDate 存 LocalDate.toEpochDay()，不受时区影响，便于天数差计算。
 */
@Entity(
    tableName = "milestone",
    indices = [Index("category")]
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val content: String,
    val category: String,
    val targetDate: Long,
    val colorHex: String,
    val detail: String = "",
    val createdAt: Long,
    val sortOrder: Int = 0
)

/** 分类聚合计数（用于筛选面板右侧显示 N 条） */
data class CategoryCount(
    val category: String,
    val count: Int
)
