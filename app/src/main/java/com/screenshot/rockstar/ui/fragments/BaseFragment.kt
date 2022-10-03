package com.screenshot.rockstar.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.screenshot.rockstar.utils.Constants
import com.screenshot.rockstar.utils.CustomFunctions

open class BaseFragment:Fragment() {

    private lateinit var mFirebaseAnalytics : FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirebaseAnalytics = Firebase.analytics

    }

    fun trackScreen(screenName:String){
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW){
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }

        val eventValues: MutableMap<String, String> = HashMap()
        eventValues[screenName] = screenName
        CustomFunctions.logEvent(requireContext(), Constants.SCREEN_EVENT, eventValues)
    }

}