package com.screenshot.rockstar.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Entity
@Parcelize
data class ImageModel(
    @PrimaryKey
    val path: String,
    val text: String?,
    val isHidden:Boolean,
    val creationTime:String,
    val hasTag:Boolean,
    val first200:Boolean = false
):Parcelable
