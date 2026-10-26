package com.piu.vpn.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.concurrent.TimeUnit

/**
 * App Open Ad — ၂၄ နာရီ ၁ ကြိမ်သာ ပြ (frequency cap ပါ).
 */
class AppOpenAdManager(private val context: Context) {

    companion object {
        private const val TAG = "AppOpenAd"
        private const val PREFS = "app_open_ad"
        private const val KEY_LAST_SHOWN = "last_shown_ms"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false
    private var loadTimeMs: Long = 0

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun canShow(): Boolean {
        if (appOpenAd == null) return false
        // Ads expire after 4 hours
        val age = System.currentTimeMillis() - loadTimeMs
        if (age > TimeUnit.HOURS.toMillis(4)) return false

        // Frequency cap: 24 hours
        val lastShown = prefs.getLong(KEY_LAST_SHOWN, 0L)
        val interval = TimeUnit.HOURS.toMillis(
            AdConfig.APP_OPEN_MIN_INTERVAL_HOURS.toLong()
        )
        return System.currentTimeMillis() - lastShown > interval
    }

    fun load() {
        if (isLoading || appOpenAd != null) return
        isLoading = true

        AppOpenAd.load(
            context,
            AdConfig.appOpen,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "onAdLoaded")
                    appOpenAd = ad
                    loadTimeMs = System.currentTimeMillis()
                    isLoading = false
                }

                override fun onAdFailedToLoad(err: LoadAdError) {
                    Log.e(TAG, "onAdFailedToLoad: ${err.message}")
                    appOpenAd = null
                    isLoading = false
                }
            },
        )
    }

    fun showIfReady(activity: Activity, onDismissed: () -> Unit) {
        val ad = appOpenAd
        if (ad == null || !canShow()) {
            onDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "onAdDismissed")
                appOpenAd = null
                prefs.edit()
                    .putLong(KEY_LAST_SHOWN, System.currentTimeMillis())
                    .apply()
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(err: AdError) {
                Log.e(TAG, "onAdFailedToShow: ${err.message}")
                appOpenAd = null
                onDismissed()
            }
        }

        ad.show(activity)
    }
}