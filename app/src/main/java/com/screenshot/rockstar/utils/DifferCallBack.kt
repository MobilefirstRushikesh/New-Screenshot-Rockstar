package com.screenshot.rockstar.utils

import androidx.recyclerview.widget.DiffUtil
import com.screenshot.rockstar.models.ImageModel

class DifferCallBack(
    private val oldList: List<ImageModel>,
    private val newList: List<ImageModel>
): DiffUtil.Callback() {


    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return  oldList[oldItemPosition].path == newList[newItemPosition].path
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].path == newList[newItemPosition].path
                && oldList[oldItemPosition].text == newList[newItemPosition].text
                && oldList[oldItemPosition].creationTime == newList[newItemPosition].creationTime
                && oldList[oldItemPosition].first200 == newList[newItemPosition].first200
                && oldList[oldItemPosition].isHidden == newList[newItemPosition].isHidden
                && oldList[oldItemPosition].hasTag == newList[newItemPosition].hasTag
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}