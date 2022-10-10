package com.screenshot.rockstar.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.screenshot.rockstar.models.*
import kotlinx.coroutines.flow.Flow


@Dao
interface RoomApiService {

    /**
     * ImageModel query
     */

    @Query("SELECT * FROM ImageModel" )
    fun getAllImages(): LiveData<List<ImageModel>>

    @Query("SELECT * FROM ImageModel WHERE isHidden IS 1" )
    fun getHiddenImages(): LiveData<List<ImageModel>>

    @Query("SELECT * FROM ImageModel WHERE text IS NULL ")
    suspend fun getAllImagesWithNullTxt(): List<ImageModel>

    @Query("SELECT * FROM ImageModel ORDER BY creationTime ASC ")
    fun getAllImagesByDateAsc(): LiveData<List<ImageModel>>

    @Query("SELECT * FROM ImageModel ORDER BY creationTime DESC")
    fun getAllImagesByDateDesc(): LiveData<List<ImageModel>>

    @Query("SELECT * FROM ImageModel WHERE text Like :searchedTxt ")
    fun getImagesByText(searchedTxt: String): LiveData<List<ImageModel>>

    @Query("SELECT * FROM ImageModel WHERE text Like :searchedTxt ORDER BY creationTime ASC ")
    fun getImagesByTextAsc(searchedTxt: String): LiveData<List<ImageModel>>

    @Query("SELECT * FROM ImageModel WHERE text Like :searchedTxt ORDER BY creationTime DESC ")
    fun getImagesByTextDesc(searchedTxt: String): LiveData<List<ImageModel>>

    @Query("SELECT * FROM ImageModel WHERE path Like :path")
    fun getImagesByPath(path: String): LiveData<ImageModel>

    @Query("SELECT * FROM ImageModel WHERE path IN (:pathList)")
    fun getImageByPathList(pathList: List<String>): LiveData<List<ImageModel>>

//    @Query("SELECT * FROM ImageModel WHERE text Like :searchedTxt IN (:list)")
//    fun getFilteredImagesBySearchedFromList(searchedTxt: String, list: List<ImageModel>): LiveData<ImageModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageModel)

    @Query("UPDATE ImageModel SET hasTag = :hasTag WHERE path IN (:path)")
    suspend fun setHasTag(hasTag: Boolean, path: List<String>)

    @Query("UPDATE ImageModel SET isHidden = :isHidden WHERE path IN (:path)")
    suspend fun setImageHidden(isHidden:Boolean,path: String)

    @Query("UPDATE ImageModel SET isHidden = :isHidden WHERE path IN(:imagePathList)" )
    suspend fun setMultipleImagesHidden(imagePathList: List<String>,isHidden: Boolean)

    @Query("UPDATE ImageModel SET isHidden = :isHidden WHERE path IN (:imagePathList)")
    suspend fun unDoHideImages(imagePathList: List<String>,isHidden: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleImages(imageList:List<ImageModel>)

    @Query("DELETE FROM ImageModel WHERE path Like :imagePath")
    suspend fun deleteImage(imagePath: String)

    @Query("DELETE FROM ImageModel WHERE path IN (:pathList)")
    suspend fun deleteMultiImage(pathList: List<String>)

    @Query("DELETE FROM ImageTagCrossRef WHERE path IN (:path) AND tagName IN (:tagText) ")
    suspend fun deleteTag(path:String,tagText:String)

    @Query("DELETE FROM ImageModel")
    suspend fun deleteWholeImageTable()

    @Query("UPDATE ImageModel SET text = (:text) WHERE path LIKE (:path) AND NULLIF(text, '') IS NULL")
    suspend fun updateImageText(text:String, path: String)

    @Query("UPDATE ImageModel SET first200 = 1 WHERE creationTime IN (SELECT creationTime FROM (SELECT creationTime FROM ImageModel ORDER BY creationTime DESC LIMIT 200) WHERE first200 = 0)")
    suspend fun setFirst200()

    @Query("UPDATE ImageModel SET first200 = 1 WHERE first200 = 0")
    suspend fun unlockAllImageOnPurchase()

    @Query("UPDATE ImageModel SET first200 = 0 WHERE creationTime IN (SELECT creationTime FROM (SELECT creationTime FROM ImageModel ORDER BY creationTime DESC LIMIT 201, (SELECT COUNT(creationTime) FROM ImageModel)) WHERE first200 = 1)")
    suspend fun setLast200()


    /**
     * TagModel Query
     */

    //Get TagModel from all Name
    @Query("SELECT * FROM TagModel WHERE tagName LIKE :tagName")
    fun getTagFromName(tagName: String): LiveData<List<TagModel>>

    @Query("SELECT * FROM TagModel WHERE tagName IN (:tagList)")
    fun getTagIdsFromList(tagList: List<String>):LiveData<List<TagModel>>

    //returns all Images associated with passed single tag
    @Transaction
    @Query("SELECT * FROM TagModel WHERE tagName Like :tag")
    fun getImagesFromTag(tag: String): Flow<List<TagWithImages>>

    @Query("SELECT * FROM TagModel WHERE tagName IN (:tags)")
    fun getImagesFromMultiTag(tags: List<String>): Flow<List<TagWithImages>>

    //returns all tags associated with passed single Image
    @Transaction
    @Query("SELECT * FROM ImageModel WHERE path Like:path ")
    fun getTagsOfImage(path: String): LiveData<List<ImageWithTags>>

    @Transaction
    @Query("SELECT * FROM ImageModel WHERE path IN (:path) ORDER BY creationTime DESC")
    suspend fun getTagsFromPaths(path: List<String>): List<ImageWithTags>

    @Query("SELECT * FROM TagModel")
    fun getAllTags(): LiveData<List<TagModel>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagModel>)

    @Query("DELETE FROM TagModel WHERE tagName Like :tag")
    suspend fun deleteTagFromTable(tag: String)

    @Query("DELETE FROM ImageTagCrossRef")
    suspend fun deleteAllTagTable()


    /**
     * CrossRef table Query
     */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImageTagEntries(list: List<ImageTagCrossRef>)

    @Query("SELECT EXISTS (SELECT 1 FROM ImageTagCrossRef WHERE tagName = :tag)")
    suspend fun isTagUsed(tag:String) : Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM ImageTagCrossRef WHERE path = :path)")
    suspend fun doesImageHasTag(path:String) : Boolean

    @Query("DELETE FROM ImageTagCrossRef")
    suspend fun deleteAllCrossRef()
}