package com.screenshot.rockstar.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

interface RoomMigrationHelper {

    companion object {

        val migration_1_2 = object : Migration(1, 2) {

            override fun migrate(database: SupportSQLiteDatabase) {

                    database.execSQL("ALTER TABLE ImageModel ADD COLUMN isHidden INTEGER NOT NULL DEFAULT(0)")
            }
        }
    }
}