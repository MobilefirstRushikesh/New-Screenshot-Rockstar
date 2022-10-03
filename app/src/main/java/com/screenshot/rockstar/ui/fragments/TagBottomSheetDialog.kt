package com.screenshot.rockstar.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.screenshot.rockstar.R
import com.screenshot.rockstar.models.TagModel
import com.screenshot.rockstar.utils.ViewUtils
import com.screenshot.rockstar.utils.ViewUtils.createChip
import com.screenshot.rockstar.viewModel.MainViewModel


class TagBottomSheetDialog(private val bottomSheetListener: TagSheetListener) :
    BottomSheetDialogFragment() {

    lateinit var path: String
    private lateinit var editTag: EditText
    private lateinit var btnClose: ImageView
    private lateinit var btnDone: Button
    private lateinit var newChipGroup: ChipGroup
    private lateinit var oldChipGroup: ChipGroup
    private lateinit var dialogView: View
    private var tagList = ArrayList<String>()

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(
            requireContext(),
            R.style.TopRoundBottomSheet
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        dialogView = inflater.inflate(R.layout.tag_bottomsheet_layout, container, false)

        editTag = dialogView.findViewById(R.id.edit_tag)
        btnClose = dialogView.findViewById(R.id.btn__tagSheet_close)
        btnDone = dialogView.findViewById(R.id.btn_tagSheet_done)
        newChipGroup = dialogView.findViewById(R.id.tagSheet_NewChipGroup)
        oldChipGroup = dialogView.findViewById(R.id.tagsheet_OldChipGroup)

        init()
        return dialogView
    }


    override fun onStart() {
        super.onStart()
        val bottomSheetBehavior = BottomSheetBehavior.from(dialogView.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun init() {
        setupUi()
        getOldTags()
    }

    private fun setupUi() {

        btnClose.setOnClickListener { dismiss() }

        btnDone.setOnClickListener {
            val text = editTag.text.trim().toString()
            if (text.isNotEmpty()) {
                tagList.add(text)
                bottomSheetListener.onTagDoneClicked(tagList)
            } else {
                if (tagList.isNotEmpty()) {
                    bottomSheetListener.onTagDoneClicked(tagList)
                }
            }
            dismiss()
        }

        editTag.setOnEditorActionListener { _, actionId, _ ->
            val tagTxt = editTag.text.trim().toString()
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE && tagTxt.isNotEmpty()) {
                addNewChip(tagTxt)
                tagList.add(tagTxt)
                handled = true
                editTag.text.clear()


            }
            handled
        }
        editTag.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (editTag.text.isNullOrEmpty() && tagList.isEmpty()) {

                    btnDone.setBackgroundResource(R.drawable.bg_edit_tag)
                } else {
                    if (s.trim().toString().isNotEmpty()) {
                        btnDone.setBackgroundResource(R.drawable.add_tag_done_bg_accent_pink)
                    }
                }

            }
        })
    }


    //--------------------Add New Chip to ChipGroup----------------------------//
    private fun addNewChip(tag: String) {

        createChip(requireActivity(),
            newChipGroup,
            tag, R.layout.tag_chip_layout,
            onclickEnable = false,
            longClickEnable = true,
            object : ViewUtils.ChipListener {
                override fun chipRemoved(text: String, chip: Chip) {
                    tagList.remove(text)
                    if (tagList.isEmpty()) {
                        btnDone.setBackgroundResource(R.drawable.bg_edit_tag)
                    }
                }

                override fun chipOnClick(chipText: String) {

                }

            }, true
        )
    }

    //--------------------Get Chip to ChipGroup----------------------------//
    private fun getOldTags() {
        viewModel.getAllTags().observe(viewLifecycleOwner) {
            it?.let { list: List<TagModel> ->
                list.forEach { tagModel ->
                    createChip(requireActivity(),
                        oldChipGroup,
                        tagModel.tagName,
                        R.layout.old_tag_chip_layout,
                        onclickEnable = true,
                        longClickEnable = false,
                        object : ViewUtils.ChipListener {
                            override fun chipRemoved(text: String, chip: Chip) {

                            }

                            override fun chipOnClick(chipText: String) {
                                addNewChip(chipText)
                                tagList.add(chipText)
                                btnDone.setBackgroundResource(R.drawable.add_tag_done_bg_accent_pink)
                            }

                        }, false
                    )
                }
            }
        }
    }

    //-------------------------Listener between this and ImageDetailActivity---------------------//
    interface TagSheetListener {
        fun onTagDoneClicked(tags: ArrayList<String>)
    }
}