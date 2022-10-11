package com.screenshot.rockstar.repository


import com.screenshot.rockstar.database.AppDatabase
import com.screenshot.rockstar.models.ImageModel
import com.screenshot.rockstar.models.ImageTagCrossRef
import com.screenshot.rockstar.models.TagModel
import javax.inject.Inject

class MainRepository @Inject constructor(
    apiHelper: AppDatabase
){

    private val dao = apiHelper.userDao()

    suspend fun insertImage(image: ImageModel) = dao.insertImage(image)

    suspend fun deleteSelectedTag(path: String,tagText: String) = dao.deleteTag(path , tagText  )

    suspend fun isTagUsed(tagText: String) = dao.isTagUsed(tagText)
    suspend fun doesImageHasTag(path: String) = dao.doesImageHasTag(path)

    suspend fun setImageHidden(image:ImageModel) = dao.setImageHidden(true,image.path)
    suspend fun setMultipleImagesHidden(imagePathList:List<String>,isHidden:Boolean) = dao.setMultipleImagesHidden(imagePathList,isHidden)
    suspend fun unDoHideImages(imagePathList: List<String>,isHidden: Boolean) = dao.unDoHideImages(imagePathList,isHidden)
    suspend fun insertMultipleImages(imageList: List<ImageModel>) = dao.insertMultipleImages(imageList)
    suspend fun deleteImage(imagePath: String) = dao.deleteImage(imagePath)
    suspend fun deleteWholeImageTable() = dao.deleteWholeImageTable()
    suspend fun deleteMultiImage(pathList:List<String>) = dao.deleteMultiImage(pathList)

    fun getAllImagesByDateAsc() = dao.getAllImagesByDateAsc()
    fun getAllImagesByDateDesc() = dao.getAllImagesByDateDesc()
    fun getAllImages() = dao.getAllImages()
    fun getImagesFromTag(tag:String) = dao.getImagesFromTag(tag)

    fun getHiddenImages() = dao.getHiddenImages()

    fun getImagesFromMultiTag(tags:List<String>) = dao.getImagesFromMultiTag(tags)

    suspend fun getAllImagesWithNullTxt() = dao.getAllImagesWithNullTxt()
    suspend fun updateImageText(text:String, path: String) = dao.updateImageText(text, path)



    fun getImageByPathList(pathList:List<String>) = dao.getImageByPathList(pathList)
    fun getTagFromName(tagName:String) = dao.getTagFromName(tagName)

    fun getImagesByText(searchedTxt:String) = dao.getImagesByText(searchedTxt)
    fun getImagesByTextAsc(searchedTxt:String) = dao.getImagesByTextAsc(searchedTxt)
    fun getImagesByTextDesc(searchedTxt:String) = dao.getImagesByTextDesc(searchedTxt)
    fun getImagesByPath(path:String) = dao.getImagesByPath(path)

    //Tag model queries
    fun getTagsOfImage(path:String) = dao.getTagsOfImage(path)
    suspend fun getTagsFromPaths(path: List<String>) = dao.getTagsFromPaths(path)
    fun getTagIdsFromList(tagList:List<String>) = dao.getTagIdsFromList(tagList)
    fun getAllTags() = dao.getAllTags()
    suspend fun deleteAllTagTable() = dao.deleteAllTagTable()
    suspend fun insertTags(tagList:List<TagModel>) = dao.insertTags(tagList)
    suspend fun deleteTagFromTable(tag: String) = dao.deleteTagFromTable(tag)
    suspend fun setHasTag(hasTag:Boolean, path: List<String>) = dao.setHasTag(hasTag, path)

    //Crossref Queries
    suspend fun insertImageTagEntries(list:List<ImageTagCrossRef>) = dao.insertImageTagEntries(list)
    suspend fun deleteAllCrossRef() = dao.deleteAllCrossRef()

    //subscription queries
    suspend fun setFirst200() = dao.setFirst200()
    suspend fun setLast200() = dao.setLast200()
    suspend fun unlockAllImageOnPurchase() = dao.unlockAllImageOnPurchase()

}