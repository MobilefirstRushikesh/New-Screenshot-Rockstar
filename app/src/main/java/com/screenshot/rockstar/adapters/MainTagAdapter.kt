package com.screenshot.rockstar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.screenshot.rockstar.R
import com.screenshot.rockstar.models.TagModel


class MainTagAdapter(private var tagListener: TagItemClickListener) : RecyclerView.Adapter<MainTagAdapter.DataViewHolder>() {



    private val differCallback = object: DiffUtil.ItemCallback<TagModel>(){
        override fun areItemsTheSame(oldItem: TagModel, newItem: TagModel): Boolean {
            return  oldItem.tagName == newItem.tagName
        }

        override fun areContentsTheSame(oldItem: TagModel, newItem: TagModel): Boolean {
            return oldItem.tagName == newItem.tagName
                    && oldItem.tag_id == oldItem.tag_id
        }

    }

    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list:List<TagModel>) = differ.submitList(list)

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val tagTitle: TextView = itemView.findViewById(R.id.txtview_rvTag_tagName)
        //val imgCount:TextView = itemView.findViewById(R.id.txtview_rvTag_PhotoCount)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (absoluteAdapterPosition != RecyclerView.NO_POSITION){
                tagListener.onTagClick(absoluteAdapterPosition, differ.currentList[absoluteAdapterPosition].tagName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rvtag_layout, parent, false)
        )


    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {

        val tagName = differ.currentList[position].tagName
        holder.tagTitle.text = tagName

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }




    interface TagItemClickListener {
        fun onTagClick(position: Int, tagName: String)
    }
}