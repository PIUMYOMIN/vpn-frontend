package com.piu.vpn.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions

/**
 * Wraps RewardedAd loading + showing.
 *
 * သတိပြုရန်: `onUserEarnedReward` က UX အတွက်ပဲ — grant ကို
 * backend SSV က ပေးတာ။ Client ကို မ ယုံပါနဲ့။
 */
class RewardedAdManager(private val context: Context) {

    companion object {
        private const val TAG = "RewardedAd"
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    var onRewarded: (() -> Unit)? = null
        private set

    var onStateChanged: ((isLoading: Boolean, isReady: Boolean) -> Unit)? = null
    var onLoadFailed: (() -> Unit)? = null

    fun isReady(): Boolean = rewardedAd != null

    /**
     * Pre-load a rewarded ad. Call this on Home screen entry so that
     * the ad is ready when user taps "Watch ad".
     *
     * @param customData SSV owner tag: "install:<uuid>" or "user:<uuid>"
     */
    fun load(customData: String) {
        if (isLoading || rewardedAd != null) return
        isLoading = true
        onStateChanged?.invoke(true, false)

        val request = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            AdConfig.rewarded,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "onAdLoaded")
                    val options = ServerSideVerificationOptions.Builder()
                        .setCustomData(customData)
                        .build()
                    ad.setServerSideVerificationOptions(options)
                    rewardedAd = ad
                    isLoading = false
                    onStateChanged?.invoke(false, true)
                }

                override fun onAdFailedToLoad(err: LoadAdError) {
                    Log.e(TAG, "onAdFailedToLoad: ${err.message} (${err.code})")
                    rewardedAd = null
                    isLoading = false
                    onStateChanged?.invoke(false, false)
                    onLoadFailed?.invoke()
                }
            },
        )
    }

    /**
     * Show the loaded rewarded ad.
     *
     * @param activity current Activity
     * @param onReward called when user finishes the ad (UX only)
     * @param onDismissed called after ad closed (success or not)
     * @param onFailed called when ad couldn't be shown
     */
    fun show(
        activity: Activity,
        customData: String,
        onReward: () -> Unit,
        onDismissed: () -> Unit,
        onFailed: (String) -> Unit,
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onFailed("ကြော်ငြာ မ အဆင်သင့်ဖြစ်သေးပါ — ခဏနေမှ ပြန်စမ်းပါ")
            load(customData)
            return
        }

        val options = ServerSideVerificationOptions.Builder()
            .setCustomData(customData)
            .build()
        ad.setServerSideVerificationOptions(options)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "onAdDismissedFullScreenContent")
                rewardedAd = null
                onStateChanged?.invoke(false, false)
                onDismissed()
                // Pre-load for next time
                load(customData)
            }

            override fun onAdFailedToShowFullScreenContent(err: AdError) {
                Log.e(TAG, "onAdFailedToShow: ${err.message}")
                rewardedAd = null
                onStateChanged?.invoke(false, false)
                onFailed(err.message)
                load(customData)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "onAdShowedFullScreenContent")
            }
        }

        ad.show(activity) {
            Log.d(TAG, "onUserEarnedReward")
            onReward()
        }
    }

    fun destroy() {
        rewardedAd = null
        isLoading = false
    }
}