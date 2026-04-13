package com.wbjang.footballdiary.di

import android.content.Context
import androidx.room.Room
import com.wbjang.footballdiary.data.local.AppDatabase
import com.wbjang.footballdiary.data.local.dao.ReviewDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "football_diary.db")
            .build()

    @Provides
    fun provideReviewDao(database: AppDatabase): ReviewDao = database.reviewDao()
}
