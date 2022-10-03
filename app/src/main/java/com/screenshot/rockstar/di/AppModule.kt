package com.screenshot.rockstar.di

import android.content.Context
import androidx.room.Room
import com.screenshot.rockstar.database.AppDatabase
import com.screenshot.rockstar.database.RoomApiService
import com.screenshot.rockstar.database.RoomMigrationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext app: Context
    ): AppDatabase =
        Room
            .databaseBuilder(
                app,
                AppDatabase::class.java,
                AppDatabase.ROOM_DATABASE
            )
            .addMigrations(RoomMigrationHelper.migration_1_2)
            .build()

    @Provides
    @Singleton
    fun provideDao(db: AppDatabase): RoomApiService =
        db.userDao()


}