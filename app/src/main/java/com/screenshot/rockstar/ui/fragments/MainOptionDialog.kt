package com.screenshot.rockstar.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.screenshot.rockstar.R
import com.screenshot.rockstar.databinding.LayoutMainOptionDialogBinding


class MainOptionDialog(private val mainOptionListener: MainOptionListener) :
    BottomSheetDialogFragment() {

    private var _binding: LayoutMainOptionDialogBinding? = null
    private val binding get() = _binding!!

    private val items = arrayOf("Oldest Date", "Newest Date")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(),
            R.style.TopRoundBottomSheet)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = LayoutMainOptionDialogBinding.inflate(inflater, container, false)

        onViewClick()

        return binding.root
    }

    private fun onViewClick() {

        binding.tvSelectAll.setOnClickListener {
            mainOptionListener.onSelectAllClick()
            dismiss()
        }
        binding.tvViewOlder.setOnClickListener {
            showDialog()
            dismiss()
        }
        binding.tvChangeDirectory.setOnClickListener {
            mainOptionListener.onChangeDirectoryClick()
            dismiss()

        }
        /*btnUpgrade.setOnClickListener {
            mainOptionListener.onUpgradeClick()
            dismiss()

        }*/
        binding.tvSupport.setOnClickListener {
            mainOptionListener.onHelpClick()
            dismiss()
        }

        binding.tvCancel.setOnClickListener {
            dismiss()
        }

        binding.tvViewHiddenShort.setOnClickListener {
            mainOptionListener.onViewHiddenShotsClick()
            dismiss()
        }
    }

    private fun showDialog() {

        val mBottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.TopRoundBottomSheet)
        val sheetView: View =
            requireActivity().layoutInflater.inflate(R.layout.layout_sort_dialog, null)
        mBottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()

        val btnOldest: MaterialButton = sheetView.findViewById(R.id.btn_sort_alert_oldest)
        val btnNewest: MaterialButton = sheetView.findViewById(R.id.btn_sort_alert_newest)

        btnOldest.setOnClickListener {
            mainOptionListener.onSortByAscClick()
            mBottomSheetDialog.dismiss()
        }

        btnNewest.setOnClickListener {
            mainOptionListener.onSortByDescClick()
            mBottomSheetDialog.dismiss()
        }
    }

    interface MainOptionListener {
        fun onUpgradeClick()
        fun onSelectAllClick()
        fun onSortByAscClick()
        fun onSortByDescClick()
        fun onChangeDirectoryClick()
        fun onHelpClick()
        fun onViewHiddenShotsClick()
    }
}