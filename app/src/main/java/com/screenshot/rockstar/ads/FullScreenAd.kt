package com.screenshot.rockstar.ads
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.screenshot.rockstar.utils.Constants
import com.screenshot.rockstar.utils.ViewUtils.showToast

class FullScreenAd(private  val activity: AppCompatActivity, private var mInterstitialAd:InterstitialAd?) {


    fun loadInterstitialAd(){
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity, Constants.INTERSTITIAL_AD_UNIT_ID_TEST, adRequest,
            object : InterstitialAdLoadCallback() {

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                            "message: ${adError.message}"
                    showToast(activity, "onAdFailedToLoad() with error $error")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    showInterstitial()
                    showToast(activity, "Ad loading")
                }
            })
    }

    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                    // loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is dismissed.
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            showToast(activity, "Ad wasn't loaded")
        }
    }
}