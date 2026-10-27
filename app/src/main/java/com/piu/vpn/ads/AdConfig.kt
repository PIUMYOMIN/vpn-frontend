package com.piu.vpn.ads

import com.piu.vpn.BuildConfig

object AdConfig {

    // ============================================================
    // Google Test Ad Unit IDs (safe for development)
    // ============================================================
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_APP_OPEN = "ca-app-pub-3940256099942544/9257395921"

    // ============================================================
    // Production Ad Unit IDs (သင့် AdMob က real IDs)
    // ============================================================
    private const val PROD_REWARDED = "ca-app-pub-6761722362940534/6659533765"
    private const val PROD_BANNER = "ca-app-pub-XXXXXXXXXXXXXXXX/BBBBBBBBBBBB"
    private const val PROD_APP_OPEN = "ca-app-pub-XXXXXXXXXXXXXXXX/CCCCCCCCCC"

    // ============================================================
    // Auto-switch: debug → test, release → prod
    // ============================================================
    val rewarded: String get() = if (BuildConfig.DEBUG) TEST_REWARDED else PROD_REWARDED
    val banner: String get() = if (BuildConfig.DEBUG) TEST_BANNER else PROD_BANNER
    val appOpen: String get() = if (BuildConfig.DEBUG) TEST_APP_OPEN else PROD_APP_OPEN

    // ============================================================
    // Frequency caps (documentation ထဲက အတိုင်း)
    // ============================================================
    const val BANNER_REFRESH_SECONDS = 60
    const val BANNER_MAX_PER_SESSION = 5
    const val BANNER_MAX_PER_DAY = 20
    const val APP_OPEN_MIN_INTERVAL_HOURS = 24
}