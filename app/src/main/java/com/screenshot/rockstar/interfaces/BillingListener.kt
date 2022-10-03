package com.screenshot.rockstar.interfaces

import com.android.billingclient.api.Purchase

interface BillingListener {
    fun onProductPurchased(purchase: Purchase)
    fun onBillingFailure(responseCode: Int)
}