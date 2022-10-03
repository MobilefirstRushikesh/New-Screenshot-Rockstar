package com.screenshot.rockstar.ui

import android.app.Application
import com.appsflyer.AppsFlyerLib
import com.revenuecat.purchases.BuildConfig
import com.revenuecat.purchases.PurchaserInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.interfaces.UpdatedPurchaserInfoListener
import com.screenshot.rockstar.utils.Constants
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        Timber.d("onCreate")
        Purchases.debugLogsEnabled = true

        Purchases.configure(this, Constants.REVENUE_CAT_PRO_PUBLIC_KEY)

        Purchases.sharedInstance.updatedPurchaserInfoListener =
            object : UpdatedPurchaserInfoListener {
                override fun onReceived(purchaserInfo: PurchaserInfo) {

                }
            }

        AppsFlyerLib.getInstance().init("jnvrdafqEvzvgWirCxL8q5", null, this)
        AppsFlyerLib.getInstance().start(this)
        AppsFlyerLib.getInstance().setDebugLog(false)

    }
}