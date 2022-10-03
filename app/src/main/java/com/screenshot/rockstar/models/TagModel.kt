package com.screenshot.rockstar.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["tagName"], unique = true)])
data class TagModel(
    @PrimaryKey(autoGenerate = true) val tag_id:Int = 0,
    @ColumnInfo(name = "tagName")val tagName:String
)
