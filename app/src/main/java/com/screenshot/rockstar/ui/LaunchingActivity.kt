package com.screenshot.rockstar.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.screenshot.rockstar.R
import com.screenshot.rockstar.interfaces.BillingSetupFinished
import com.screenshot.rockstar.interfaces.InitSetupListener
import com.screenshot.rockstar.interfaces.OnBoardFinishListener
import com.screenshot.rockstar.interfaces.SplashScreenFinishListener
import com.screenshot.rockstar.ui.fragments.BillingFragment
import com.screenshot.rockstar.ui.fragments.InitSetupFragment
import com.screenshot.rockstar.ui.fragments.OnBoardingFragment
import com.screenshot.rockstar.ui.fragments.SplashScreenFragment
import com.screenshot.rockstar.utils.Constants.Companion.BILLING_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.INIT_SETUP_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.ON_BOARD_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.SPLASH_SCREEN_FRAG_TAG
import com.screenshot.rockstar.utils.SessionManager
import com.screenshot.rockstar.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class LaunchingActivity : AppCompatActivity(),
    InitSetupListener, OnBoardFinishListener,
    BillingSetupFinished, SplashScreenFinishListener {

    //directory variables
    private lateinit var imageDirectory: String
    private val rootPath = Environment.getExternalStorageDirectory().toString()

    private lateinit var container: FragmentContainerView
    private lateinit var sessionManager: SessionManager

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launching)
        container = findViewById(R.id.launch_fragment_container)

        sessionManager = SessionManager(this)


        startSplashScreen()

    }

    private fun startSplashScreen() {
        supportFragmentManager.beginTransaction()
            .add(R.id.launch_fragment_container, SplashScreenFragment(), SPLASH_SCREEN_FRAG_TAG)
            .commit()
    }


    /**
     * launch fragment depending upon [sessionManager.getFirstTimePref()]
     */
    private fun firstTimeCheck() {

        if (sessionManager.getFirstTimePref()) {
            replaceFragment(ON_BOARD_FRAG_TAG)

        } else {
            replaceFragment(INIT_SETUP_FRAG_TAG)
        }
    }

    /**
     * replaceFragment when each Fragment listener called
     */
    private fun replaceFragment(replaceWith: String) {

        when (replaceWith) {

            BILLING_FRAG_TAG -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.launch_fragment_container,
                    BillingFragment(), BILLING_FRAG_TAG
                )
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commitAllowingStateLoss()
            }

            ON_BOARD_FRAG_TAG -> {
                Log.e("startingCheck", "onBoardFrag replace")
                supportFragmentManager.beginTransaction().replace(
                    R.id.launch_fragment_container,
                    OnBoardingFragment(), ON_BOARD_FRAG_TAG
                )
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commitAllowingStateLoss()
            }

            INIT_SETUP_FRAG_TAG -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.launch_fragment_container,
                    InitSetupFragment(), INIT_SETUP_FRAG_TAG
                )
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commitAllowingStateLoss()
            }
        }

    }


    /**
     *  Automatically select Directory based on Phone model
     */
    private fun autoDirectoryDetector(): String {
        val deviceModel = Build.MANUFACTURER.lowercase(Locale.getDefault())
        Log.d("MODEL", deviceModel)

        return when (deviceModel) {
            "oneplus", "nokia" -> {
                imageDirectory = "Pictures/Screenshots"
                val selectedPath = "$rootPath/$imageDirectory/"
                selectedPath
            }
            else -> {
                imageDirectory = "DCIM/Screenshots"
                val selectedPath = "$rootPath/$imageDirectory/"
                selectedPath
            }
        }

    }


    private fun setFirstTimePrefToSession(isFirst: Boolean) {
        sessionManager.addFirstTimePref(isFirst)
    }

    private fun setDefaultDirectoryPrefToSession() {
        val defaultDirectory = autoDirectoryDetector()
        sessionManager.apply {
            setDefaultDirectory(defaultDirectory)
            addDirectoryPref(defaultDirectory)
        }
    }

    private fun finishAndStartSearchActivity() {
        setFirstTimePrefToSession(false)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Listener methods
     */
    override fun initialSetupFinished() {
        finishAndStartSearchActivity()
    }

    override fun onBoardSetupFinish() {
        setDefaultDirectoryPrefToSession()
        replaceFragment(INIT_SETUP_FRAG_TAG)
    }

    override fun onBillingSetupFinished() {
        replaceFragment(INIT_SETUP_FRAG_TAG)
    }

    override fun onSplashScreenFinish() {
        Log.e("startingCheck", "splasreen listener called")
        firstTimeCheck()
    }


}