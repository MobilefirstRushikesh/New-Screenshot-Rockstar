package com.screenshot.rockstar.ui.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.billingclient.api.SkuDetails
import com.appsflyer.AppsFlyerLib
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.revenuecat.purchases.*
import com.revenuecat.purchases.models.StoreTransaction
import com.screenshot.rockstar.R
import com.screenshot.rockstar.interfaces.BillingSetupFinished
import com.screenshot.rockstar.interfaces.ChangeFragmentListener
import com.screenshot.rockstar.utils.Constants
import com.screenshot.rockstar.utils.Constants.Companion.BASIC_SUB
import com.screenshot.rockstar.utils.Constants.Companion.INIT_SETUP_FRAG_TAG
import com.screenshot.rockstar.utils.Constants.Companion.REVENUE_ENTITLEMENT_ID_PRO
import com.screenshot.rockstar.utils.Constants.Companion.URL_TERMS_CONDITION
import com.screenshot.rockstar.utils.CustomFunctions.handleBillingError
import com.screenshot.rockstar.utils.SessionManager
import com.screenshot.rockstar.utils.SubscriptionType
import com.screenshot.rockstar.utils.ViewUtils.alertDialog
import com.screenshot.rockstar.utils.ViewUtils.showToast
import timber.log.Timber


class BillingFragment : BaseFragment() {

    //Logic variables
    private var selectedPlan: SubscriptionType? = null
    private lateinit var fragView: View
    private lateinit var listener: BillingSetupFinished
    private lateinit var skuDetailList: MutableList<SkuDetails>
    private var isFirstTime: Boolean = false
    private var currentlyActiveSub: String = BASIC_SUB
    private lateinit var changeFragmentListener: ChangeFragmentListener
    private lateinit var offeringPackages: ArrayList<Package>
    private var isProductsAvailable =  false
    private lateinit var cardViewLists: ArrayList<MaterialCardView>

    //View variables
    private lateinit var cardFree:MaterialCardView
    private lateinit var cardYearly:MaterialCardView
    private lateinit var cardMonthly:MaterialCardView
    private lateinit var loadingRel: RelativeLayout
    private lateinit var mainRel:RelativeLayout
    private lateinit var relActiveFree:RelativeLayout
    private lateinit var relActiveMonthly:RelativeLayout
    private lateinit var relActiveYearly:RelativeLayout
    private lateinit var checkedYearly: ImageView
    private lateinit var checkedMonthly:ImageView
    private lateinit var checkedFree:ImageView
    private lateinit var imgActiveFreeChecked:ImageView
    private lateinit var imgActiveMonthlyChecked:ImageView
    private lateinit var imgActiveYearlyChecked:ImageView
    private lateinit var btnProceed: MaterialButton
    private lateinit var btnRestore:MaterialButton
    private lateinit var txtTerms: TextView
    private lateinit var imgClose: ImageButton
    private lateinit var sessionManager: SessionManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragView =  inflater.inflate(
            R.layout.activity_billing,
            container, false
        )

        cardFree = fragView.findViewById(R.id.billing_card_basic)
        cardYearly = fragView.findViewById(R.id.billing_card_pro_yearly)
        cardMonthly = fragView.findViewById(R.id.billing_card_pro_monthly)
        checkedYearly = fragView.findViewById(R.id.billing_img_pro_yearly_checked)
        checkedMonthly = fragView.findViewById(R.id.billing_img_pro_checked_monthly)
        checkedFree = fragView.findViewById(R.id.billing_img_free_checked)
        btnProceed = fragView.findViewById(R.id.billing_btn_proceed)
        btnRestore = fragView.findViewById(R.id.billing_btn_restore)
        txtTerms = fragView.findViewById(R.id.billing_txt_terms)
        imgClose = fragView.findViewById(R.id.billing_btn_close)
        relActiveFree = fragView.findViewById(R.id.billing_free_active_rel)
        relActiveMonthly = fragView.findViewById(R.id.billing_monthly_active_rel)
        relActiveYearly = fragView.findViewById(R.id.billing_yearly_active_rel)
        imgActiveFreeChecked = fragView.findViewById(R.id.billing_img_free_checked_active)
        imgActiveMonthlyChecked = fragView.findViewById(R.id.billing_img_pro_checked_active)
        imgActiveYearlyChecked = fragView.findViewById(R.id.billing_img_pro_yearly_checked_active)
        loadingRel = fragView.findViewById(R.id.billing_loading_rel)
        mainRel = fragView.findViewById(R.id.billing_main_screen_rel)



        sessionManager = SessionManager(requireContext())
        isFirstTime = sessionManager.getFirstTimePref()

        if (isFirstTime){
            imgClose.visibility = View.GONE
        }

        init()
        return fragView
    }


    private fun init(){
        cardViewLists = arrayListOf(cardFree, cardMonthly, cardYearly)

        offeringPackages = arrayListOf()
        checkSubscriptionState()
        fetchOffering()
        setupUi()
        trackScreen("BillingScreen")
    }

    private fun setupUi(){

        skuDetailList  = mutableListOf()

        cardFree.setOnClickListener {
            setSelectedCardView(cardFree)
        }

        cardYearly.setOnClickListener {
            setSelectedCardView(cardYearly)
        }

        cardMonthly.setOnClickListener {
            setSelectedCardView(cardMonthly)
        }

        imgClose.setOnClickListener {
            changeFragmentListener.popBackStack()
        }

        btnProceed.setOnClickListener {
            setupPurchase()
        }

        btnRestore.setOnClickListener {
           restorePurchase()
        }

        txtTerms.setOnClickListener {
            setupTermsWeb()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            listener = context as BillingSetupFinished
            changeFragmentListener = context as ChangeFragmentListener
        }catch (e: ClassCastException){
            throw ClassCastException(context.toString() + "must implement " + "BillingSetupFinishedListener")
        }
    }


    private fun fetchOffering(){
        Purchases.sharedInstance.getOfferingsWith({ error ->
            // An error occurred
            handleBillingError(requireActivity(), error)

        }) { offerings ->
            offerings.current?.availablePackages?.takeUnless { it.isEmpty() }?.let {
                // All packages from current offering
                if (it.isNotEmpty()){
                    it.forEach { p:com.revenuecat.purchases.Package  ->

                        offeringPackages.add(p)
                    }
                    isProductsAvailable = true
                }

                Timber.d("fetchOffering: success: ${it.size}")

            }
        }
    }

    private fun makePurchase(pack:Package){
        Purchases.sharedInstance.purchasePackageWith(
            requireActivity(),
            packageToPurchase = pack ,
            onError = {error, userCancelled ->
                Timber.d( "makePurchase: userCancelled: $userCancelled")
                handleBillingError(requireActivity(), error)
            },
            onSuccess = { product, purchaserInfo ->
                if (purchaserInfo.entitlements[REVENUE_ENTITLEMENT_ID_PRO]?.isActive == true) {
                    Timber.d( "makePurchase: success: ${product.originalJson} ")
                    logPurchaseEvent(product, pack)

                    afterPurchaseSuccessSetup()
                }
            })
    }

    private fun logPurchaseEvent(product: StoreTransaction, pack: Package) {
        val eventValues: MutableMap<String, String> = HashMap()
        eventValues[Constants.SUBSCRIPTION_TYPE] = pack.product.sku
        AppsFlyerLib.getInstance().validateAndLogInAppPurchase(requireContext(),
            Constants.PLAY_CONSOLE_KEY,
            product.signature,
            product.originalJson.toString(),
            pack.product.originalPrice,
            pack.product.priceCurrencyCode,
            eventValues)

    }

    private fun checkSubscriptionState(){
        Purchases.sharedInstance.getCustomerInfoWith({ error ->
            Timber.e( "checkSubscriptionState: error: ${error.message}")
            handleBillingError(requireActivity(), error)
        })
        { purchaserInfo ->
            Timber.d("checkSubscriptionState: success: activeSub size: ${purchaserInfo.activeSubscriptions.size}")
            checkCondition(purchaserInfo)
        }
    }


    private fun checkCondition(purchaserInfo: CustomerInfo){

        Timber.d("checkCondition: ${purchaserInfo.entitlements.active.size}")
        purchaserInfo.entitlements.active.entries.forEach { map->
            map.value.let { entitlementInfo ->
                Timber.d( "checkCondition: \n" +
                        "isActive: ${entitlementInfo.isActive}\n" +
                        "billingIssue: ${entitlementInfo.billingIssueDetectedAt}\n" +
                        "expirationDate: ${entitlementInfo.expirationDate}\n" +
                        "willRenew: ${entitlementInfo.willRenew}\n" +
                        "unscribeDetect At: ${entitlementInfo.unsubscribeDetectedAt}")
            }
        }

        if (isFirstTime){
            if (purchaserInfo.activeSubscriptions.isEmpty())
                return
            else{
                setupFinish()
                return
            }
        }

        if (purchaserInfo.activeSubscriptions.isEmpty()) {
            setCardActive(cardFree)
            return
        }
        purchaserInfo.activeSubscriptions.forEach { sku ->
            when (sku) {
                SubscriptionType.MONTHLY_SUB.type -> setCardActive(cardMonthly)
                SubscriptionType.YEARLY_SUB.type -> setCardActive(cardYearly)
            }
        }

    }

    private fun restorePurchase(){
        Purchases.sharedInstance.restorePurchasesWith(onError = {error ->
            handleBillingError(requireActivity(), error)
        },
        onSuccess = { purchaserInfo ->
            if(purchaserInfo.entitlements[REVENUE_ENTITLEMENT_ID_PRO]?.isActive == true){
                afterPurchaseSuccessSetup()
            }
        })
    }

    private fun getSelectedPackage(selected: String): Package?{
        for (offering in offeringPackages){
            if (offering.product.sku == selected)
                return offering
        }
        return null
    }

    private fun setupPurchase() {
        if (selectedPlan != null){

            when(selectedPlan) {
                SubscriptionType.BASIC_SUB -> {
                    if (isFirstTime)
                        setupFinish()
                }

                SubscriptionType.MONTHLY_SUB, SubscriptionType.YEARLY_SUB -> {
                    if (isProductsAvailable){
                        getSelectedPackage(selectedPlan!!.type)?.let {
                            makePurchase(it)
                        }
                    }
                }
                else -> {}
            }

        }else{

            showToast(requireActivity() as AppCompatActivity, "Please select plan first")
        }
    }


    private fun setCardActive(cardView: MaterialCardView) = cardView.apply {
        Timber.d( "setCardActive: ")
        cardViewLists.forEach { card->
            Timber.d("setCardActive: cardMatch: ${card == this} ")
            checkCard(card, card == this)
        }
    }

    private fun checkCard(cardView: MaterialCardView, active:Boolean){
        when(cardView){
            cardFree->{
                Timber.d( "checkCard: free ")
                if (active){
                    btnProceed.visibility = View.VISIBLE
                    txtTerms.visibility = View.VISIBLE
                }
                setAsCurrentActive(cardFree, relActiveFree, imgActiveFreeChecked, active)
            }
            cardMonthly->{
                if (active) {
                    cardFree.visibility = View.GONE
                    btnProceed.visibility = View.VISIBLE
                    txtTerms.visibility = View.VISIBLE
                }
                setAsCurrentActive(cardMonthly, relActiveMonthly, imgActiveMonthlyChecked, active)
            }
            cardYearly->{
                if (active){
                    cardFree.visibility = View.GONE
                    cardMonthly.visibility = View.GONE
                    btnProceed.visibility = View.GONE
                    txtTerms.visibility = View.GONE
                }
                setAsCurrentActive(cardYearly, relActiveYearly, imgActiveYearlyChecked, active)
            }
        }
    }

    private fun setAsCurrentActive(cardView: MaterialCardView , relActive:RelativeLayout , imgActive: ImageView , active: Boolean){
        if (active){
            Timber.d( "setAsCurrentActive: isActive ")
            cardView.isClickable = false
            cardView.isCheckable = false
            cardView.isFocusable = false
            cardView.isEnabled = false
            relActive.visibility = View.VISIBLE
            imgActive.visibility =  View.VISIBLE
        }
        else{
            cardView.isClickable = true
            relActive.visibility = View.GONE
            imgActive.visibility =  View.GONE
        }

    }


    private fun setSelectedCardView(selectedCard:MaterialCardView){
        checkedFree.visibility = View.GONE
        checkedMonthly.visibility = View.GONE
        checkedYearly.visibility = View.GONE


        when (selectedCard) {
            cardFree -> {
                deselectOtherCards(cardFree, cardMonthly,cardYearly)
                checkedFree.visibility = View.VISIBLE
                btnProceed.text = getString(R.string.proceed_with_basic)
                selectedPlan = SubscriptionType.BASIC_SUB
            }
            cardYearly -> {
                deselectOtherCards(cardYearly, cardMonthly,cardFree)
                checkedYearly.visibility = View.VISIBLE
                btnProceed.text = getString(R.string.proceed_with_pro_yearly)
                selectedPlan = SubscriptionType.YEARLY_SUB

            }
            cardMonthly -> {
                deselectOtherCards(cardMonthly, cardFree,cardYearly)
                checkedMonthly.visibility = View.VISIBLE
                btnProceed.text = getString(R.string.proceed_with_pro_monthly)
                selectedPlan = SubscriptionType.MONTHLY_SUB
            }
        }

    }

    private fun deselectOtherCards(selectedCard: MaterialCardView, deselectedCard1:MaterialCardView , deselectedCard2: MaterialCardView){
        deselectedCard1.strokeColor = ContextCompat.getColor(requireContext(),R.color.theme_light_grey_E7E7E7)
        deselectedCard2.strokeColor = ContextCompat.getColor(requireContext(),R.color.theme_light_grey_E7E7E7)
        selectedCard.strokeColor = ContextCompat.getColor(requireContext(),R.color.theme_violet)
    }


    private fun afterPurchaseSuccessSetup(){


        requireActivity().runOnUiThread {
            alertDialog(requireActivity(),
                "Congratulation! Subscription purchased",
                "Enjoy unlimited screenshots",
                "Okay",
                "",
                false,
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        -1 -> {
                            if (sessionManager.getFirstTimePref())
                                setupFinish()

                            else{
                                if (SearchFragment.isProcessing){
                                    SearchFragment.requireBreak = true

                                    Handler(Looper.getMainLooper()).postDelayed({
                                        changeFragmentListener.removeAndAddFragment(
                                            Constants.SEARCH_FRAG_TAG, INIT_SETUP_FRAG_TAG,
                                            addToBackStack = false,
                                            popCurrentFragFromBackStack = true
                                        )
                                    }, 2000)

                                }
                                else{
                                    dialog.dismiss()
                                    changeFragmentListener.removeAndAddFragment(
                                        Constants.SEARCH_FRAG_TAG, INIT_SETUP_FRAG_TAG,
                                        addToBackStack = false,
                                        popCurrentFragFromBackStack = true
                                    )
                                }
                            }

                        }
                    }
                })

        }
    }


    private fun setupFinish() = listener.onBillingSetupFinished()


    private fun setupTermsWeb() =
        startActivity(Intent("android.intent.action.VIEW", Uri.parse(URL_TERMS_CONDITION)))


}