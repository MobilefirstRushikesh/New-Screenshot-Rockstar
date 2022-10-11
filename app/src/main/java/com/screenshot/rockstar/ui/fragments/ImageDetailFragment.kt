package com.screenshot.rockstar.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.screenshot.rockstar.R
import com.screenshot.rockstar.adapters.ImageSliderAdapter
import com.screenshot.rockstar.databinding.FragmentImageDetailBinding
import com.screenshot.rockstar.interfaces.ChangeFragmentListener
import com.screenshot.rockstar.models.ImageDetail
import com.screenshot.rockstar.models.ImageTagCrossRef
import com.screenshot.rockstar.models.TagModel
import com.screenshot.rockstar.models.toImageModel
import com.screenshot.rockstar.utils.Constants
import com.screenshot.rockstar.utils.Constants.Companion.BILLING_FRAG_TAG
import com.screenshot.rockstar.utils.Status
import com.screenshot.rockstar.utils.ViewUtils.alertDialog
import com.screenshot.rockstar.utils.ViewUtils.showToast
import com.screenshot.rockstar.viewModel.MainViewModel
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.raw_albums.*
import java.io.File

@AndroidEntryPoint
class ImageDetailFragment : BaseFragment(), ImageSliderAdapter.ImageSliderAdapterListener {


    private var changeFragmentListener: ChangeFragmentListener? = null
    private var zoomViewBuilder: StfalconImageViewer<File>? = null
    private val viewModel: MainViewModel by activityViewModels()
    private var imageSliderAdapter: ImageSliderAdapter? = null


    private var currentImageView: ImageView? = null
    private var isUpdate = false

    private var _binding: FragmentImageDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentImageDetailBinding.inflate(inflater, container, false)

        init()



        return binding.root
    }


    companion object {
        const val TAG = "FragmentImageDetail"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            changeFragmentListener = activity as ChangeFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement" + "ChangeFragmentListener")
        }
    }


    private fun init() {
        isUpdate = true
        setupObservers()
        setupUi()
        trackScreen("ImageDetailScreen")
    }

    private fun setupUi() {

        binding.btnAddTag.setOnClickListener {
            onTagClick()
        }

        binding.btnShare.setOnClickListener {
            onShareClick()
        }

        binding.btnHideDetailShot.setOnClickListener {
            hideImage()
        }

        binding.btnBack.setOnClickListener { onBackClick() }


        imageSliderAdapter = ImageSliderAdapter(requireActivity(), this)
        binding.viewPagerImgDetail.apply {
            adapter = imageSliderAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupObservers() {

        viewModel.getImageDetailList().observe(viewLifecycleOwner) {
            it?.let { res ->

                when (res.status) {

                    Status.SUCCESS -> {
                        res.data?.let { list ->

                            if (SearchFragment.isProcessing) {

                                if (isUpdate) {
                                    bindData(list)
                                }

                            } else {
                                bindData(list)
                            }

                            isUpdate = false
                        }
                    }
                    Status.ERROR -> {
                        showToast(requireActivity(), "${res.message}")
                        onBackClick()
                    }

                    Status.LOADING -> {

                    }

                }

            }
        }


        binding.viewPagerImgDetail.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val path = imageSliderAdapter?.getItemAtPosition(position)?.path.orEmpty()
                    if (path.isEmpty()) {
                        changeFragmentListener?.popBackStack()
                    } else {
                        viewModel.setCurrentPosition(position)
                        viewModel.setCurrentImagePath(path)
                        //getViewHolder(position)
                    }

                }
            })
    }


    private fun bindData(list: List<ImageDetail>) {
        if (list.isEmpty()) {
            showToast(requireActivity(), "Wait for a moment.")
            onBackClick()
            return
        }

        imageSliderAdapter?.submitList(list)

        viewModel.currentImagePosition.value.let { pos ->
            if (-1 != pos) {
                changeOnViewPagerImageChange(pos)
            }
        }
    }

    private fun onBackClick() {
        changeFragmentListener?.popBackStack()
    }


    private fun changeOnViewPagerImageChange(position: Int) {
        binding.viewPagerImgDetail.setCurrentItem(position, false)
        viewModel.setCurrentImagePath(
            imageSliderAdapter?.getItemAtPosition(position)?.path.orEmpty()
        )
    }


    private fun onTagClick() {

        val tagBottomSheetDialog = TagBottomSheetDialog(object :
            TagBottomSheetDialog.TagSheetListener {
            override fun onTagDoneClicked(tags: ArrayList<String>) {
                insertTagIntoDatabase(tags)
            }

        })
        tagBottomSheetDialog.show(
            requireActivity().supportFragmentManager,
            "tagBottomSheetDialog"
        )

    }


    private fun insertTagIntoDatabase(tags: ArrayList<String>) {

        val pathList = ArrayList<String>()
        pathList.add(viewModel.currentImagePath.value)

        val tagModelList = ArrayList<TagModel>()
        val crossRefModelList = ArrayList<ImageTagCrossRef>()

        tags.forEach { tag->
            tagModelList.add(TagModel(tagName = tag))
            crossRefModelList.add(ImageTagCrossRef(viewModel.currentImagePath.value, tag))
        }

        viewModel.apply {
            insertTags(tagModelList)
            insertImageTagEntries(crossRefModelList)
            setHasTag(true,pathList)
        }

        SearchFragment.isUpdating = true
        isUpdate = true
        setupObservers()

    }


    private fun hideImage() {

        viewModel.setImageHidden(
            imageSliderAdapter?.getItemAtPosition(viewModel.currentImagePosition.value)
                ?.toImageModel()!!
        )
        SearchFragment.isUpdating = true
        isUpdate = true
        setupObservers()
        showToast(requireActivity(), "Image Hidden")

    }


    private fun onShareClick() {
        checkImageUnlock(viewModel.currentImagePosition.value)?.let { isUnlock ->
            if (isUnlock) {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.type = "image/*"
                val file = File(viewModel.currentImagePath.value)
                val imageUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireActivity().packageName}.fileprovider",
                    file
                )
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                startActivityForResult(
                    Intent.createChooser(shareIntent, "Share image using"),
                    Constants.REQUEST_CODE_IMAGE_SHARE
                )
            }
        }

    }


  /*  private fun hideSelectedAlert() {
        alertDialog(requireActivity(),
            "Are you sure?",
            "This will hide the shot.",
            "Yes",
            "No",
            false,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {

                    -1 -> {

                        dialog.dismiss()
                    }

                    -2 -> {
                        dialog.dismiss()
                    }

                }
            })
    }*/


    private fun createZoomViewBuilder() {
        val zoomOverlay = layoutInflater.inflate(R.layout.layout_zoom_image_back, container, false)
        val zoomBack: ImageButton = zoomOverlay.findViewById(R.id.zoom_back)

        if (zoomOverlay.parent != null) {
            (zoomOverlay.parent as ViewGroup).removeView(zoomOverlay)
        }

        zoomViewBuilder = StfalconImageViewer.Builder(
            requireContext(),
            imageSliderAdapter?.getImagesAsFiles()
        ) { view, image ->
            Glide.with(this)
                .load(image)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(view)
        }
            .withOverlayView(zoomOverlay)
            .withHiddenStatusBar(true)
            .allowZooming(true)
            .allowSwipeToDismiss(true)
            .withTransitionFrom(currentImageView)
            .withStartPosition(viewModel.currentImagePosition.value)
            .withImageChangeListener {
                changeOnViewPagerImageChange(it)
                zoomViewBuilder?.updateTransitionImage(currentImageView)

            }
            .show()

        zoomBack.setOnClickListener {
            zoomViewBuilder?.close()
        }
    }

    private fun getViewHolder(position: Int) {
        try {
            val holder =
                (binding.viewPagerImgDetail[0] as RecyclerView).findViewHolderForAdapterPosition(position) as ImageSliderAdapter.ViewHolder
            currentImageView = holder.imgSlider

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onSliderImageClick(Position: Int) {
        createZoomViewBuilder()
    }

    override fun onChipClick() {

    }

    override fun onChipRemoved(text: String) {
        viewModel.deleteSelectedTag(
            viewModel.currentImagePath.value,
            text
        )

        showToast(requireActivity(), "removed")

        SearchFragment.isUpdating = true
    }

    private fun checkImageUnlock(position: Int): Boolean? {
        return imageSliderAdapter?.getItemAtPosition(position)?.let { imgModel ->
            if (imgModel.first200) {
                return@let true
            } else {
                alertDialog(requireActivity(), "Go Pro & Get Much More",
                    "Subscribe Now!",
                    "Learn More & Subscribe",
                    "Cancel",
                    true,
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            -1 -> {
                                changeFragmentListener?.addFragment(BILLING_FRAG_TAG, true)
                                dialog.dismiss()
                            }
                            -2 -> {
                                dialog.dismiss()
                            }
                        }
                    })

                return@let false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        changeFragmentListener = null
        imageSliderAdapter = null
        zoomViewBuilder = null

        viewModel.currentImagePosition.value = -1
    }
}

