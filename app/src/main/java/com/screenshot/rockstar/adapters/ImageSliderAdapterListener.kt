package com.screenshot.rockstar.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import com.screenshot.rockstar.R
import com.screenshot.rockstar.databinding.LayoutImageSliderBinding
import com.screenshot.rockstar.models.ImageDetail
import kotlinx.android.synthetic.main.raw_albums.*
import java.io.File


class ImageSliderAdapter(
    private val activity: Activity,
    private val imageSlideListener: ImageSliderAdapterListener
) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<ImageDetail>() {

        override fun areItemsTheSame(oldItem: ImageDetail, newItem: ImageDetail): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: ImageDetail, newItem: ImageDetail): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<ImageDetail>) = differ.submitList(list)

    fun getItemAtPosition(position: Int): ImageDetail? {
        return if (differ.currentList.isNotEmpty()) {
            differ.currentList[position]
        } else {
            null
        }

    }

    fun getImagesAsFiles(): List<File> {
        val files = mutableListOf<File>()
        differ.currentList.map {
            files.add(File(it.path))
        }

        return files
    }

    inner class ViewHolder(private val binding : LayoutImageSliderBinding)
        : RecyclerView.ViewHolder(binding.root) {

        val imgSlider = binding.imgSlider

        fun bind(item: ImageDetail) {

            val options = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(RoundedCorners(25))

            Glide.with(binding.imgSlider)
                .load(File(item.path))
                .thumbnail(0.33f)
                .apply(options)
                .into(binding.imgSlider)


            binding.tagChipGroup.removeAllViews()

            item.tags.forEach { tag ->
                createChip(tag.tagName)
            }

            binding.root.setOnClickListener {
                imageSlideListener.onSliderImageClick(
                    absoluteAdapterPosition
                )
            }

        }

        private fun createChip(name: String) {

            val chip = activity.layoutInflater.inflate(
                R.layout.tag_chip_layout,
                activity.container,
                false
            ) as Chip
            chip.text = name
            chip.isChipIconVisible = true
            chip.isClickable = true
            chip.isCheckable = false

            chip.setOnLongClickListener {
                when (chip.isCloseIconVisible) {
                    true -> chip.isCloseIconVisible = false
                    false -> chip.isCloseIconVisible = true
                }
                true
            }

            chip.setOnCloseIconClickListener {
                binding.tagChipGroup.removeView(chip as View)
                imageSlideListener.onChipRemoved(name)
            }

            binding.tagChipGroup.addView(chip as View)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            binding = LayoutImageSliderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[holder.absoluteAdapterPosition])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    interface ImageSliderAdapterListener {
        fun onSliderImageClick(Position: Int)

        fun onChipClick()

        fun onChipRemoved(text: String)
    }
}