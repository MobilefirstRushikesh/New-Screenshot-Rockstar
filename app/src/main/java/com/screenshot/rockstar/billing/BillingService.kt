package com.screenshot.rockstar.billing


/*class BillingService(
    private val context: Context,
    private val billingListener: BillingListener
) : LifecycleObserver,
    PurchasesUpdatedListener, BillingClientStateListener,
    PurchasesResponseListener, SkuDetailsResponseListener, PurchaseHistoryResponseListener {


    private lateinit var billingClient: BillingClient

    //val skusWithSkuDetails = MutableLiveData<Map<String, SkuDetails>>()
    val skusWithSkuDetails = MutableLiveData<MutableList<SkuDetails>>()
    val alreadyPurchased = MutableLiveData<List<Purchase>>()

    private fun checkBillingSupported(): Boolean =
        billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).responseCode == BillingResponseCode.OK


    companion object {
        const val TAG = "BillingService"
        var isBillingReady = false
        var isProductsAvailableToPurchase = false
        var isPurchasedVerified = false

        val LIST_OF_SKUS = listOf(
            Constants.MONTHLY_SUB,
            Constants.YEARLY_SUB
        )
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        billingClient = BillingClient.newBuilder(context)
            .setListener(this@BillingService)
            .enablePendingPurchases()
            .build()


        if (!billingClient.isReady) {
            Log.d(TAG, "BillingClient: Start Connection..")
            billingClient.startConnection(this)
        }


    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        Log.d(TAG, "ON_DESTROY")
        if (billingClient.isReady) {
            Log.d(TAG, "BillingClient can only be used once -- closing connection")
            billingClient.endConnection()
        }
    }


    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingResponseCode.OK) {
            isBillingReady = true
            if (checkBillingSupported()) {
                querySkuDetails()
                queryPurchases()
            } else
                billingListener.onBillingFailure(BillingResponseCode.FEATURE_NOT_SUPPORTED)
        } else
            billingListener.onBillingFailure(responseCode)
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected")
        billingListener.onBillingFailure(BillingResponseCode.SERVICE_DISCONNECTED)
    }


    /**
     * [querySkuDetails] to get available product to purchase
     **/
    private fun querySkuDetails() {
        //Log.d(TAG, "querySkuDetails")
        if (billingClient.isReady) {
            val params = SkuDetailsParams.newBuilder()
                .setType(SUBS)
                .setSkusList(LIST_OF_SKUS)
                .build()
            params.let { skuDetailsParams ->
                //Log.i(TAG, "querySkuDetailsAsync")
                billingClient.querySkuDetailsAsync(skuDetailsParams, this)
            }
        } else
            billingListener.onBillingFailure(BillingResponseCode.SERVICE_DISCONNECTED)
    }


    /**
     * [onSkuDetailsResponse] returns list with all available skus
     **/

    override fun onSkuDetailsResponse(
        billingResult: BillingResult,
        skuDetailsList: MutableList<SkuDetails>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage

        when (responseCode) {
            BillingResponseCode.OK -> {
                //Log.i(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
                val expectedSkuDetailsCount = LIST_OF_SKUS.size
                if (skuDetailsList == null) {
                    skusWithSkuDetails.postValue(mutableListOf())
                    Log.e(

                        TAG, "onSkuDetailsResponse: " +
                                "Expected ${expectedSkuDetailsCount}, " +
                                "Found null SkuDetails. " +
                                "Check to see if the SKUs you requested are correctly published " +
                                "in the Google Play Console."
                    )
                    billingListener.onBillingFailure(BillingResponseCode.ITEM_UNAVAILABLE)
                } else
                    skusWithSkuDetails.postValue(skuDetailsList.also { postedValue ->
                        val skuDetailsCount = postedValue.size
                        if (skuDetailsCount == expectedSkuDetailsCount) {
                            isProductsAvailableToPurchase = true
                            Log.i(TAG, "onSkuDetailsResponse: Found $skuDetailsCount SkuDetails")
                        } else {
                            Log.e(
                                TAG, "onSkuDetailsResponse: " +
                                        "Expected $expectedSkuDetailsCount, " +
                                        "Found $skuDetailsCount SkuDetails. " +
                                        "Check to see if the SKUs you requested are correctly published " +
                                        "in the Google Play Console."
                            )
                        }
                    })
            }
            else -> {
                billingListener.onBillingFailure(responseCode)
            }
        }
    }


    /**
     ** call [queryPurchases] to retrieve all currently activate purchase items/subscription
     */
    private fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready")
            billingListener.onBillingFailure(BillingResponseCode.SERVICE_DISCONNECTED)
            return
        }
        billingClient.queryPurchasesAsync(SUBS, this)
    }

    /**
     * [onQueryPurchasesResponse] retrieve all currently activate purchase items/subscription
     **/

    override fun onQueryPurchasesResponse(
        billingResult: BillingResult,
        purchasesList: List<Purchase>
    ) {

        val responseCode = billingResult.responseCode
        val debugMassage = billingResult.debugMessage
        Log.d(TAG, "queryPurchasesResponse: ${purchasesList.size} purchases  ")

        if (purchasesList.isNotEmpty()) {
            if (responseCode == BillingResponseCode.OK) {
                for ((i, purchase) in purchasesList.withIndex()) {
                    Log.d(
                        TAG, "onQueryPurchaseResponse:  purchase $i : sku:" +
                                " ${purchase.skus} \n" +
                                "order ID: ${purchase.orderId} \n" +
                                "time: ${purchase.purchaseTime}, \n" +
                                "purchaseState: ${purchase.purchaseState} \n" +
                                "isAuto: ${purchase.isAutoRenewing} \n" +
                                "isAcknowledged: ${purchase.isAcknowledged} \n" +
                                "original jason: ${purchase.originalJson}"
                    )
                }
                alreadyPurchased.postValue(purchasesList)
            }
            else {
                billingListener.onBillingFailure(responseCode)
            }
        } else
            alreadyPurchased.postValue(emptyList())

    }


    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {

        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onPurchasesUpdated: $responseCode $debugMessage")
        when (responseCode) {
            BillingResponseCode.OK -> {
                if (purchases == null) {
                    Log.d(TAG, "onPurchasesUpdated: null purchase list")
                    processPurchases(null)
                } else {
                    Log.d(TAG, "onPurchasesUpdated: $responseCode")
                    processPurchases(purchases)
                }
            }
            else -> billingListener.onBillingFailure(responseCode)
        }
    }


    private fun processPurchases(purchasesList: List<Purchase>?) {
        Log.d(TAG, "processPurchases: ${purchasesList?.size} purchase(s)")
        if (purchasesList == null) {
            billingListener.onBillingFailure(BillingResponseCode.ITEM_NOT_OWNED)
            Log.d(TAG, "purchase null")
            return
        }

        purchasesList.forEach { purchase ->
            Log.d(TAG, "processPurchases: purchase State: ${purchase.purchaseState}")
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PENDING -> {

                }
                Purchase.PurchaseState.PURCHASED -> {
                    if (isPurchaseVerified(purchase)) {
                        isPurchasedVerified = true
                        acknowledgePurchase(purchase)
                    } else
                        billingListener.onBillingFailure(PURCHASE_VERIFICATION_FAILED)

                }
                Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                    billingListener.onBillingFailure(BillingResponseCode.ITEM_NOT_OWNED)
                }
            }
        }

    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val token = purchase.purchaseToken
        val orderId = purchase.orderId
        if (!purchase.isAcknowledged) {
            Log.d(TAG, "purchase is not acknowledge:")
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(token)
                .build()
            billingClient.acknowledgePurchase(params) { billingResult ->
                val responseCode = billingResult.responseCode
                val debugMessage = billingResult.debugMessage
                Log.d(TAG, "acknowledgePurchase: $responseCode")

                if (responseCode == BillingResponseCode.OK) {
                    Log.d(TAG, "acknowledge success: ${purchase.originalJson} ")
                    billingListener.onProductPurchased(purchase)
                } else {
                    billingListener.onBillingFailure(responseCode)
                }

            }
        } else {
            Log.d(TAG, "already acknowledge: $orderId , token: $token ")
            billingListener.onProductPurchased(purchase)
        }

    }

    /**
     * Launching the billing flow.
     *
     * Launching the UI to make a purchase requires a reference to the Activity.
     */
    fun launchBillingFlow(activity: Activity, sku: SkuDetails?) {
        if (billingClient.isReady && sku != null) {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(sku)
                .build()
            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            Log.d(TAG, "launchBillingFlow: BillingResponse $responseCode $debugMessage")

        } else {
            billingListener.onBillingFailure(BillingResponseCode.SERVICE_DISCONNECTED)
        }

    }


    /**
    * call to retrieve all currently activate purchase items/ subscription even it is expired
     */
    fun queryHistoryPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready")
        }
        Log.d(TAG, "queryPurchases: SUBS")
        billingClient.queryPurchaseHistoryAsync(SUBS, this)
    }

    /**
     *
    *  retrieve all purchase items/ subscription even canceled, expired
     */
    override fun onPurchaseHistoryResponse(
        billingResult: BillingResult,
        purchaseList: MutableList<PurchaseHistoryRecord>?
    ) {

        purchaseList?.forEachIndexed { index, purchase ->
            Log.d(
                TAG, "onPurchaseHistoryResponse:  $index : sku: ${purchase.skus}\n" +
                        "  time: ${purchase.purchaseTime}\n" +
                        "QTY: ${purchase.quantity}" +
                        " originalJson: ${purchase.originalJson}\n\n"
            )
        }

    }

    private fun isPurchaseVerified(purchase: Purchase): Boolean =
         Security.verifyPurchase(PLAY_CONSOLE_KEY, purchase.originalJson, purchase.signature)

}*/