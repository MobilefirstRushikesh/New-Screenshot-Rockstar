package com.screenshot.rockstar.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.screenshot.rockstar.models.ImageModel
import com.vmadalin.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

object CustomFunctions {

    fun sortListPref(imageList:List<ImageModel>, sortPref:String): List<ImageModel> {

        when(sortPref){

            Constants.SORT_PREFERENCE_DATE_DESC ->{
                return imageList.sortedByDescending { image ->
                    image.creationTime
                }
            }
            Constants.SORT_PREFERENCE_DATE_ASC ->{
                return imageList.sortedBy{ image ->
                    image.creationTime
                }
            }

            else->{
                return imageList.sortedByDescending { image ->
                    image.creationTime
                }
            }
        }
    }

    interface SortPreferenceListener{
        fun sortedList(list: List<ImageModel>)
    }

    /*fun hasPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ){
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            )
        }else{
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
       }
    }*/

    fun hasPermissions(context: Context): Boolean =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )


    fun getResizedBitmap(image: Bitmap, bitmapWidth: Int, bitmapHeight: Int): Bitmap {
        when {
            bitmapWidth > 720 && bitmapHeight > 1280 -> {
                return Bitmap.createScaledBitmap(
                    image, 720, 1280,
                    true
                )
            }
            bitmapWidth > 720 -> {
                return Bitmap.createScaledBitmap(
                    image, 720, bitmapHeight,
                    true
                )
            }
            bitmapHeight > 1280 -> {
                return Bitmap.createScaledBitmap(
                    image, bitmapWidth, 1280,
                    true
                )
            }
            else -> return image
        }

    }

    /**
     * [getMimeType] returns extension of file
     */
    fun getMimeType(activity: Activity, uri: Uri): String {

        var extension:String

        //Check uri format to avoid null
        extension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(activity.contentResolver.getType(uri))!!
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path!!)).toString())!!
        }

        if(extension.contains("jpeg") || extension.contains("jpg") || extension.contains("png") ){
            extension = "image/$extension"
        }

        return  extension
    }

    fun getCreationDate(file: File?): String {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attr = Files.readAttributes(file?.toPath(), BasicFileAttributes::class.java)
            val dateInMillis = attr.creationTime().toMillis()
            dateInMillis.toString()

        } else {
            val lastModifiedDate = file?.lastModified()
            lastModifiedDate.toString()
        }
    }



    fun checkNetworkConnection(context: Context):Boolean{
        var isConnected = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            isConnected = when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                activeNetworkInfo?.run {
                    isConnected = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }
        return isConnected
    }

    fun handleBillingError(activity: Activity, error: PurchasesError){
        Log.e("RevenueCat", "handleBillingError:")
        with(error) {
            // log error details
            Log.e("RevenueCat","Error: $code")
            Log.e("RevenueCat","Message: $message")
            Log.e("RevenueCat","Underlying Error: $underlyingErrorMessage")

            when (code) {
                PurchasesErrorCode.PurchaseNotAllowedError -> {
                    showAlert(activity, "Purchase Error" ,"Subscription not allowed on this device.")
                }
                PurchasesErrorCode.PurchaseInvalidError -> {
                    showAlert(
                        activity,
                        "Purchase Error",
                        "Purchase invalid, check payment source."
                    )
                }
                PurchasesErrorCode.NetworkError->{
                    showAlert(activity,"Network Error" ,"Check your internet connection")
                }
                PurchasesErrorCode.OperationAlreadyInProgressError->{
                    showAlert(activity,"Purchase Error" ,"Another purchase already in progress")
                }
                PurchasesErrorCode.PaymentPendingError->{
                    showAlert(activity,"Payment Error" ,"Purchase payment is pending")
                }
                PurchasesErrorCode.IneligibleError->{
                    showAlert(activity,"Eligibility Error" ,"Ineligible for purchase")
                }
                PurchasesErrorCode.ProductAlreadyPurchasedError->{
                    showAlert(activity, "Purchase Error","Subscription has already been purchased")
                }
                PurchasesErrorCode.PurchaseCancelledError->{
                    showAlert(activity,"Purchase Error" ,"Purchase was cancelled")
                }
                PurchasesErrorCode.ProductNotAvailableForPurchaseError->{
                    showAlert(activity, "Purchase Error","Current subscription is not available to purchase")
                }
                PurchasesErrorCode.StoreProblemError->{
                    showAlert(activity, "Error","Problem is facing from our side. Please try again later!")
                }

                else -> {
                    showAlert(activity, "Error" ,message)
                }
            }
        }

    }

    private fun showAlert(activity: Activity, title:String, message:String){
        ViewUtils.alertDialog(activity,
            title,
            message,
            "Okay",
            "",
            true
        ) { dialog, which ->
            when (which) {
                -1 -> {
                    dialog.dismiss()
                }
                -2 -> {
                    dialog.dismiss()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
     fun convertDateIntoMillies(date: String): Long {
        val formatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val localDate: LocalDateTime = LocalDateTime.parse(date, formatter)
        return localDate.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    fun logEvent(context: Context, eventType: String ,eventValues: Map<String, Any>) {
        AppsFlyerLib.getInstance().logEvent(
            context,
            eventType, eventValues, object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    Timber.tag("AppFlyer").d("log event success")
                }
                override fun onError(errorCode: Int, errorDesc: String) {
                    Timber.tag("AppFlyer").e("log event error $errorCode, desc: $errorDesc")
                }
            })
    }


}