package com.screenshot.rockstar.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.screenshot.rockstar.R
import com.screenshot.rockstar.adapters.HiddenImagesRCAdapter
import com.screenshot.rockstar.databinding.FragmentHiddenShotsBinding
import com.screenshot.rockstar.models.ImageModel
import com.screenshot.rockstar.utils.ViewUtils.alertDialog
import com.screenshot.rockstar.utils.ViewUtils.showToast
import com.screenshot.rockstar.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiddenShotsFragment : Fragment(), HiddenImagesRCAdapter.OnImageClickListener {

    private lateinit var imageAdapter: HiddenImagesRCAdapter

    private var _binding: FragmentHiddenShotsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHiddenShotsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onViewClick()
        initRcView()
        getHiddenShots()

    }

    private fun onViewClick() {

        binding.undoHideItemsBtn.setOnClickListener {

            if (imageAdapter.getSelectedItems().isNotEmpty()) {
                revealSelectedAlert()
            } else {
                showToast(requireActivity(), getString(R.string.select_at_least_one))
            }
        }

        binding.ivSelectAll.setOnClickListener {
            selectUnselectAll()
        }

        binding.ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun initRcView() {

        imageAdapter = HiddenImagesRCAdapter(requireContext(), this)

        binding.hiddenShotsRcView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
            adapter = imageAdapter
        }

    }

    private fun getHiddenShots() {
        viewModel.getHiddenImages().observe(viewLifecycleOwner) {
            it?.let { images ->

                bindData(images)
            }
        }
    }

    private fun bindData(images: List<ImageModel>) {

        if (images.isEmpty()) {

            binding.emptyListTv.visibility = View.VISIBLE
            binding.llHiddenOptions.visibility = View.GONE
            binding.hiddenShotsRcView.visibility = View.GONE

            imageAdapter.clearAdapter()

        } else {

            binding.emptyListTv.visibility = View.GONE
            binding.llHiddenOptions.visibility = View.VISIBLE
            binding.hiddenShotsRcView.visibility = View.VISIBLE

            imageAdapter.addItems(images)
            selectAllIconColor()
        }
    }

    private fun selectUnselectAll() {
        imageAdapter.selectUnselectAllImage()
        incrementDecrementShots()
        selectAllIconColor()
    }

    private fun revealSelectedAlert() {
        alertDialog(requireActivity(),
            "Are you sure?",
            "This will reveal selected hidden shots.",
            "Reveal",
            "Cancel",
            false,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {

                    -1 -> {
                        unDoHide()
                        dialog.dismiss()
                    }

                    -2 -> {
                        dialog.dismiss()
                    }

                }
            })
    }

    private fun unDoHide() {

        val selectedItemList = imageAdapter.getSelectedItems()

        if (selectedItemList.isNotEmpty()) {

            val pathList = selectedItemList.map { it.path }

            viewModel.unDoHideImages(pathList, false)

            imageAdapter.setSelection(false)

            incrementDecrementShots()
        }
    }

    private fun incrementDecrementShots() {

        val selectedItemList = imageAdapter.getSelectedItems()

        if (imageAdapter.isSelection && selectedItemList.isNotEmpty()) {
            binding.selectedShotsCountTv.visibility = View.VISIBLE

            binding.selectedShotsCountTv.text = if (selectedItemList.size < 2) {
                "${selectedItemList.size} shot selected"
            } else {
                "${selectedItemList.size} shots selected"
            }

        } else {
            binding.selectedShotsCountTv.visibility = View.GONE
        }
    }

    override fun onImageClick(position: Int, view: View, item: ImageModel) {

        if (imageAdapter.isSelection) {

            imageAdapter.updateSelectedImage(item, position)
            incrementDecrementShots()

            if (imageAdapter.getSelectedItems().isEmpty()) {
                imageAdapter.setSelection(false)
            }

            selectAllIconColor()

        } else {
            showToast(requireActivity(), getString(R.string.long_press_to_select))
        }
    }

    override fun onImageLongClick(position: Int, view: View, item: ImageModel) {

        if (!imageAdapter.isSelection) {
            imageAdapter.setSelection(true)

            imageAdapter.updateSelectedImage(item, position)

            incrementDecrementShots()
        }
    }

    private fun selectAllIconColor() {

        if (imageAdapter.isAllSelected()) {

            binding.ivSelectAll.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.delete_button_ripple
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
        } else {

            binding.ivSelectAll.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
        }
    }
}

