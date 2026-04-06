package com.wbjang.footballdiary.di

import com.wbjang.footballdiary.data.repository.FootballRepositoryImpl
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindFootballRepository(
        impl: FootballRepositoryImpl
    ): FootballRepository
}
