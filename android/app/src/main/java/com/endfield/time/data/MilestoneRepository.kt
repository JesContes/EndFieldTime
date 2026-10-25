package com.endfield.time.data

import com.endfield.time.data.local.CategoryCount
import com.endfield.time.data.local.MilestoneDao
import com.endfield.time.data.local.MilestoneEntity
import kotlinx.coroutines.flow.Flow

/**
 * 唯一数据源抽象：UI 只经由 ViewModel -> Repository -> DAO。
 * 后续若增加云同步，只需在此扩展 RemoteDataSource。
 */
class MilestoneRepository(private val dao: MilestoneDao) {

    fun observe(category: String): Flow<List<MilestoneEntity>> =
        if (category == ALL_CATEGORY) dao.observeAll() else dao.observeByCategory(category)

    fun observeCategories(): Flow<List<String>> = dao.observeCategories()

    fun observeCategoryCounts(): Flow<List<CategoryCount>> = dao.observeCategoryCounts()

    suspend fun getById(id: Long): MilestoneEntity? = dao.getById(id)

    suspend fun insert(entity: MilestoneEntity): Long = dao.insert(entity)

    suspend fun update(entity: MilestoneEntity): Int = dao.update(entity)

    suspend fun deleteById(id: Long): Int = dao.deleteById(id)

    suspend fun getAll(): List<MilestoneEntity> = dao.getAll()

    suspend fun insertAll(entities: List<MilestoneEntity>): Int {
        if (entities.isEmpty()) return 0
        return dao.insertAll(entities).size
    }

    suspend fun deleteAll(): Int = dao.deleteAll()

    /** 导入去重键：内容 + 分类 + 目标日期 */
    fun identityKey(content: String, category: String, targetDate: Long): String =
        "${content.trim()}|${category.trim()}|$targetDate"

    fun identityKeyOf(entity: MilestoneEntity): String =
        identityKey(entity.content, entity.category, entity.targetDate)

    companion object {
        const val ALL_CATEGORY = "全部"
        val DEFAULT_CATEGORIES = listOf("生活", "工作", "学习", "纪念日")
        val PRESET_COLORS = listOf(
            "#66CCFF", "#4DB6AC", "#81C784", "#FFD54F",
            "#FFB74D", "#F06292", "#9575CD", "#FF8A65"
        )
    }
}
