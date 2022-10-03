package com.screenshot.rockstar.ui.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.screenshot.rockstar.R
import com.screenshot.rockstar.interfaces.InitSetupListener
import com.screenshot.rockstar.models.ImageModel
import com.screenshot.rockstar.utils.Constants.Companion.INIT_SETUP_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.REQUEST_CODE_CHANGE_DIRECTORY
import com.screenshot.rockstar.utils.Constants.Companion.REVENUE_ENTITLEMENT_ID_PRO
import com.screenshot.rockstar.utils.CustomFunctions.checkNetworkConnection
import com.screenshot.rockstar.utils.CustomFunctions.getCreationDate
import com.screenshot.rockstar.utils.CustomFunctions.getMimeType
import com.screenshot.rockstar.utils.CustomFunctions.handleBillingError
import com.screenshot.rockstar.utils.CustomFunctions.sortListPref
import com.screenshot.rockstar.utils.SessionManager
import com.screenshot.rockstar.utils.Status
import com.screenshot.rockstar.utils.SubscriptionType
import com.screenshot.rockstar.utils.TreeToRealPath
import com.screenshot.rockstar.utils.ViewUtils.alertDialog
import com.screenshot.rockstar.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class InitSetupFragment : Fragment(R.layout.fragment_init_setup) {

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var workingDirectory: String
    private lateinit var sessionManager: SessionManager
    private lateinit var newImageList:ArrayList<ImageModel>
    private lateinit var listener: InitSetupListener
    private var isFirstTime = true
    private var hasSubscription = false
    private var subscriptionStateChanged = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val fragView =  inflater.inflate(
            R.layout.fragment_init_setup,
            container, false
        )

        checkInternetConnection()
        return fragView
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            listener = activity as InitSetupListener
        }catch (e: ClassCastException){
            throw ClassCastException(activity.toString() + "must implement" + "InitSetupListener")
        }
    }

    private fun init(){
        createSession()
        //setupViewModel()
        processFileList()
    }


    private fun checkInternetConnection() {
        if (checkNetworkConnection(requireContext())){
            init()
            return
        }
        alertDialog(requireActivity(), "Internet connection unavailable",
        "",
        "Retry",
        "Exit",
        false,
        DialogInterface.OnClickListener { dialog, which ->
            when(which){

                -1->{
                    checkInternetConnection()
                    dialog.dismiss()
                }

                -2-> {
                    requireActivity().finishAndRemoveTask()
                }

            }
        })
    }

    private fun checkSubscriptionState(){
        Purchases.sharedInstance.getCustomerInfoWith({ error -> /* Optional error handling */
            Log.e("RevenueCat", "initSetup: error:")
            handleBillingError(requireActivity(), error)
        })
        { purchaserInfo ->

            if (purchaserInfo.entitlements[REVENUE_ENTITLEMENT_ID_PRO]?.isActive == true) {
                hasSubscription = true
                purchaserInfo.activeSubscriptions.forEach { sku ->
                    Log.e("RevenueCat", "initSetup: active subscription: $sku")
                    val subscriptionType: SubscriptionType = when (sku) {
                        SubscriptionType.MONTHLY_SUB.type -> SubscriptionType.MONTHLY_SUB
                        SubscriptionType.YEARLY_SUB.type -> SubscriptionType.YEARLY_SUB
                        else -> SubscriptionType.BASIC_SUB
                    }
                    sessionManager.addSubsPref(subscriptionType)
                }
            } else {
                hasSubscription = false
                sessionManager.addSubsPref(SubscriptionType.BASIC_SUB)
            }
            /*hasSubscription =
                (purchaserInfo.entitlements[REVENUE_ENTITLEMENT_ID_PRO]?.isActive == true)
            sessionManager.addSubsPref(hasSubscription)*/

            Log.wtf(
                "RevenueCat", "setupObserver: hasSub: $hasSubscription \n" +
                        "sessionManager: ${sessionManager.getSubscriptionPref()}\n" +
                        ""
            )
            restrictingDataBaseImages()
        }

    }

    private fun createSession() {
        sessionManager = SessionManager(requireContext())
        isFirstTime = sessionManager.getFirstTimePref()
        workingDirectory = sessionManager.getDirPref().toString()
    }


    private fun processFileList(){

        val fileList = getFilesFromDirectory()
        newImageList = ArrayList()
        fileList?.let {
            for (file in fileList) {
                Uri.parse(file.absolutePath)?.let {uri->

                    val mimeTypeMap = getMimeType(requireActivity(), uri)
                    val formattedDate = getCreationDate(file)
                    if (mimeTypeMap.contains("image")) {

                            val imageInfo =
                                ImageModel(
                                    path = file.absolutePath,
                                    text = null,
                                    creationTime = formattedDate,
                                    hasTag = false,
                                    first200 = false,
                                    isHidden = false
                                )

                        newImageList.add(imageInfo)
                    }
                }
                //val imageFile = File(uri.path!!)


            }

//            ViewUtils.sortListPref(newImageList, sessionManager.getSortPref(), object :ViewUtils.SortPreferenceListener{
//                override fun sortedList(list: List<ImageModel>) {
//                    if(isFirstTime) insertIntoDatabase(newImageList) else retrieveStoredImages()
//                }
//
//            })
            val filteredList = sortListPref(newImageList, sessionManager.getSortPref())

            if(isFirstTime) insertIntoDatabase(filteredList) else retrieveStoredImages()
        }
    }


    /**
     * retrieving files from [workingDirectory]
     */
    private fun getFilesFromDirectory(): Array<out File>? {

        val directory = File(workingDirectory)

        if (directory.exists()) {
            return directory.listFiles()
        }else{
            alertDialog(requireActivity(),
                resources.getString(R.string.directory_not_Found_title),
                "Directory not found or has no permission. Try changing another directory",
                resources.getString(R.string.set_manually),
                resources.getString(R.string.retry),
                false
            ) { dialog, which ->
                when (which) {
                    -1 -> {
                        showDirectory()
                    }
                    -2 -> {
                        processFileList()
                    }
                }
            }

            return null
        }
    }


    /**
     * [insertIntoDatabase] only call when user open app first time
     */
    private fun insertIntoDatabase(imageList:List<ImageModel>){
        viewModel.insertMultipleImage(imageList)
        hasSubscription = false
        sessionManager.addSubsPref(SubscriptionType.BASIC_SUB)
        allowAllImages()
        //checkSubscriptionState()
    }


    private fun deleteUnnecessaryImages(imageList: List<ImageModel>){
        val deleteImageList = ArrayList<String>()

        imageList.forEach { imageModel ->
            deleteImageList.add(imageModel.path)
        }
        viewModel.deleteMultipleImage(deleteImageList)
    }


    /**
     * Retrieve images when already data stored in DB
     */
    private fun retrieveStoredImages() {

        viewModel.getAllImage().observe(viewLifecycleOwner) {
            val checkResult = compareNewChanges(it as ArrayList<ImageModel>, newImageList)
            if (checkResult.first) {
                deleteUnnecessaryImages(checkResult.second)
                insertIntoDatabase(checkResult.third)
            }
            hasSubscription = false
            sessionManager.addSubsPref(SubscriptionType.BASIC_SUB)
            allowAllImages()

            //checkSubscriptionState()
        }


    }

    /**
     * Compare two list for its dissimilarities
     * @return  Kotlin Triple with First value has Boolean indicates two list differs or not,
     *          second returns items to delete from old,
     *          third returns items to add in old
     */
    private fun compareNewChanges(oldList:ArrayList<ImageModel>, newList:ArrayList<ImageModel> ): Triple<Boolean, List<ImageModel>, List<ImageModel>> {
        var isDiffer = false
        val deleteItems = ArrayList<ImageModel>()
        val addItems = ArrayList<ImageModel>()
        val sum = newList + oldList
        val filteredList = sum.groupBy { it.path }
            .filter { it.value.size == 1 }
            .flatMap { it.value }


        for (filter in filteredList){
            var found  = false
            for(old in oldList){
                if (filter.path == old.path){
                    deleteItems.add(old)
                    found = true
                    break
                }
            }
            if (found){
                continue
            }

            addItems.add(filter)
        }

        if (filteredList.isNotEmpty()) isDiffer = true

        //return true if any changes found along with items to delete and items to add
        //return Triple(false, deleteItems, addItems)

        //return difference from both array giving newList items first then oldList
        return Triple(isDiffer, deleteItems, addItems)
    //logic to compare two list
    }

    private fun restrictingDataBaseImages(){
        Log.d(INIT_SETUP_FRAG_TAG, "restrictingDataBaseImages: ")
        if (hasSubscription)
            allowAllImages()
        else
            allowFirst200Images()

    }

    /**
     * Allow images according to user subscription
     */
    private fun allowFirst200Images(){
        viewModel.setFirst200().observe(viewLifecycleOwner) {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        viewModel.setLast200().observe(viewLifecycleOwner) { last200 ->
                            last200?.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        onSetupFinish()
                                    }
                                    Status.ERROR -> {

                                    }
                                    Status.LOADING -> {

                                    }
                                }
                            }
                        }
                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }
                }
            }
        }
    }

    private fun allowAllImages(){
        viewModel.unlockAllImageOnPurchase().observe(viewLifecycleOwner) {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        onSetupFinish()
                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }
                }
            }
        }
    }

    private fun onSetupFinish(){
        listener.initialSetupFinished()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){

            REQUEST_CODE_CHANGE_DIRECTORY->{
                data?.let {
                    val uri = data.data
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(
                        uri,
                        DocumentsContract.getTreeDocumentId(uri)

                    )
                    val selectedPath = TreeToRealPath().getRealPathFromURI(requireContext(), docUri)

                    if (selectedPath != null) {

                        sessionManager.addDirectoryPref(selectedPath)
                        workingDirectory = selectedPath
                        processFileList()
                    } else {

                        processFileList()
                    }

                }
            }
        }
    }

    private fun showDirectory(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        startActivityForResult(
            Intent.createChooser(intent, "Choose directory"),
            REQUEST_CODE_CHANGE_DIRECTORY
        )

    }
}