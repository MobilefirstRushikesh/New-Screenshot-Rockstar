package com.screenshot.rockstar.ads

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class BottomBarAd(private val activity: AppCompatActivity) {

     @SuppressLint("MissingPermission")
     fun initializeAd(): AdRequest {
        MobileAds.initialize(activity)
         return AdRequest.Builder().build()
    }

}