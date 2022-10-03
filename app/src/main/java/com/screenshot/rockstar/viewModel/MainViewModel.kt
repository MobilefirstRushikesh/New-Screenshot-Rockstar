package com.screenshot.rockstar.viewModel

import androidx.lifecycle.*
import com.screenshot.rockstar.models.*
import com.screenshot.rockstar.repository.MainRepository
import com.screenshot.rockstar.utils.Constants.Companion.SORT_PREFERENCE_DATE_ASC
import com.screenshot.rockstar.utils.Constants.Companion.SORT_PREFERENCE_DATE_DESC
import com.screenshot.rockstar.utils.CustomFunctions.sortListPref
import com.screenshot.rockstar.utils.FilterEnum
import com.screenshot.rockstar.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {


    fun insertImage(image: ImageModel) = viewModelScope.launch {
        mainRepository.insertImage(image)
    }

    fun setImageHidden(image: ImageModel) = viewModelScope.launch {
        mainRepository.setImageHidden(image)
    }

    fun deleteSelectedTag(path: String, tagText: String) = viewModelScope.launch {
        mainRepository.deleteSelectedTag(path, tagText)

        //check if crossRef has tag in similar path
        val isTagUsed = mainRepository.isTagUsed(tagText)

        if (isTagUsed.not()) {
            mainRepository.deleteTagFromTable(tagText)
        }

        // check if last tag used
        val doesImageHasTag = mainRepository.doesImageHasTag(path)

        if (doesImageHasTag.not()) {
            val paths = mutableListOf<String>().apply {
                add(path)
            }
            mainRepository.setHasTag(false, path = paths)
        }
    }

    fun unDoHideImages(imagePathList: List<String>, isHidden: Boolean) = viewModelScope.launch {
        mainRepository.unDoHideImages(imagePathList, isHidden)
    }

    fun setMultipleImagesHidden(imagePathList: List<String>, isHidden: Boolean) =
        viewModelScope.launch {
            mainRepository.setMultipleImagesHidden(imagePathList, isHidden)
        }

    fun insertMultipleImage(imageList: List<ImageModel>) = viewModelScope.launch {
        mainRepository.insertMultipleImages(imageList)
    }

    fun deleteImage(imagePath: String) = viewModelScope.launch {
        mainRepository.deleteImage(imagePath)
    }

    fun deleteMultipleImage(pathList: List<String>) = viewModelScope.launch {
        mainRepository.deleteMultiImage(pathList)
    }

    fun deleteWholeImageTable() = viewModelScope.launch {
        mainRepository.deleteWholeImageTable()
    }

    fun updateImageText(text: String, path: String) = viewModelScope.launch {
        mainRepository.updateImageText(text, path)
    }

    fun insertTags(tags : List<TagModel>) = viewModelScope.launch {
        mainRepository.insertTags(tags)

    }

    fun insertImageTagEntries(list: List<ImageTagCrossRef>) = viewModelScope.launch {
        mainRepository.insertImageTagEntries(list)
    }

    fun setHasTag(hasTag: Boolean, path: List<String>) = viewModelScope.launch {
        mainRepository.setHasTag(hasTag, path)
    }

    fun deleteAllCrossRef() = viewModelScope.launch {
        mainRepository.deleteAllCrossRef()
    }

    fun deleteAllTagTable() = viewModelScope.launch {
        mainRepository.deleteAllTagTable()
    }


    //get Image with null text

    fun getAllImagesWithNullTxt() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getAllImagesWithNullTxt()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    //Subscription methods

    fun setFirst200() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.setFirst200()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun setLast200() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.setLast200()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun unlockAllImageOnPurchase() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.unlockAllImageOnPurchase()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    //get Image list No search text
    fun getAllImage() = mainRepository.getAllImages()
    private fun getAllImageByDateAsc() = mainRepository.getAllImagesByDateAsc()
    private fun getAllImageByDateDesc() = mainRepository.getAllImagesByDateDesc()
    private fun getImagesFromSingleTag(tag: String) = mainRepository.getImagesFromTag(tag)
    private fun getImagesFromMultiTag(tagList: List<String>) =
        mainRepository.getImagesFromMultiTag(tagList)

    fun getHiddenImages() = mainRepository.getHiddenImages()

    //get Image list with search text
    fun getImagesByText(searchedText: String) = mainRepository.getImagesByText(searchedText)
    private fun getImagesByTextDateAsc(searchedText: String) =
        mainRepository.getImagesByTextAsc(searchedText)

    private fun getImagesByTextDateDesc(searchedText: String) =
        mainRepository.getImagesByTextDesc(searchedText)

    fun getImagesByPath(path: String) = mainRepository.getImagesByPath(path)


    //Tag methods
    fun getTagsOfSingleImage(imagePath: String) = mainRepository.getTagsOfImage(imagePath)
    fun getTagFromName(tagName: String) = mainRepository.getTagFromName(tagName)
    fun getAllTags() = mainRepository.getAllTags()


    fun getImageDetailList() = liveData<Resource<List<ImageDetail>>> {

        emit(Resource.loading(data = null))

        try {

            val pathList = _currentViewedImages.value?.map { it.path }

            if (!pathList.isNullOrEmpty()) {

                mainRepository.getTagsFromPaths(pathList).collectLatest { list ->

                    val imageList = mutableListOf<ImageDetail>()

                    list.forEach { imageWithTags ->

                        if (imageWithTags.image.isHidden)
                            return@forEach

                        val imageDetail = imageWithTags.image.toImageDetail(
                            tags = imageWithTags.tags
                        )

                        imageList.add(imageDetail)
                    }

                    emit(Resource.success(imageList))
                }
            } else {
                emit(Resource.error(message = "No Images Found", data = null))
            }
        } catch (e: Exception) {
            emit(Resource.error(message = "Wait a moment!", data = null))
            e.printStackTrace()
        }
    }


    var currentImagePosition = MutableStateFlow(-1)
        private set

    var currentImagePath = MutableStateFlow("")
        private set

    fun setCurrentPosition(pos: Int) {
        currentImagePosition.value = pos
    }

    fun setCurrentImagePath(path: String) {
        currentImagePath.value = path
    }

    private val _currentViewedImages = MutableLiveData<List<ImageModel>>()
    val currentViewedImages: LiveData<List<ImageModel>> = _currentViewedImages


    fun setCurrentViewedImages(imageList: List<ImageModel>) {
        _currentViewedImages.value = imageList
    }

    fun setCurrentImagePosition(position: Int) {
        currentImagePosition.value = position
    }

    var mediator = MediatorLiveData<List<ImageModel>>()


    var changeObserver = MutableLiveData<Triple<String, String, List<String>>>()

    private fun checkSearchText(searchedText: String): String {
        return if (searchedText.isEmpty()) {
            "NO_TEXT"
        } else {
            "TEXT"
        }
    }

    private fun checkTagList(tagList: List<String>): String {
        return when {
            tagList.isEmpty() -> {
                "NO_FILTER"
            }
            tagList.size == 1 -> {
                "SINGLE_FILTER"
            }
            else -> {
                "MULTI_FILTER"
            }
        }
    }

    private fun checkSortPref(sortPreference: String): String {
        return if (sortPreference == SORT_PREFERENCE_DATE_ASC)
            "ASC"
        else
            "DESC"
    }

    private fun switchObserver(
        searchedText: String,
        tagList: List<String>,
        sortPreference: String
    ): LiveData<List<ImageModel>> {

        val text = checkSearchText(searchedText)
        val filter = checkTagList(tagList)
        val order = checkSortPref(sortPreference)

        val finalString = "${filter}_${text}_${order}"


        return when (FilterEnum.valueOf(finalString)) {
            FilterEnum.NO_FILTER_NO_TEXT_ASC -> {
                getAllImageByDateAsc()
            }
            FilterEnum.NO_FILTER_NO_TEXT_DESC -> {
                getAllImageByDateDesc()
            }
            FilterEnum.NO_FILTER_TEXT_ASC -> {
                getImagesByTextDateAsc(searchedText)
            }
            FilterEnum.NO_FILTER_TEXT_DESC -> {
                getImagesByTextDateDesc(searchedText)
            }

            FilterEnum.SINGLE_FILTER_NO_TEXT_DESC -> {
                return liveData {
                    getImagesFromSingleTag(tagList[0]).collect { list ->
                        emit(sortListPref(list.firstOrNull()?.images ?: emptyList(), SORT_PREFERENCE_DATE_DESC))
                    }
                }
                /*val liveData = MutableLiveData<List<ImageModel>>()
                getImagesFromSingleTag(tagList[0]).observeForever {
                    it?.let {list->
                        Log.d("switchMap","SINGLE_FILTER_NO_TEXT_DESC   size: ${list.size}")
                        liveData.value = sortListPref(list.first().images, SORT_PREFERENCE_DATE_DESC)
                    }

                }
                return liveData*/
            }
            FilterEnum.SINGLE_FILTER_NO_TEXT_ASC -> {
                return liveData {
                    getImagesFromSingleTag(tagList[0]).collect { list ->
                        emit(sortListPref(list.firstOrNull()?.images ?: emptyList(), SORT_PREFERENCE_DATE_ASC))
                    }
                }
            }
            FilterEnum.SINGLE_FILTER_TEXT_ASC -> {

                return liveData {
                    getImagesFromSingleTag(tagList[0]).collect { list ->
                        val search = searchedText.drop(1).dropLast(1)
                        val singleFilterList = ArrayList<ImageModel>()
                        list.forEach { tagWithImage ->

                            val images = tagWithImage.images
                            images.forEach { imageModel ->

                                imageModel.text?.let { imgTxt ->
                                    if (imgTxt.lowercase(Locale.getDefault()).contains(
                                            search.lowercase(
                                                Locale.getDefault()
                                            )
                                        )
                                    ) {
                                        singleFilterList.add(imageModel)
                                    }
                                }
                            }
                            emit(sortListPref(singleFilterList, SORT_PREFERENCE_DATE_ASC))
                        }
                    }
                }
            }
            FilterEnum.SINGLE_FILTER_TEXT_DESC -> {
                return liveData {
                    getImagesFromSingleTag(tagList[0]).collect { list ->
                        val search = searchedText.drop(1).dropLast(1)
                        val singleFilterList = ArrayList<ImageModel>()
                        list.forEach { tagWithImage ->

                            val images = tagWithImage.images
                            images.forEach { imageModel ->

                                imageModel.text?.let { imgTxt ->
                                    if (imgTxt.lowercase(Locale.getDefault()).contains(
                                            search.lowercase(
                                                Locale.getDefault()
                                            )
                                        )
                                    ) {
                                        singleFilterList.add(imageModel)
                                    }
                                }
                            }
                            emit(sortListPref(singleFilterList, SORT_PREFERENCE_DATE_DESC))
                        }
                    }
                }
            }

            FilterEnum.MULTI_FILTER_NO_TEXT_DESC -> {

                return liveData {
                    getImagesFromMultiTag(tagList).collect { listTag ->
                        val sum = arrayListOf<ImageModel>()
                        for (list in listTag as ArrayList<TagWithImages>) {
                            sum += list.images
                        }
                        val finalList = ArrayList<ImageModel>()

                        sum.groupBy { it.path }
                            .filter { it.value.size == tagList.size }
                            .flatMap { it.value }
                            .forEachIndexed { index, imageTable ->
                                if (!finalList.contains(imageTable)) {
                                    finalList.add(imageTable)
                                }
                            }
                        emit(sortListPref(finalList, SORT_PREFERENCE_DATE_DESC))
                    }
                }


            }
            FilterEnum.MULTI_FILTER_NO_TEXT_ASC -> {
                return liveData {
                    getImagesFromMultiTag(tagList).collect { ListTagWithImage ->
                        val sum = ArrayList<ImageModel>()
                        for (list in ListTagWithImage as ArrayList<TagWithImages>) {
                            sum += list.images
                        }
                        val finalList = ArrayList<ImageModel>()

                        sum.groupBy { it.path }
                            .filter { it.value.size == tagList.size }
                            .flatMap { it.value }
                            .forEachIndexed { index, imageTable ->
                                if (!finalList.contains(imageTable)) {
                                    finalList.add(imageTable)
                                }
                            }
                        emit(sortListPref(finalList, SORT_PREFERENCE_DATE_ASC))
                    }
                }
            }
            FilterEnum.MULTI_FILTER_TEXT_ASC -> {
                return liveData {
                    getImagesFromMultiTag(tagList).collect { ListTagWithImage ->
                        val sum = ArrayList<ImageModel>()
                        for (list in ListTagWithImage as ArrayList<TagWithImages>) {
                            sum += list.images
                        }
                        val finalList = ArrayList<ImageModel>()

                        sum.groupBy { it.path }
                            .filter { it.value.size == tagList.size }
                            .flatMap { it.value }
                            .forEachIndexed { index, imageTable ->
                                if (!finalList.contains(imageTable)) {
                                    finalList.add(imageTable)
                                }
                            }

                        val search = searchedText.drop(1).dropLast(1)
                        val multiFilterList = ArrayList<ImageModel>()
                        finalList.forEach { image ->
                            image.text?.let { imgTxt ->
                                if (imgTxt.lowercase(Locale.getDefault()).contains(
                                        search.lowercase(
                                            Locale.getDefault()
                                        )
                                    )
                                ) {
                                    multiFilterList.add(image)
                                }
                            }
                        }
                        emit(sortListPref(multiFilterList, SORT_PREFERENCE_DATE_ASC))
                    }
                }

            }
            FilterEnum.MULTI_FILTER_TEXT_DESC -> {
                return liveData {
                    getImagesFromMultiTag(tagList).collect { ListTagWithImage ->
                        val sum = ArrayList<ImageModel>()
                        for (list in ListTagWithImage as ArrayList<TagWithImages>) {
                            sum += list.images
                        }
                        val finalList = ArrayList<ImageModel>()

                        sum.groupBy { it.path }
                            .filter { it.value.size == tagList.size }
                            .flatMap { it.value }
                            .forEachIndexed { index, imageTable ->
                                if (!finalList.contains(imageTable)) {
                                    finalList.add(imageTable)
                                }
                            }

                        val search = searchedText.drop(1).dropLast(1)
                        val multiFilterList = ArrayList<ImageModel>()
                        finalList.forEach { image ->
                            image.text?.let { imgTxt ->
                                if (imgTxt.lowercase(Locale.getDefault()).contains(
                                        search.lowercase(
                                            Locale.getDefault()
                                        )
                                    )
                                ) {
                                    multiFilterList.add(image)
                                }
                            }
                        }
                        emit(sortListPref(multiFilterList, SORT_PREFERENCE_DATE_DESC))

                    }
                }
            }
        }
    }


    var switchMap: LiveData<List<ImageModel>> =
        Transformations.switchMap(changeObserver) { triple ->
            switchObserver(
                searchedText = triple.first,
                sortPreference = triple.second,
                tagList = triple.third
            )
        }

    fun setSwitchMapObserver(searchedText: String, tagList: List<String>, sortPreference: String) {
        changeObserver.value = Triple(searchedText, sortPreference, tagList)
    }


}