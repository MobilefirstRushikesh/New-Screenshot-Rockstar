package com.screenshot.rockstar.ui.fragments

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.screenshot.rockstar.R
import com.screenshot.rockstar.adapters.OnBoardAdapter
import com.screenshot.rockstar.interfaces.OnBoardFinishListener
import com.screenshot.rockstar.utils.Constants.Companion.REQUEST_CODE_STORAGE_PERMISSION
import com.screenshot.rockstar.utils.CustomFunctions.hasPermissions
import com.screenshot.rockstar.utils.SessionManager
import com.screenshot.rockstar.utils.ViewUtils.showToast
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vmadalin.easypermissions.models.PermissionRequest


class OnBoardingFragment : Fragment(), ViewPager.OnPageChangeListener, EasyPermissions.PermissionCallbacks {

    private lateinit var imageDirectory:String
    private lateinit var listener: OnBoardFinishListener
    private val rootPath = Environment.getExternalStorageDirectory().toString()
    lateinit var onBoardViewPager: ViewPager
    lateinit var dotsLayout: LinearLayout

    private lateinit var OnBoardAdapter: OnBoardAdapter
    private lateinit var btnNext: ImageView
    lateinit var btnSkip: Button
    lateinit var btnAuthorize: Button
    lateinit var animation: Animation
    private lateinit var sessionManager: SessionManager
    private lateinit var fragView: View
    var currentPos: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragView =  inflater.inflate(
            R.layout.activity_on_boarding,
            container, false
        )

        dotsLayout = fragView.findViewById(R.id.dots)
        onBoardViewPager = fragView.findViewById(R.id.viewPager_onBoard)
        btnNext = fragView.findViewById(R.id.onBoard_btn_next)
        btnSkip = fragView.findViewById(R.id.btn_onBoard_skip)
        btnAuthorize = fragView.findViewById(R.id.btn_onBoard_authorised)
        OnBoardAdapter = OnBoardAdapter(requireContext())
        onBoardViewPager.adapter = OnBoardAdapter

        sessionManager = SessionManager(requireContext())
        onBoardViewPager.addOnPageChangeListener(this)


        btnAuthorize.setOnClickListener {
            authorisePermission()
        }
        btnNext.setOnClickListener {
            next()
        }
        btnSkip.setOnClickListener {
            authorisePermission()
        }

        addDots(0)


        return fragView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            listener = activity as OnBoardFinishListener
        }catch (e: ClassCastException){
            throw ClassCastException(activity.toString() + "must implement" + "InitSetupListener")
        }
    }

    /**
     *  Dots code
     */
    private fun addDots(position: Int) {
        val dots = arrayOfNulls<TextView>(3)
        dotsLayout.removeAllViews()
        for ((i, txtview) in dots.withIndex()) {
            dots[i] = TextView(requireContext())
            dots[i]?.text = Html.fromHtml("•")
            dots[i]?.setTextColor(requireActivity().resources.getColor(R.color.white))
            dots[i]?.alpha = 0.50F
            dots[i]?.textSize = 25F
            dotsLayout.addView(dots[i])

        }
        if (dots.isNotEmpty()) {
            dots[position]?.alpha = 1F
            dots[position]?.setTextColor(resources.getColor(R.color.white))
        }
    }

    /**
     * Viewpager code
     */
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        addDots(position)
        currentPos = position
        when (position) {
            0 -> {
                btnNext.visibility = View.VISIBLE
                btnAuthorize.visibility = View.GONE
                btnSkip.visibility = View.VISIBLE
            }
            1 -> {
                btnNext.visibility = View.VISIBLE
                btnAuthorize.visibility = View.GONE
                btnSkip.visibility = View.VISIBLE
            }
            else -> {
                btnNext.visibility = View.GONE
                btnSkip.visibility = View.GONE
                animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bottom_animation)
                btnAuthorize.animation = animation
                btnAuthorize.visibility = View.VISIBLE
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }


    private fun next() {
        onBoardViewPager.currentItem = currentPos + 1
    }


    /**
     * Storage permissions
     */

    private fun authorisePermission(){
        if (hasPermissions(requireContext()))
            listener.onBoardSetupFinish()

        else{
            val request = PermissionRequest.Builder(requireContext())
                .code(REQUEST_CODE_STORAGE_PERMISSION)
                .perms(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE))
                .theme(R.style.ThemeMaterialAlertDialog)
                .rationale("Storage permission required to use this app")
                .positiveButtonText("Grant")
                .negativeButtonText("Deny")
                .build()
            EasyPermissions.requestPermissions(this, request)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            SettingsDialog.Builder(requireContext()).build().show()
        }else{
            authorisePermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        showToast(requireActivity(), "Permissions Granted!")
        listener.onBoardSetupFinish()
    }

    /*private fun authorisePermission() {

        if(hasPermissions(requireContext())){
            listener.onBoardSetupFinish()

        }else{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ){
                alertDialog(requireContext(), "Permission required to run this app",
                    "",
                    "OK",
                    "",
                    false,
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            -1 -> {
                                showAllFilePermissionSetting()
                                dialog.dismiss()
                            }
                        }
                    })
            }else{
                EasyPermissions.requestPermissions(
                    this,
                    "Storage permission required to use this app",
                    REQUEST_CODE_STORAGE_PERMISSION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ALL_FILE_ACCESS_PERMISSION) {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
               if (Environment.isExternalStorageManager()){
                    listener.onBoardSetupFinish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ){

                alertDialog(requireContext(), "Permission required to run this app",
                "",
                "OK",
                "",
                false,
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            -1 -> {
                                showAllFilePermissionSetting()
                                dialog.dismiss()
                            }
                        }
                    })

            }else{
                AppSettingsDialog.Builder(this).build().show()
            }*/
            //AppSettingsDialog.Builder(this).build().show()
            SettingsDialog.Builder(requireContext()).build().show()
        }else{
            authorisePermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        showToast(requireActivity(), "Permissions Granted!")
        listener.onBoardSetupFinish()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showAllFilePermissionSetting(){
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(
                String.format(
                    "package:%s",
                    requireActivity().applicationContext.packageName
                )
            )
            startActivityForResult(intent, REQUEST_CODE_ALL_FILE_ACCESS_PERMISSION)
        } catch (e: Exception) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivityForResult(intent, REQUEST_CODE_ALL_FILE_ACCESS_PERMISSION)
        }
    }*/
}