package com.piu.vpn.ads

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

/**
 * Bottom banner ad slot. Uses AndroidView to embed AdView.
 *
 * Refresh: AdMob default (60s). Session cap managed by AdMob.
 */
@Composable
fun BannerAdSlot(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.banner,
) {
    val context = LocalContext.current

    // Initialize MobileAds (idempotent — safe to call multiple times)
    DisposableEffect(Unit) {
        MobileAds.initialize(context) { status ->
            Log.d("BannerAd", "MobileAds init: $status")
        }
        onDispose { }
    }

    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
    ) {
        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}