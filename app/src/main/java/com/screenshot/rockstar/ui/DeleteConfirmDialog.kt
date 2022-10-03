package com.screenshot.rockstar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.screenshot.rockstar.R

class DeleteConfirmDialog(
    private val deleteImageCount:Int,
    private val deleteDialogListener: DeleteDialogListener
): BottomSheetDialogFragment() {

    private lateinit var btnCancel: Button
    private lateinit var btnDelete: Button
    private lateinit var dialogView: View
    private var selectedImages: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        dialogView = inflater.inflate(R.layout.delete_dialog_layout, container, false)
        btnCancel = dialogView.findViewById(R.id.btn_deleteDialog_cancel)
        btnDelete = dialogView.findViewById(R.id.btn_deleteDialog_delete)

        if (deleteImageCount > 1){
            btnDelete.text = "Delete $deleteImageCount photos"
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnDelete.setOnClickListener {
            deleteDialogListener.onDeleteClick()
            dismiss()
        }

        return dialogView
    }

    override fun onStart() {
        super.onStart()
        val bottomSheetBehavior = BottomSheetBehavior.from(dialogView.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    interface DeleteDialogListener{
        fun onDeleteClick()

    }
}