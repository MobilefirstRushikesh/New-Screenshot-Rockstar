package com.screenshot.rockstar.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class ImageWithTags(
    @Embedded val image: ImageModel,
    @Relation(
        parentColumn = "path",
        entityColumn = "tagName",
        associateBy = Junction(ImageTagCrossRef::class)
    )
    val tags: List<TagModel>
)
