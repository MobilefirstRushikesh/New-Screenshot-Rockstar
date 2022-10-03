package com.screenshot.rockstar.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.screenshot.rockstar.R


class ProcessBottomDialog(private val actionTitle:String,
                          private val totalItemCount : Int,
                          private val progressBottomListener: ProgressBottomListener
): BottomSheetDialogFragment() {

    private lateinit var txtCompleted: TextView
    private lateinit var txtTitle:TextView
    private lateinit var txtTotal:TextView
    private lateinit var progressBar: ContentLoadingProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val progressView = inflater.inflate(R.layout.layout_progress_bottom_sheet, container, false)

        txtCompleted = progressView.findViewById(R.id.bottomProgress_txt_completed)
        txtTitle = progressView.findViewById(R.id.bottomProgress_txt_title)
        txtTotal = progressView.findViewById(R.id.bottomProgress_txt_total)
        progressBar = progressView.findViewById(R.id.bottomProgress_progressBar)

        setupUi()

        return progressView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       progressBottomListener.onBottomViewCreated()

    }

    private fun setupUi() {
        txtTotal.text = totalItemCount.toString()
        txtTitle.text = actionTitle
    }

    fun updateCompletedText(completedCount:Int){
        txtCompleted.text = completedCount.toString()
        progressBar.progress = completedCount * 100 / totalItemCount
    }

    interface ProgressBottomListener{
        fun onBottomViewCreated()
    }

}