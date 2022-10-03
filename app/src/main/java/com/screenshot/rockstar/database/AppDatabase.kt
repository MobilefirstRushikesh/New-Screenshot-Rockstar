package com.screenshot.rockstar.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.screenshot.rockstar.models.ImageModel
import com.screenshot.rockstar.models.ImageTagCrossRef
import com.screenshot.rockstar.models.TagModel


@Database(
    entities = [ImageModel::class, TagModel::class, ImageTagCrossRef::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): RoomApiService

    companion object {
        const val ROOM_DATABASE = "user_database"
    }
}