package com.screenshot.rockstar.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.airbnb.lottie.LottieAnimationView
import com.screenshot.rockstar.R


class OnBoardAdapter(val context: Context): PagerAdapter(){

    private lateinit var layoutInflater: LayoutInflater

    val images = arrayOf(
        R.raw.de_clutter_manage_new,
        R.raw.privacy_first_new,
        R.raw.image_permission_new
    )

    /*val headings = arrayOf(
        R.string.magic_ai,
        R.string.privacy_first,
        R.string.image_permission
    )*/

    private val descriptions = arrayOf(
        "Screenshot Rockstar uses AI to make all your screenshots searchable. Whether you use screenshots to keep notes, bills, etc - now simply just search for what you are looking for instead of endless scrolling.",
        "At Screenshot Rockstar your privacy is primary, No screenshot or related data leaves your phone. The analysis of your screenshots happen on your phone natively and not server side.",
        "On clicking process please make sure you give Screenshot Rockstar permission to your images. As mentioned on the earlier point Privacy is of utmost importance to us. Your images and the data never leaves your phone."
    )


    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        //layoutInflater = context.getSystemService(context.LAYOUT_INFLATER_SERVICE)
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.onboard_slides_layout, container, false)

        val image: LottieAnimationView = view.findViewById(R.id.onBoard_img)
        //val title: TextView = view.findViewById(R.id.onBoard_title)
        val desc: TextView = view.findViewById(R.id.onBoard_desc)

        image.setAnimation(images[position])
        desc.text = descriptions[position]

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }
}