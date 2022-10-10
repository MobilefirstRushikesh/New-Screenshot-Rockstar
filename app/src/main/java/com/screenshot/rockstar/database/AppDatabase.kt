package com.screenshot.rockstar.database

import android.content.ClipData
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.screenshot.rockstar.models.ImageModel
import com.screenshot.rockstar.models.ImageTagCrossRef
import com.screenshot.rockstar.models.TagModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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