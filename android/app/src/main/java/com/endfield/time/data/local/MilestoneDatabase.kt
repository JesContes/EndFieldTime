package com.endfield.time.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MilestoneEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MilestoneDatabase : RoomDatabase() {
    abstract fun milestoneDao(): MilestoneDao
}
