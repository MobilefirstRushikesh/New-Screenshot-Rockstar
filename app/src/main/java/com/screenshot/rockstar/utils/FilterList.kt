package  com.screenshot.rockstar.utils

import androidx.appcompat.app.AppCompatActivity
import com.screenshot.rockstar.viewModel.MainViewModel


const val NO_FILTER = "noFilter"
const val MULTI_FILTER = "mFilter"
const val SINGLE_FILTER = "sFilter"
//private lateinit var filterListener: FilterList.GetFilteredList

class FilterList(private val activity: AppCompatActivity,
                 private val viewModel: MainViewModel
) {
 /*   private var sessionManager = SessionManager(activity)


    fun filter(searchedText:String,
               filterTagList:ArrayList<String>,
               currentList:ArrayList<ImageModel>,){



            if (filterTagList.isNotEmpty()){

                if (filterTagList.size > 1 ){
                    searchBarCondition(MULTI_FILTER, searchedText, filterTagList, currentList)
                }else{
                    //Log.d(FILTER_TAG, "filter not empty single filter")
                    searchBarCondition(SINGLE_FILTER, searchedText, filterTagList, currentList)
                }
            }
            else{
                searchBarCondition(NO_FILTER, searchedText, filterTagList, currentList)
            }

    }


    private fun searchBarCondition(filterChoice:String,
                                   searchedText:String,
                                   filterTagList:ArrayList<String>,
                                   currentList:ArrayList<ImageModel>, ){
        if(searchedText.isNotEmpty()){
                val searchData = "%$searchedText%"
                when(filterChoice){

                    NO_FILTER->{
                        Log.d(Constants.FILTER_TAG, "search bar if no filter")
                        viewModel.getImagesByText(searchData).observe(activity,{
                            it?.let {
                                    resource ->
                                when(resource.status){
                                    Status.SUCCESS ->{
                                        resource.data?.let { imageList ->
                                            sortListPerPref(imageList)
                                        }
                                    }
                                    Status.ERROR -> {

                                    }
                                    Status.LOADING -> {
                                    }

                                }
                            }
                        })
                    }

                    SINGLE_FILTER->{
                        Log.d(Constants.FILTER_TAG, "search bar if single filter ${currentList.size}")
                        val singleFilterList = ArrayList<ImageModel>()
                        for(image in currentList)
                        {
                            val search = image.text.lowercase(Locale.getDefault())
                            if (search.contains(searchedText)) {
                                singleFilterList.add(image)

                            }
                        }
                        sortListPerPref(singleFilterList)
                    }

                    MULTI_FILTER->{
                        Log.d(Constants.FILTER_TAG, "search bar if multi filter ${currentList.size}")
                        val multiFilterList = ArrayList<ImageModel>()
                        for(image in currentList)
                        {
                            val search = image.text.toLowerCase()
                            if (search.contains(searchedText)) {
                                multiFilterList.add(image)

                            }
                        }
                       sortListPerPref(multiFilterList)
                    }
                }


            }
        else{
                Log.d(Constants.FILTER_TAG, "filter prefrence: $filterChoice")
                when(filterChoice){
                    NO_FILTER->{
                        Log.d(Constants.FILTER_TAG, "search bar else no filter")
                        viewModel.getAllImages().observe(activity,{
                            it?.let {
                                    resource ->
                                when(resource.status){
                                    Status.SUCCESS ->{
                                        resource.data?.let { imageList ->
                                           sortListPerPref(imageList)
                                        }
                                    }
                                    Status.ERROR -> {

                                    }
                                    Status.LOADING -> {
                                    }

                                }
                            }
                        })
                    }

                    SINGLE_FILTER -> {
                        Log.d(Constants.FILTER_TAG, "search bar else single filter ${filterTagList[0]}")
                        viewModel.getImagesFromTag(filterTagList[0]).observe(activity, {
                            it?.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        resource.data?.let { imageList ->
                                            val imageModel = ArrayList<ImageModel>()
                                            //returnedList.clear()
                                            Log.d("imageListSizeCheck", "$imageList")
                                            imageModel.addAll(imageList[0].images)
                                            sortListPerPref(imageModel)

                                        }
                                    }
                                    Status.ERROR -> {

                                    }
                                    Status.LOADING -> {
                                    }

                                }
                            }
                        })
                    }
                    MULTI_FILTER->{
                        Log.d(Constants.FILTER_TAG, "search bar else multi filter ${filterTagList}")
                        viewModel.getImagesFromMultiTag(filterTagList).observe(activity, {
                            it?.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        resource.data?.let { imageList ->


                                            for (image in imageList){

                                            }


                                        }
                                    }
                                    Status.ERROR -> {

                                    }
                                    Status.LOADING -> {
                                    }

                                }
                            }
                        })

                    }
                }
            }



    }


    private fun filterThroughImageCrossTable(list: List<ImageTagCrossRef>){

        val imagePathList = ArrayList<String>()
        Log.d(Constants.FILTER_TAG, "filterThorugh: ${list}")
        for (image in list){
            imagePathList.add(image.path)
        }
        viewModel.getImageByPathList(imagePathList).observe(activity,{
            it?.let {
                    resource ->
                when(resource.status){
                    Status.SUCCESS ->{
                        resource.data?.let { imageList ->

                            Log.d(Constants.FILTER_TAG, "multiple Images:  $imageList")
                            sortListPerPref(imageList)
                        }
                    }
                    Status.ERROR -> {
                        Log.d(Constants.FILTER_TAG, "multiple Images:  error")
                    }
                    Status.LOADING -> {
                        Log.d(Constants.FILTER_TAG, "multiple Images:  loading")
                    }

                }
            }
        })
    }

    private fun sortListPerPref(imageList:List<ImageModel>){

        when(sessionManager.getSortPref()){

            SORT_PREFERENCE_DATE_DESC ->{
               imageList.sortedByDescending { image ->
                    image.creationTime
                }.also {
                    filterListener.filteredList(it as ArrayList<ImageModel>)
               }
            }
            SORT_PREFERENCE_DATE_ASC ->{
                imageList.sortedBy{ image ->
                    image.creationTime
                }.also {
                    filterListener.filteredList(it as ArrayList<ImageModel>)
                }

            }
        }

    }

  

    fun setFilterListener(listener:GetFilteredList){
        filterListener = listener
    }

    interface GetFilteredList {
        fun filteredList(filterList: ArrayList<ImageModel>)
    }

  */
}