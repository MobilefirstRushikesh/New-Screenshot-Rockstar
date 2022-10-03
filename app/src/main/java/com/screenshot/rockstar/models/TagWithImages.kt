package com.screenshot.rockstar.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class TagWithImages(
    @Embedded val tags: TagModel,
    @Relation(
        parentColumn = "tagName",
        entityColumn = "path",
        associateBy = Junction(ImageTagCrossRef::class)
    )
    val images: List<ImageModel>
)
