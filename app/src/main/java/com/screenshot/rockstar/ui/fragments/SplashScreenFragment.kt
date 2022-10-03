package com.screenshot.rockstar.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.screenshot.rockstar.R
import com.screenshot.rockstar.interfaces.SplashScreenFinishListener


class SplashScreenFragment : Fragment() {

    private val splashDelayedTime:Long = 3000
    private lateinit var listener: SplashScreenFinishListener
    private lateinit var fragView: View


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.e("startingCheck", "splashONcreate")
        fragView =  inflater.inflate(
            R.layout.activity_splash_screen,
            container, false
        )

        Handler(Looper.getMainLooper()).postDelayed({

           listener.onSplashScreenFinish()

        }, splashDelayedTime)



        return  fragView
    }


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try{
            listener = activity as SplashScreenFinishListener
        }catch (e: ClassCastException){
            throw ClassCastException(activity.toString() + "must implement" + "InitSetupListener")
        }
    }




}