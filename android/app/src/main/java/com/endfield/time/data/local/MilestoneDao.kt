package com.endfield.time.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {

    @Query("SELECT * FROM milestone ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestone WHERE category = :category ORDER BY createdAt DESC")
    fun observeByCategory(category: String): Flow<List<MilestoneEntity>>

    @Query("SELECT DISTINCT category FROM milestone ORDER BY category ASC")
    fun observeCategories(): Flow<List<String>>

    @Query("SELECT category AS category, COUNT(*) AS count FROM milestone GROUP BY category")
    fun observeCategoryCounts(): Flow<List<CategoryCount>>

    @Query("SELECT * FROM milestone WHERE id = :id")
    suspend fun getById(id: Long): MilestoneEntity?

    /** 导出用：一次性取全量（按创建时间升序，便于阅读） */
    @Query("SELECT * FROM milestone ORDER BY createdAt ASC")
    suspend fun getAll(): List<MilestoneEntity>

    @Insert
    suspend fun insert(entity: MilestoneEntity): Long

    @Insert
    suspend fun insertAll(entities: List<MilestoneEntity>): List<Long>

    @Update
    suspend fun update(entity: MilestoneEntity): Int

    @Query("DELETE FROM milestone WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    /** 「覆盖导入」用：清空全表 */
    @Query("DELETE FROM milestone")
    suspend fun deleteAll(): Int
}
