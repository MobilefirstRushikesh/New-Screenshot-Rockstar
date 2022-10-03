package com.screenshot.rockstar.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.screenshot.rockstar.R
import com.screenshot.rockstar.ui.fragments.SplashScreenFragment
import dagger.hilt.android.internal.managers.ViewComponentManager
import kotlinx.android.synthetic.main.raw_albums.*
import kotlin.system.exitProcess


object ViewUtils {

    fun alertDialog(
        sActivity: Context?,
        title: String?,
        message: String?,
        yes: String?,
        no: String?,
        cancelable:Boolean,
        dialogInterface: DialogInterface.OnClickListener?
    ) {
        val builder =
            MaterialAlertDialogBuilder(sActivity!!, R.style.ThemeMaterialAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable)
                .setPositiveButton(yes, dialogInterface)
                .setNegativeButton(no, dialogInterface)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideKeyboard(context: Context) {

        val mContex: Context = if (context is ViewComponentManager.FragmentContextWrapper) context.baseContext else context
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // check if no view has focus:
        val v = (mContex as Activity).currentFocus ?: return
        inputManager.hideSoftInputFromWindow(v.windowToken, 0)

    }

    fun hideKeyBoard(activity: AppCompatActivity) {

        if (activity.isFinishing) return

        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun hideKeyBoard(activity: AppCompatActivity, view: View) {
        if (activity.isFinishing) return

        val imm = (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        imm.hideSoftInputFromInputMethod(view.windowToken, 0)
        view.clearFocus()
    }

    fun showToast(activity: Activity, massage: String){
        val toast = Toast(activity)
        toast.duration = Toast.LENGTH_SHORT
        val inflater = activity.layoutInflater

        val view: View = inflater.inflate(
            R.layout.layout_toast, activity.findViewById(R.id.toast_constraint_layout))
        val toastTxt: TextView =view.findViewById(R.id.toast_txt)
        toastTxt.text =  massage
        toast.view = view
        toast.show()
    }

    fun createChip(activity: Activity,
                   chipGroup: ChipGroup,
                   chipText:String,
                   layoutId:Int,
                   onclickEnable:Boolean,
                   longClickEnable: Boolean,
                   chipListener: ChipListener,
                   closeIconVisibility:Boolean
    ){
        val chip = activity.layoutInflater.inflate(layoutId, activity.container, false) as Chip
        chip.text = chipText
        chip.isChipIconVisible = true
        chip.isCloseIconVisible = closeIconVisibility
        chip.isClickable = true
        chip.isCheckable = false

        if (longClickEnable){
            chip.setOnLongClickListener {
                if(!closeIconVisibility) {
                    when (chip.isCloseIconVisible) {
                        true -> chip.isCloseIconVisible = false
                        false -> chip.isCloseIconVisible = true
                    }
                }

                true
            }
        }

        if (onclickEnable){
            chip.setOnClickListener {
                chipListener.chipOnClick(chip.text as String)
            }
        }

        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip as View)
            chipListener.chipRemoved(chip.text as String , chip)
        }

        chipGroup.addView(chip as View)
    }

   fun killApp(context: Context){
        val mStartActivity = Intent(context, SplashScreenFragment::class.java)
        val mPendingIntentId = 123456
        val mPendingIntent = PendingIntent.getActivity(
            context,
            mPendingIntentId,
            mStartActivity,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 100 , mPendingIntent)
        exitProcess(0)
   }

    interface ChipListener{
        fun chipRemoved(text: String, chip: Chip)
        fun chipOnClick(chipText: String)
    }


}