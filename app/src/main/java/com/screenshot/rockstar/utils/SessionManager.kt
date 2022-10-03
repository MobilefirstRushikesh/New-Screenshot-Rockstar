package  com.screenshot.rockstar.utils

import android.content.Context
import android.content.SharedPreferences
import com.screenshot.rockstar.utils.Constants.Companion.DEFAULT_DIRECTORY
import com.screenshot.rockstar.utils.Constants.Companion.DIRECTORY_PREF
import com.screenshot.rockstar.utils.Constants.Companion.FIRST_TIME
import com.screenshot.rockstar.utils.Constants.Companion.HAS_SUBSCRIPTION_PREF
import com.screenshot.rockstar.utils.Constants.Companion.PREF_NAME
import com.screenshot.rockstar.utils.Constants.Companion.PREV_SUBSCRIPTION_PREF
import com.screenshot.rockstar.utils.Constants.Companion.SORT_PREF
import com.screenshot.rockstar.utils.Constants.Companion.SORT_PREFERENCE_DATE_DESC



class SessionManager(context: Context) {

    private var pref: SharedPreferences? = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    var editor: SharedPreferences.Editor? = pref!!.edit()


    /**
     * user subscription pref
     */
    fun prevSubPref(prevSub:Boolean){
        editor!!.putBoolean(PREV_SUBSCRIPTION_PREF, prevSub)?.commit()
    }

    fun getPrevSub():Boolean{
        return pref!!.getBoolean(PREV_SUBSCRIPTION_PREF, false)
    }

    /*@Deprecated(
        "function is deprecated. Use addSubPref(SubscriptionInfo: Sting) instead", ReplaceWith(
            "editor!!.putBoolean(HAS_SUBSCRIPTION_PREF, hasSubscription).commit()",
            "com.jaadoo.screenshots.utils.Constants.Companion.HAS_SUBSCRIPTION_PREF"
        )
    )
    fun addSubsPref(hasSubscription:Boolean){
        editor!!.putBoolean(HAS_SUBSCRIPTION_PREF,hasSubscription ).commit()
    }*/

    fun addSubsPref(subscriptionType: SubscriptionType){
        editor!!.putString(HAS_SUBSCRIPTION_PREF, subscriptionType.name).commit()
    }

    /*fun getSubscriptionPref(): Boolean {
        return  pref!!.getBoolean(HAS_SUBSCRIPTION_PREF, false)
    }*/

    fun getSubscriptionPref(): String? {
        return  pref!!.getString(HAS_SUBSCRIPTION_PREF, SubscriptionType.BASIC_SUB.name)
    }


    /**
     * sort pref
     */
    fun addSortPref(sortPreferences: String){
        editor!!.putString(SORT_PREF, sortPreferences).commit()
    }

    fun getSortPref(): String {
        return  pref!!.getString(SORT_PREF, SORT_PREFERENCE_DATE_DESC)!!
    }



    /**
     *  first time pref
     */
    fun addFirstTimePref(firstTime:Boolean){
        editor!!.putBoolean(FIRST_TIME, firstTime).commit()
    }

    fun getFirstTimePref(): Boolean {
        return pref!!.getBoolean(FIRST_TIME, true)
    }



    /**
     * directory pref
     */
    fun addDirectoryPref(DirPreferences: String){
        editor!!.putString(DIRECTORY_PREF, DirPreferences).commit()
    }

    fun getDirPref(): String? {
        return  pref!!.getString(DIRECTORY_PREF, pref!!.getString(DEFAULT_DIRECTORY, null))
    }

    fun setDefaultDirectory(defaultDir: String){
        editor!!.putString(DEFAULT_DIRECTORY, defaultDir)
    }

}