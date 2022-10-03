package com.screenshot.rockstar.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.screenshot.rockstar.R
import com.screenshot.rockstar.models.ImageModel
import com.screenshot.rockstar.ui.fragments.SearchFragment
import kotlinx.android.synthetic.main.raw_albums.view.*
import java.io.File


class MainAdapter(
    private val activity: AppCompatActivity,
    private val imageListener: OnImageClickListener
) :
    RecyclerView.Adapter<MainAdapter.DataViewHolder>() {
    private var multiSelect = false
    private var selectAll = false
    private val selectedItems = ArrayList<ImageModel>()

    /**
     * change only items that are different than prev list
     */
    private val differCallback = object: DiffUtil.ItemCallback<ImageModel>(){
        override fun areItemsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem.path == newItem.path
                    && oldItem.text == newItem.text
                    && oldItem.creationTime == newItem.creationTime
                    && oldItem.hasTag == newItem.hasTag
                    && oldItem.first200 == newItem.hasTag
        }
    }


    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list:List<ImageModel>) = differ.submitList(list)


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        private val imgTag: ImageView = itemView.findViewById(R.id.rvuser_img_imageHasTag)
        private val imgView: ImageView = itemView.findViewById(R.id.imageViewAvatar)
        private val imgCheckBlank: ImageView = itemView.findViewById(R.id.img_check_blank)
        private val imgChecked: ImageView = itemView.findViewById(R.id.img_checked)
        private val lockRel: RelativeLayout = itemView.findViewById(R.id.raw_album_lock_rel)


        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        @SuppressLint("NewApi")
        fun bind(currentList: MutableList<ImageModel>, image: ImageModel, activity: AppCompatActivity) {

            imgTag.visibility = if (image.hasTag) View.VISIBLE else View.GONE
            lockRel.visibility = if (!image.first200) View.VISIBLE else View.GONE

            val options = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(
                    ContextCompat.getDrawable(
                        activity,
                        R.color.transparent_white_6
                    )
                )
                .error(
                    ContextCompat.getDrawable(
                        activity,
                        R.color.transparent_red_6
                    )
                )
                .centerCrop()

            Glide.with(imgView)
                .load(File(image.path))
                .thumbnail(0.33f)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgView)

            //if multiSelect true visible CheckBlank to all images
            when {
                multiSelect -> {
                    imgCheckBlank.visibility = View.VISIBLE
                }
                selectAll -> {
                    imgCheckBlank.visibility = View.VISIBLE
                    imgChecked.visibility = View.VISIBLE
                    itemView.imageViewAvatar.foreground = ContextCompat.getDrawable(
                        activity,
                        R.color.selected_foreGround
                    )
                }
                else  // deSelect All
                -> {
                    imgCheckBlank.visibility = View.GONE
                    imgChecked.visibility = View.GONE
                    itemView.imageViewAvatar.foreground = ContextCompat.getDrawable(
                        activity,
                        R.color.deselected_foreGround
                    )
                    SearchFragment.multiSelectedImage.clear()

                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onClick(v: View?) {
            val currentImage = differ.currentList[absoluteAdapterPosition]
            if (multiSelect || selectAll) {
                if (currentImage.first200){
                    if (SearchFragment.multiSelectedImage.contains(currentImage)) {
                        imgCheckBlank.visibility = View.VISIBLE
                        imgChecked.visibility = View.GONE
                        SearchFragment.multiSelectedImage.remove(currentImage)
                        //selectedItems.remove(differ.currentList[absoluteAdapterPosition])
                        itemView.imageViewAvatar.foreground = ContextCompat.getDrawable(
                            activity,
                            R.color.deselected_foreGround
                        )
                        Log.d("selectedItem", selectedItems.size.toString())
                    }
                    else {
                        imgCheckBlank.visibility = View.GONE
                        imgChecked.visibility = View.VISIBLE
                        itemView.imageViewAvatar.foreground = ContextCompat.getDrawable(
                            activity,
                            R.color.selected_foreGround
                        )
                        SearchFragment.multiSelectedImage.add(currentImage)
                        //selectedItems.add(differ.currentList[absoluteAdapterPosition])
                        Log.d("selectedItem", selectedItems.size.toString())
                    }
                }
            } else {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    imageListener.onImageClick(absoluteAdapterPosition,currentImage)
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onLongClick(v: View?): Boolean {
            if (differ.currentList[absoluteAdapterPosition].first200){
                if (!multiSelect)
                    multiSelect = true

                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    imageListener.onLongImageClick(absoluteAdapterPosition)
                    imgChecked.visibility = View.VISIBLE
                    itemView.imageViewAvatar.foreground =
                        ContextCompat.getDrawable(activity, R.color.selected_foreGround)
                    SearchFragment.multiSelectedImage.add(differ.currentList[absoluteAdapterPosition])
                    //selectedItems.add(differ.currentList[absoluteAdapterPosition])
                    notifyDataSetChanged()
                    Log.d("selectedItem", selectedItems.size.toString())
                }
                return true
            }
           return false
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.raw_albums, parent, false)
        )

    override fun getItemCount(): Int = differ.currentList.size

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
            holder.bind(differ.currentList, differ.currentList[position], activity)
    }


    //----------------------Recycler item listener----------------------------//
    interface OnImageClickListener {
        fun onImageClick(position: Int, image: ImageModel)
        fun onLongImageClick(position: Int)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun deselectAll() {
        multiSelect = false
        selectAll  = false
        SearchFragment.multiSelectedImage.clear()
        notifyDataSetChanged()
    }

    fun selectAll(){
        selectAll = true
        SearchFragment.multiSelectedImage.addAll(differ.currentList)
        notifyDataSetChanged()
    }

}