package com.screenshot.rockstar.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.screenshot.rockstar.R
import com.screenshot.rockstar.adapters.MainAdapter
import com.screenshot.rockstar.adapters.MainTagAdapter
import com.screenshot.rockstar.interfaces.ChangeFragmentListener
import com.screenshot.rockstar.models.ImageModel
import com.screenshot.rockstar.models.ImageTagCrossRef
import com.screenshot.rockstar.models.TagModel
import com.screenshot.rockstar.ui.DeleteConfirmDialog
import com.screenshot.rockstar.utils.*
import com.screenshot.rockstar.utils.Constants.Companion.ACTION_INSERT
import com.screenshot.rockstar.utils.Constants.Companion.ACTION_UPDATE
import com.screenshot.rockstar.utils.Constants.Companion.BILLING_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.DELETE_CONFIRM_DIALOG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.IMAGE_CREATION_TIME
import com.screenshot.rockstar.utils.Constants.Companion.IMAGE_DETAIL_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.IMAGE_PATH
import com.screenshot.rockstar.utils.Constants.Companion.IMAGE_PATH_FILE
import com.screenshot.rockstar.utils.Constants.Companion.IMAGE_TEXT
import com.screenshot.rockstar.utils.Constants.Companion.INIT_SETUP_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.INTENT_SERVICE_PATH
import com.screenshot.rockstar.utils.Constants.Companion.MAIN_OPTION_MENU_DIALOG
import com.screenshot.rockstar.utils.Constants.Companion.MULTI_SELECTION_MODE
import com.screenshot.rockstar.utils.Constants.Companion.PROCESS_DIALOG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.REQUEST_CODE_CHANGE_DIRECTORY
import com.screenshot.rockstar.utils.Constants.Companion.SINGLE_SELECTION_MODE
import com.screenshot.rockstar.utils.Constants.Companion.SORT_PREFERENCE_DATE_ASC
import com.screenshot.rockstar.utils.Constants.Companion.SORT_PREFERENCE_DATE_DESC
import com.screenshot.rockstar.utils.Constants.Companion.TAG_BOTTOM_DIALOG_TAG
import com.screenshot.rockstar.utils.CustomFunctions.getCreationDate
import com.screenshot.rockstar.utils.CustomFunctions.getMimeType
import com.screenshot.rockstar.utils.CustomFunctions.getResizedBitmap
import com.screenshot.rockstar.utils.ViewUtils.alertDialog
import com.screenshot.rockstar.utils.ViewUtils.createChip
import com.screenshot.rockstar.utils.ViewUtils.showToast
import com.screenshot.rockstar.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutionException


@AndroidEntryPoint
class SearchFragment : BaseFragment(), ViewModelStoreOwner,
    MainTagAdapter.TagItemClickListener,
    MainAdapter.OnImageClickListener, FileSystemObserverService.FileObserverListener {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var fragView: View
    private lateinit var imageAdapter: MainAdapter
    private lateinit var tagAdapter: MainTagAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var sortPreference: String
    private lateinit var workingDirectory: String
    private var filterTagList = ArrayList<String>()
    private var filterMutable: MutableLiveData<ArrayList<String>>? = null


    //views
    private lateinit var rvImages: RecyclerView
    private lateinit var rvTag: RecyclerView
    private lateinit var editSearchBar: EditText
    private lateinit var processDialog: RelativeLayout
    private lateinit var relMultiSelectBar: RelativeLayout
    private lateinit var linearMainBar: LinearLayout
    private lateinit var btnMultiCancel: MaterialButton
    private lateinit var btnMultiShare: ImageButton
    private lateinit var btnMultiDelete: ImageButton
    private lateinit var btnMultiTag: ImageButton
    private lateinit var imgMainOption: ImageView
    private lateinit var searchBarCancel: ImageView
    private lateinit var txtOops: TextView
    private lateinit var btnhide: ImageView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: LinearProgressIndicator


    //listener
    private lateinit var changeFragmentListener: ChangeFragmentListener
    private lateinit var fileObserverListener: FileSystemObserverService.FileObserverListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate:")
        //setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("onCreateView:")
        fragView = inflater.inflate(
            R.layout.activity_search,
            container, false
        )
        rvImages = fragView.findViewById(R.id.rvuser)
        rvTag = fragView.findViewById(R.id.rvTag)
        editSearchBar = fragView.findViewById(R.id.etSearch)
        processDialog = fragView.findViewById(R.id.frmProgress)
        relMultiSelectBar = fragView.findViewById(R.id.rel_multiselectBar)
        linearMainBar = fragView.findViewById(R.id.linear_MainBar)
        btnMultiCancel = fragView.findViewById(R.id.btn_multiSelect_cancel)
        btnMultiDelete = fragView.findViewById(R.id.btn_multiSelect_delete)
        btnhide = fragView.findViewById(R.id.btn_multiSelect_hide)
        btnMultiShare = fragView.findViewById(R.id.btn_multiSelect_share)
        btnMultiTag = fragView.findViewById(R.id.btn_multiSelect_tag)
        imgMainOption = fragView.findViewById(R.id.img_search_detail)
        searchBarCancel = fragView.findViewById(R.id.search_cancel)
        txtOops = fragView.findViewById(R.id.txt_oops)
        swipeRefreshLayout = fragView.findViewById(R.id.search_swipeRefreshLayout)
        progressBar = fragView.findViewById(R.id.prBar)

        init()

        return fragView
    }

    companion object {
        var displayHeight: Int = 0
        var displayWidth: Int = 0

        //        var currentViewedImages = MutableLiveData<List<ImageModel>>()
        var multiSelectedImage = ArrayList<ImageModel>()
        var currentImagePos = 0
        lateinit var currentImg: ImageModel

        var isProcessing = false
        var isUpdating = false
        var requireBreak = false
        var purchaseSubscription = SubscriptionType.BASIC_SUB.name

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            changeFragmentListener = activity as ChangeFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement" + "ChangeFragmentListener")
        }
    }

    private fun init() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            // btnMultiDelete.visibility = View.GONE
        }
        getPreferences()
        calculateScreenSize()
        //setupViewModel()
        setupUI()
        setupObserver()
        observeTextChange()
        searchImages()
        getImagesWithNullTxt()
        trackScreen("SearchFragment")
    }

    private fun searchImages() {

        var searchData = editSearchBar.text.trim().toString()
        Timber.d("searchImage: called  sortPref : $searchData")

        if (searchData.isNotEmpty()) {
            searchData = "%$searchData%"
            Timber.d("searchImage: if searched: $searchData")
            tagObserver(searchData)
            viewModel.setSwitchMapObserver(searchData, tagList = filterTagList, sortPreference)
        } else {
            rvTag.visibility = View.GONE
            viewModel.setSwitchMapObserver(searchData, tagList = filterTagList, sortPreference)
        }

    }

    /**
     * starting services to observe changes in file directory
     */
    private fun startServices() {
        val intent = Intent(requireContext(), FileSystemObserverService::class.java)
        intent.putExtra(INTENT_SERVICE_PATH, workingDirectory)
        requireActivity().startService(intent)
        FileSystemObserverService().setServiceListener(this)
    }

    private fun calculateScreenSize() {

        val display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        displayHeight = size.y
        displayWidth = size.x
    }

    private fun getPreferences() {
        sessionManager = SessionManager(requireContext())
        sessionManager.apply {
            getDirPref()?.let {
                workingDirectory = it
            }
            getSortPref().let {
                sortPreference = it
            }

            getSubscriptionPref()?.let {
                purchaseSubscription = it
            }
        }
    }

    private fun setupUI() {

        filterMutable = MutableLiveData(arrayListOf())

        //----------Image recycler setup------------//
        imageAdapter = MainAdapter(requireActivity() as AppCompatActivity, this)
        rvImages.adapter = imageAdapter
        rvImages.layoutManager = GridLayoutManager(requireContext(), 3)

        //----------Tag recycler setup------------//
        tagAdapter = MainTagAdapter(this)
        rvTag.adapter = tagAdapter

        swipeRefreshLayout.isEnabled = false

        searchBarCancel.setOnClickListener { searchBarCancel() }
        imgMainOption.setOnClickListener { openMainOptionMenu() }
        btnMultiTag.setOnClickListener { multiselectTag() }
        btnMultiShare.setOnClickListener { multiselectShare() }
        btnMultiDelete.setOnClickListener { multiselectDelete() }
        btnMultiCancel.setOnClickListener { multiselectCancel() }
        btnhide.setOnClickListener { hideSelectedAlert() }

    }


    /**
     * Observers the Tags
     */
    private fun tagObserver(tag: String) {

        viewModel.getTagFromName(tag).observe(
            viewLifecycleOwner
        ) { t ->
            Timber.d("$t")
            t?.let {
                if (it.isNotEmpty() && editSearchBar.text.isNotEmpty()) {
                    rvTag.visibility = View.VISIBLE
                    Timber.e("tag list ${t.size}")
                    tagAdapter.submitList(t)
                } else {
                    rvTag.visibility = View.GONE
                }
            }
        }

    }

    /**
     * sets of LiveData Active Observes
     */
    private fun setupObserver() {
        viewModel.switchMap.observe(
            viewLifecycleOwner
        ) { t ->
            t?.let { list ->
                if (isProcessing) {
                    if (isUpdating) {
                        Timber.e("setupObserver: mediator observed ${list.size}")
                        viewModel.setCurrentViewedImages(list)
                    }
                } else {
                    viewModel.setCurrentViewedImages(list)
                }
            }
        }

        viewModel.currentViewedImages.observe(
            viewLifecycleOwner
        ) { t ->
            isUpdating = false

            t?.let { modelList ->
                val searchText = editSearchBar.text.trim()
                val newList = modelList.filterNot { it.isHidden }
                //check if newList has contents or search results.
                if(newList.isEmpty()){
                    if(searchText.isEmpty()){
                        txtOops.visibility = View.VISIBLE
                        txtOops.text = requireActivity().resources.getString(
                            R.string.empty_model_list)
                    }
                    else{
                        txtOops.visibility = View.VISIBLE
                        txtOops.text = requireActivity().resources.getString(
                            R.string.no_result,searchText)
                    }
                    imageAdapter.submitList(newList)
                }else {
                    txtOops.visibility = View.GONE
                    imageAdapter.submitList(newList)
                    //rvImages.scrollToPosition(0)
                    //isSearching = true
                }
            }
        }

        filterMutable?.observe(viewLifecycleOwner
        ) { t ->
            t?.let {
                isUpdating = true
                Timber.d("FilterMutable Observer:")
                searchImages()
            }
        }
    }

    private fun observeTextChange() {
        var job : Job? = null

        editSearchBar.addTextChangedListener {
            isUpdating = true

            job?.cancel()

            job = MainScope().launch {

                delay(250L)

                Timber.d(" afterTextChanged  isProcessing: $isProcessing  isSearching: $isUpdating")
                searchBarCancel.visibility = View.VISIBLE
                if (editSearchBar.text.isEmpty()) {
                    searchBarCancel.visibility = View.GONE
                }
                searchImages()
            }

        }
    }


    /**
     * updates bottom progress bar according to process completed
     */
    private fun updateProgress(i: Int, totalSize: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val str: java.lang.StringBuilder = java.lang.StringBuilder()
            str.append("Importing screenshots: ")
            str.append(i)
            str.append(" of ")
            str.append(totalSize)
            tvStatus.text = str.toString()
            progressBar.progress = i * 100 / totalSize
        }

    }

    private fun getImagesWithNullTxt() {
        viewModel.getAllImagesWithNullTxt().observe(viewLifecycleOwner) { imageList ->
            imageList?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data?.let { list ->
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(2000L)
                                textRecognitionSetup(list)
                            }
                        }
                    }
                    Status.ERROR -> {}
                    Status.LOADING -> {}
                }
            }
        }
    }

    private fun textRecognitionSetup(imageList: List<ImageModel>) {
        requireActivity().runOnUiThread {
            processDialog.visibility = View.VISIBLE
            isProcessing = true
        }

        for ((i, image) in imageList.withIndex()) {
            if (!requireBreak) {

                val file = File(image.path)

                val returnTask = textRecognitionProcess(image.path, file)
                returnTask?.let { task ->
                    try {
                        Tasks.await(task)
                        updateProgress(i, imageList.size)
                    } catch (e: ExecutionException) {
                        e.localizedMessage?.let { it1 -> Timber.d(it1) }
                    }

                }


            } else if (requireBreak) {
                isProcessing = false
                requireBreak = false
                break
            }
        }
        requireActivity().runOnUiThread {
            isProcessing = false
            processDialog.visibility = View.GONE
        }
    }

    private fun textRecognitionProcess(
        imagePath: String,
        file: File,
        imageModel: ImageModel? = null
    ): Task<Text>? {
        Timber.d("textRecognitionProcess")
        if (file.exists() && getMimeType(requireActivity(), Uri.fromFile(file)).contains("image")) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
            val resizedBitmap = getResizedBitmap(bitmap, bitmap.width, bitmap.height)

            val fileImage: InputImage
            try {
                fileImage = InputImage.fromBitmap(resizedBitmap, 0)
                //fileImage = InputImage.fromFilePath(requireContext(), uri)
                val textRecognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                return textRecognizer.process(fileImage)
                    .addOnSuccessListener { visionText ->
                        val resultText = visionText.text
                        val stringBuilder: StringBuilder = java.lang.StringBuilder()
                        for (block in visionText.textBlocks) {
                            val blockText = block.text
                            stringBuilder.append(" #$blockText")
                        }

                        var text = stringBuilder.toString()
                        if (text.isBlank() || text.isEmpty()) {
                            text = ""
                        }
                        if (imageModel != null) {
                            insertImageWithTextIntoDatabase(
                                text,
                                imagePath,
                                imageModel,
                                ACTION_INSERT
                            )
                        } else {
                            insertImageWithTextIntoDatabase(text, imagePath, action = ACTION_UPDATE)
                        }
                        //Insert text into database
                    }

            } catch (e: IOException) {
                e.printStackTrace()

                return null
            }
        }
        return null
    }

    private fun insertImageWithTextIntoDatabase(
        text: String,
        path: String,
        imageModel: ImageModel? = null,
        action: String
    ) {
        when (action) {
            ACTION_UPDATE -> viewModel.updateImageText(text = text, path = path)
            ACTION_INSERT -> imageModel?.let { viewModel.insertImage(it) }
        }

    }

    override fun onDetach() {
        super.onDetach()


    }


    //MultiSelection Methods

    private fun searchBarCancel() {
        etSearch.apply {
            text.clear()
            clearFocus()
        }
        ViewUtils.hideKeyboard(requireContext())
    }


    private fun multiselectCancel() {
        changeSelectionMode(SINGLE_SELECTION_MODE)
    }

    private fun multiselectTag() {
        if (checkForZeroItemSelection()) {
            val tagBottomSheetDialog =
                TagBottomSheetDialog(object : TagBottomSheetDialog.TagSheetListener {
                    override fun onTagDoneClicked(tags: ArrayList<String>) {
                        insertTagIntoDatabase(tags)
                    }
                })
            tagBottomSheetDialog.show(
                requireActivity().supportFragmentManager,
                TAG_BOTTOM_DIALOG_TAG
            )
        }
    }

    private fun multiselectShare() {
        if (checkForZeroItemSelection()) {
            val pathList = getSelectedImageModelAttributeList(multiSelectedImage, IMAGE_PATH)?.first
            pathList?.let {
                shareMultipleImages(it)
            }
        }
    }

    private fun multiselectDelete() {
        if (checkForZeroItemSelection()) {
            val deleteDialog = DeleteConfirmDialog(multiSelectedImage.size, object :
                DeleteConfirmDialog.DeleteDialogListener {
                override fun onDeleteClick() {
                    //deleteMultipleImagesFromStorage()
                }
            })
            deleteDialog.show(requireActivity().supportFragmentManager, DELETE_CONFIRM_DIALOG_TAG)

        }
    }

    // fun to hide multi-selected images
    private fun multiSelectHide() {
        val pathList = getSelectedImageModelAttributeList(multiSelectedImage, IMAGE_PATH)?.first
        if (pathList != null) {
            isUpdating = true
            viewModel.setMultipleImagesHidden(pathList, true)
            showToast(requireActivity(), "Images hidden")
            changeSelectionMode(SINGLE_SELECTION_MODE)

        }
    }

    private fun hideSelectedAlert() {
        alertDialog(requireActivity(),
            "Are you sure?",
            "This will hide the selected shots.",
            "Yes",
            "No",
            false,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {

                    -1 -> {
                        multiSelectHide()
                        dialog.dismiss()
                    }

                    -2 -> {
                        dialog.dismiss()
                    }

                }
            })
    }

    private fun deleteMultipleImagesFromStorage() {
        val pathList = getSelectedImageModelAttributeList(multiSelectedImage, IMAGE_PATH)?.first
        var isProgressBottomCreated = false

        pathList?.let {
            val processBottomDialog = ProcessBottomDialog("Deleting images...", it.size,
                object : ProcessBottomDialog.ProgressBottomListener {
                    override fun onBottomViewCreated() {
                        Timber.d("Created:")
                        isProgressBottomCreated = true
                    }
                })
            processBottomDialog.show(requireActivity().supportFragmentManager, PROCESS_DIALOG_TAG)

            var counter = 0
            it.forEachIndexed { index, path ->
                val file = File(path)
                if (file.exists()) {

                    try {
                        if (file.delete()) {
                            counter += 1
                            viewModel.deleteImage(file.absolutePath)
                            isUpdating = true
                        } else
                            Timber.d("File not Deleted")

                    } catch (e: SecurityException) {
                        Timber.e(e)
                    }
                }

            }
            showToast(requireActivity(), "$counter Images deleted")
            changeSelectionMode(SINGLE_SELECTION_MODE)
            processBottomDialog.dismiss()
        }

    }

    private fun sendSupport() {
        val selectorIntent = Intent(Intent.ACTION_SENDTO)
        selectorIntent.data = Uri.parse("mailto:")


        val intent = Intent(Intent.ACTION_SENDTO)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@jaadoo.ai"))
        intent.selector = selectorIntent
        //intent.type = "message/rfc822"


        try {
            requireActivity().startActivity(Intent.createChooser(intent, "Choose Email Client.."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "There are no email clients installed.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun getSelectedImageModelAttributeList(
        imageList: ArrayList<ImageModel>,
        attribute: String
    ): Pair<ArrayList<String>?, ArrayList<File>?>? {

        val selectedImageModelAttributeList = ArrayList<String>()

        when (attribute) {
            IMAGE_PATH -> {
                imageList.forEachIndexed { _, imageModel ->
                    selectedImageModelAttributeList.add(imageModel.path)
                }
                return Pair(selectedImageModelAttributeList, null)
            }

            IMAGE_TEXT -> {
                imageList.forEachIndexed { _, imageModel ->
                    imageModel.text?.let { selectedImageModelAttributeList.add(it) }
                }
                return Pair(selectedImageModelAttributeList, null)
            }

            IMAGE_CREATION_TIME -> {
                imageList.forEachIndexed { _, imageModel ->
                    selectedImageModelAttributeList.add(imageModel.creationTime)
                }
                return Pair(selectedImageModelAttributeList, null)
            }

            IMAGE_PATH_FILE -> {
                val pathFileList = ArrayList<File>()

                imageList.forEachIndexed { _, imageModel ->
                    pathFileList.add(File(imageModel.path))
                }
                return Pair(null, pathFileList)
            }

            else -> {
                return null
            }

        }
    }


    private fun insertTagIntoDatabase(tags: ArrayList<String>) {
        val pathList = getSelectedImageModelAttributeList(multiSelectedImage, IMAGE_PATH)?.first

        pathList?.let {

            val tagModelList = ArrayList<TagModel>()
            val crossRefModelList = ArrayList<ImageTagCrossRef>()

            tags.forEach { tag->
                tagModelList.add(TagModel(tagName = tag))

                pathList.forEach { path->
                    crossRefModelList.add(ImageTagCrossRef(path, tag))
                }
            }

            viewModel.apply {
                insertTags(tagModelList)
                insertImageTagEntries(crossRefModelList)
                setHasTag(true, pathList)
            }

            changeSelectionMode(SINGLE_SELECTION_MODE)
            isUpdating = true
        }
    }


    private fun shareMultipleImages(imgPathList: ArrayList<String>) {

        val uris: ArrayList<Uri> = ArrayList()

        for (path in imgPathList) {
            val file = File(path)
            uris.add(
                FileProvider.getUriForFile(
                    requireContext(),
                    "${requireActivity().packageName}.fileprovider",
                    file
                )
            )
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "*/*"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        startActivity(
            Intent.createChooser(
                intent,
                "Share image using"
            )
        )
    }

    private fun checkForZeroItemSelection(): Boolean = multiSelectedImage.let {
        if (it.isNotEmpty())
            return@let true
        else {
            showToast(requireActivity(), "Please select an image first")
            return@let false
        }
    }


    private fun changeSelectionMode(selectionMode: String) {
        when (selectionMode) {
            SINGLE_SELECTION_MODE -> {
                relMultiSelectBar.visibility = View.GONE
                linearMainBar.visibility = View.VISIBLE
                imageAdapter.deselectAll()
            }

            MULTI_SELECTION_MODE -> {
                relMultiSelectBar.visibility = View.VISIBLE
                linearMainBar.visibility = View.GONE
            }
        }

    }


    private fun openMainOptionMenu() {

        MainOptionDialog(object : MainOptionDialog.MainOptionListener {
            override fun onUpgradeClick() {
                changeFragmentListener.addFragment(BILLING_FRAG_TAG, true)
            }

            override fun onSelectAllClick() {
                imageAdapter.selectAll()
                changeSelectionMode(MULTI_SELECTION_MODE)
            }

            override fun onSortByAscClick() {
                sessionManager.addSortPref(SORT_PREFERENCE_DATE_ASC)
                sortPreference = SORT_PREFERENCE_DATE_ASC
                searchImages()
            }

            override fun onSortByDescClick() {
                sessionManager.addSortPref(SORT_PREFERENCE_DATE_DESC)
                sortPreference = SORT_PREFERENCE_DATE_DESC
                searchImages()
            }

            override fun onChangeDirectoryClick() {

                alertDialog(requireActivity(),
                    "Warning!",
                    "Changing directory will remove all tags from current directory.",
                    "Proceed",
                    "No",
                    cancelable = true,
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            -1 -> showDirectoryTree()
                            -2 -> {
                                showToast(
                                    requireActivity(),
                                    "Request cancelled"
                                )
                                dialog.dismiss()
                            }
                        }
                    })
            }

            override fun onHelpClick() {
                sendSupport()
            }

            override fun onViewHiddenShotsClick() {
                Log.d("hiddenFrag", "frag loaded")
                changeFragmentListener.addFragment(Constants.VIEW_HIDDEN_SHOTS_FRAG_TAG, true)
            }

        })
            .show(requireActivity().supportFragmentManager, MAIN_OPTION_MENU_DIALOG)
    }

    private fun showDirectoryTree() {
        if (!isProcessing) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                startActivityForResult(
                    Intent.createChooser(intent, "Choose directory"),
                    REQUEST_CODE_CHANGE_DIRECTORY
                )


            }
        } else {
            isProcessing = false
            requireBreak = true
            showDirectoryTree()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_CHANGE_DIRECTORY -> {
                if (data != null) {
                    cleanUpOnDirectoryChanged(data)
                } else {
                    showToast(requireActivity(), "Directory not changed")
                }
            }


        }

    }

    private fun cleanUpOnDirectoryChanged(data: Intent) {
        val dataUri = data.data
        dataUri?.let { uri ->
            val docUri = DocumentsContract.buildDocumentUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri)
            )

            docUri?.let {
                val selectedPath = TreeToRealPath().getRealPathFromURI(requireContext(), it)
                if (selectedPath != null) {
                    sessionManager.addDirectoryPref(selectedPath)
                    workingDirectory = selectedPath
                    val intent = Intent(
                        requireContext(),
                        FileSystemObserverService::class.java
                    )
                    intent.putExtra(INTENT_SERVICE_PATH, workingDirectory)
                    requireActivity().startService(intent)
                    requireBreak = false

                    viewModel.apply {
                        deleteAllCrossRef()
                        deleteAllTagTable()
                        deleteWholeImageTable()
                    }

                    changeFragmentListener.replaceFragment(INIT_SETUP_FRAG_TAG, true)

                } else {
                    showToast(requireActivity(), "Directory change failed")
                }
            }
        }
    }

    /**
     * Tag listener Methods
     */
    override fun onTagClick(position: Int, tagName: String) {
        isUpdating = true
        rvTag.visibility = View.GONE
//        editSearchBar.text.clear()
//        editSearchBar.clearFocus()


        val filterChipGroup: ChipGroup = requireActivity().findViewById(R.id.filter_chipGroup)
        val linearFilter: LinearLayout = requireActivity().findViewById(R.id.linear_filter)
        createChip(
            requireActivity() as AppCompatActivity,
            filterChipGroup,
            tagName,
            R.layout.filter_chip_layout,
            onclickEnable = true,
            longClickEnable = false,
            object : ViewUtils.ChipListener {

                override fun chipRemoved(text: String, chip: Chip) {
                    Timber.d(text)
                    filterTagList.remove(text)
                    filterMutable?.value = filterTagList
                    //invisible if no tag
                    if (filterChipGroup.childCount == 0) {
                        linearFilter.visibility = View.GONE
                        etSearch.text.clear()
                        search_cancel.visibility = View.GONE
                    }
                    //setupObserver()
                }

                override fun chipOnClick(chipText: String) {

                }

            },true)
        linearFilter.visibility = View.VISIBLE
        filterTagList.add(tagName)
        filterMutable?.value = filterTagList
        searchBarCancel()
    }


    /**
     * Main Recycler Listener Methods
     */
    override fun onImageClick(position: Int, image: ImageModel) {

        ViewUtils.hideKeyboard(requireContext())

        viewModel.currentViewedImages.value?.get(position)?.let { imgModel ->
            if (imgModel.first200) {
                viewModel.setCurrentImagePosition(position)
                viewModel.setCurrentImagePath(image.path)
                isUpdating = true
                changeFragmentListener.addFragment(IMAGE_DETAIL_FRAG_TAG, addToBackStack = true)
                return
            }
            alertDialog(requireActivity(), "Go Pro & Get Much More",
                "Subscribe Now!",
                "Learn More & Subscribe",
                "Cancel",
                true,
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        -1 -> {
                            changeFragmentListener.addFragment(BILLING_FRAG_TAG, true)
                            dialog.dismiss()
                        }
                        -2 -> {
                            dialog.dismiss()
                        }
                    }
                })

        }

    }


    override fun onLongImageClick(position: Int) {
        changeSelectionMode(MULTI_SELECTION_MODE)
    }

    override fun fileDelete(filePath: String) {

    }

    override fun fileCreate(filePath: String) {
        Timber.d("fileCreate $filePath")
        val file = File(filePath)
        val mimeTypeMap = getMimeType(requireActivity(), Uri.parse(file.absolutePath))

        if (mimeTypeMap.contains("image")) {
            val formattedDate = getCreationDate(file)
            val imageInfo =
                ImageModel(
                    path = file.absolutePath,
                    text = null,
                    creationTime = formattedDate,
                    hasTag = false,
                    first200 = false,
                    isHidden = false
                )
            textRecognitionProcess(filePath, file, imageInfo)
        }

    }


    private fun deleteFile(file: File) {
        val pathone = MediaStore.MediaColumns.DATA + "=?"
        val selectedArgs = arrayOf<String>(
            file.absolutePath
        )
        val contentResolver = requireContext().contentResolver
        val fileUri = MediaStore.Files.getContentUri("external")

        contentResolver.delete(fileUri, pathone, selectedArgs)

        if (file.exists()) {
            contentResolver.delete(fileUri, pathone, selectedArgs)
            viewModel.deleteImage(file.absolutePath)
        }

    }


    /*private fun requestDeletePermission(uriList: List<Uri>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi: PendingIntent =
                MediaStore.createDeleteRequest(requireActivity().contentResolver, uriList)
            try {
                startIntentSenderForResult(
                    pi.intentSender, REQUEST_PERM_DELETE, null, 0, 0,
                    0
                )
            } catch (e: IntentSender.SendIntentException) {
            }
        }
    }*/


}