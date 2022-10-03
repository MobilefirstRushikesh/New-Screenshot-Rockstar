package com.screenshot.rockstar.models



data class ImageDetail(
    val path: String,
    val text: String?,
    val isHidden:Boolean,
    val creationTime:String,
    val hasTag:Boolean,
    val first200:Boolean = false,
    val tags: List<TagModel>
)

fun ImageModel.toImageDetail(tags: List<TagModel>) =
    ImageDetail(
        path = this.path,
        text = this.text,
        isHidden = this.isHidden,
        creationTime = this.creationTime,
        hasTag = this.hasTag,
        first200 = this.first200,
        tags = tags,
    )

fun ImageDetail.toImageModel() =
    ImageModel(
        path = this.path,
        text = this.text,
        isHidden = this.isHidden,
        creationTime = this.creationTime,
        hasTag = this.hasTag,
        first200 = this.first200
    )