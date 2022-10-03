package com.screenshot.rockstar.ui

import android.content.Context
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.screenshot.rockstar.R
import com.screenshot.rockstar.interfaces.*
import com.screenshot.rockstar.ui.fragments.*
import com.screenshot.rockstar.utils.Constants.Companion.BILLING_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.IMAGE_DETAIL_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.INIT_SETUP_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.ON_BOARD_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.SEARCH_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.SPLASH_SCREEN_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.VIEW_HIDDEN_SHOTS_FRAG_TAG
import com.screenshot.rockstar.utils.SessionManager
import com.screenshot.rockstar.utils.ViewUtils.showToast
import com.screenshot.rockstar.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ChangeFragmentListener, BillingSetupFinished,
    InitSetupListener, OnBoardFinishListener, SplashScreenFinishListener {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var doubleBackToExitPressedOnce = false

    //directory variables
    private lateinit var imageDirectory:String
    private val rootPath = Environment.getExternalStorageDirectory().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        sessionManager = SessionManager(this)

        init()

    }

    private fun init(){
        if(sessionManager.getFirstTimePref()){
            addFragmentToMainActivity(SPLASH_SCREEN_FRAG_TAG, false)
        }else{
            addFragmentToMainActivity(INIT_SETUP_FRAG_TAG,false)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            Log.d("BackStackCount", "BackStack count: ${supportFragmentManager.backStackEntryCount}")
        }
    }

    /**
     * Add fragment to [MainActivity]
     * @param addToBackStack  true if want to add fragment to backStack
     */
    private fun addFragmentToMainActivity(fragmentTag: String, addToBackStack: Boolean){

        val fragment: Fragment = when(fragmentTag){
            IMAGE_DETAIL_FRAG_TAG -> ImageDetailFragment()
            SEARCH_FRAG_TAG -> SearchFragment()
            BILLING_FRAG_TAG -> BillingFragment()
            ON_BOARD_FRAG_TAG -> OnBoardingFragment()
            INIT_SETUP_FRAG_TAG-> InitSetupFragment()
            SPLASH_SCREEN_FRAG_TAG-> SplashScreenFragment()
            VIEW_HIDDEN_SHOTS_FRAG_TAG->HiddenShotsFragment()
            else-> SearchFragment()
        }

        if (addToBackStack){
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.main_fragment_container, fragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commit()
        }else{
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.main_fragment_container, fragment, fragmentTag)
                .commit()
        }

    }


    /**
     * replace fragment with current [MainActivity] fragment
     */
    private fun replaceFragmentToMainActivity(fragmentTag: String, addToBackStack: Boolean){

        val fragment:Fragment = when(fragmentTag){

            IMAGE_DETAIL_FRAG_TAG -> ImageDetailFragment()
            SEARCH_FRAG_TAG -> SearchFragment()
            BILLING_FRAG_TAG -> BillingFragment()
            ON_BOARD_FRAG_TAG -> OnBoardingFragment()
            INIT_SETUP_FRAG_TAG-> InitSetupFragment()
            else-> SearchFragment()
        }

        if (addToBackStack){
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_fragment_container, fragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commitAllowingStateLoss()
        }else{
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_fragment_container, fragment, fragmentTag)
                .commitAllowingStateLoss()
        }
    }

    private fun removeFragment(fragmentTag: String){
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (fragment != null)
            supportFragmentManager.beginTransaction().remove(fragment).commit()
    }


//    override fun onBackPressed() {
//
//        if (supportFragmentManager.backStackEntryCount == 0) {
//            this.finish()
//        } else {
//            Log.d(MAIN_ACTIVITY_TAG, "onBackPressed: popBackStack ")
//            supportFragmentManager.popBackStack()
//        }
//        super.onBackPressed()
//    }

    /**
     * launch fragment depending upon [sessionManager.getFirstTimePref()]
     */
    private fun firstTimeCheck(){
        if (sessionManager.getFirstTimePref()){
            replaceFragmentToMainActivity(ON_BOARD_FRAG_TAG, false)

        }else{
            replaceFragmentToMainActivity(INIT_SETUP_FRAG_TAG, false)
        }
    }

    /**
     *  Automatically select Directory based on Phone model
     */

    private fun autoDirectoryDetector():String{
        val deviceModel = Build.MANUFACTURER.lowercase(Locale.ROOT)
        Log.d("MODEL", deviceModel)

        return when(deviceModel){
            "oneplus", "nokia", "Nokia" -> {
                imageDirectory = "Pictures/Screenshots"
                val selectedPath = "$rootPath/$imageDirectory/"
                selectedPath
            }
            else->{
                imageDirectory = "DCIM/Screenshots"
                val selectedPath  = "$rootPath/$imageDirectory/"
                selectedPath
            }
        }

    }

    private fun setFirstTimePrefToSession(isFirst:Boolean){
        sessionManager.addFirstTimePref(isFirst)
    }

    private fun setDefaultDirectoryPrefToSession(){
        val defaultDirectory = autoDirectoryDetector()
        sessionManager.apply {
            setDefaultDirectory(defaultDirectory)
            addDirectoryPref(defaultDirectory)
        }
    }

    private fun findFragment(fragmentTag: String): Fragment? {
        if (supportFragmentManager.findFragmentByTag(fragmentTag) != null){
            return supportFragmentManager.findFragmentByTag(fragmentTag)
        }
        return null
    }

    override fun addFragment(fragmentTag: String, addToBackStack: Boolean) {
        addFragmentToMainActivity(fragmentTag, addToBackStack)
    }

    override fun replaceFragment(fragmentTag: String, addToBackStack: Boolean) {
        replaceFragmentToMainActivity(fragmentTag, addToBackStack)
    }

    override fun popBackStack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
    }

    override fun removeAndAddFragment(
        removeFragmentTag: String,
        addFragmentTag: String,
        addToBackStack: Boolean,
        popCurrentFragFromBackStack: Boolean
    ) {

        if (popCurrentFragFromBackStack)
            if (supportFragmentManager.backStackEntryCount > 0)
                supportFragmentManager.popBackStack()

        findFragment(removeFragmentTag)?.let { fragment ->
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }

        addFragmentToMainActivity(addFragmentTag, addToBackStack)
    }


    override fun onBillingSetupFinished() {
        if (sessionManager.getFirstTimePref()){
            replaceFragment(INIT_SETUP_FRAG_TAG, false)
        }

    }

    override fun initialSetupFinished() {
        setFirstTimePrefToSession(false)
        replaceFragment(SEARCH_FRAG_TAG, false)
    }

    override fun onBoardSetupFinish() {
        setDefaultDirectoryPrefToSession()
        // replaceFragment(BILLING_FRAG_TAG, false)
        replaceFragment(INIT_SETUP_FRAG_TAG,false)
    }

    override fun onSplashScreenFinish() {
        firstTimeCheck()
    }


    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        if (count == 0) {
            //super.onBackPressed()
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            this.doubleBackToExitPressedOnce = true
            showToast(this,"Press again to exit.")

            Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }




}