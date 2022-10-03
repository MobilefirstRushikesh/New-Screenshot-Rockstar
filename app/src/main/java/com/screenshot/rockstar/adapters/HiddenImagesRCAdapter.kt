package com.screenshot.rockstar.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.screenshot.rockstar.R
import com.screenshot.rockstar.databinding.RawAlbumsBinding
import com.screenshot.rockstar.models.ImageModel
import java.io.File


class HiddenImagesRCAdapter(
    val context: Context,
    private var mListener: OnImageClickListener
) : RecyclerView.Adapter<HiddenImagesRCAdapter.ItemViewHolder>() {

    private val dataset = mutableListOf<ImageModel>()

    private val selectedImage = mutableListOf<ImageModel>()

    var isSelection = false
    private set

    fun isAllSelected(): Boolean {
        return selectedImage.size == dataset.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(items: List<ImageModel>) {

        if (dataset == items) return

        dataset.clear()
        selectedImage.clear()
        dataset.addAll(items)
        notifyDataSetChanged()
    }

    fun updateSelectedImage(item: ImageModel, position: Int) {
        if (selectedImage.contains(item)) {
            selectedImage.remove(item)
        } else {
            selectedImage.add(item)
        }

        notifyItemChanged(position)
    }

    fun selectUnselectAllImage() {
        if (dataset.isEmpty()) return

        if (selectedImage.size == dataset.size) {
            selectedImage.clear()
            isSelection = false
        } else {
            selectedImage.clear()
            selectedImage.addAll(dataset)
            isSelection = true
        }

        notifyItemRangeChanged(0, dataset.size)
    }

    fun getSelectedItems(): List<ImageModel> = selectedImage

    fun clearAdapter() = dataset.clear()

    fun setSelection (isSelection : Boolean) {
        /*clearing prev selected items before selection state change*/
        selectedImage.clear()
        this.isSelection = isSelection
    }

    inner class ItemViewHolder(val binding: RawAlbumsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ImageModel) {

            if (selectedImage.contains(item)) {
                binding.imgChecked.visibility = View.VISIBLE
            } else {
                binding.imgChecked.visibility = View.GONE
            }

            val options = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(
                    ContextCompat.getDrawable(
                        context,
                        R.color.transparent_white_6
                    )
                )
                .error(
                    ContextCompat.getDrawable(
                        context,
                        R.color.transparent_red_6
                    )
                )
                .centerCrop()

            Glide.with(binding.imageViewAvatar)
                .load(File(item.path))
                .thumbnail(0.33f)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imageViewAvatar)


            binding.root.setOnClickListener {
                mListener.onImageClick(absoluteAdapterPosition, binding.imgChecked, item)
            }

            binding.root.setOnLongClickListener {
                mListener.onImageLongClick(absoluteAdapterPosition, binding.imgChecked, item)
                true
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(
            binding = RawAlbumsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(dataset[holder.absoluteAdapterPosition])
    }


    override fun getItemCount(): Int {
        return dataset.size
    }


    interface OnImageClickListener {
        fun onImageClick(position: Int, view: View, item: ImageModel)
        fun onImageLongClick(position: Int, view: View, item: ImageModel)
    }
}


