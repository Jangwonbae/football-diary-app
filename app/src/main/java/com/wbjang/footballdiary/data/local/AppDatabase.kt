package com.wbjang.footballdiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wbjang.footballdiary.data.local.dao.ReviewDao
import com.wbjang.footballdiary.data.local.entity.ReviewEntity

@Database(entities = [ReviewEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao
}
