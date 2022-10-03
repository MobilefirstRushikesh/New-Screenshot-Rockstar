package com.screenshot.rockstar.models

import androidx.room.Entity

@Entity(primaryKeys = ["path", "tagName"])
data class ImageTagCrossRef(
    val path: String,
    val tagName: String
)
