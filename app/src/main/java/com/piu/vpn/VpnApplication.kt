package com.piu.vpn

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.piu.vpn.data.api.ApiClient
import com.piu.vpn.data.local.TokenStore

class VpnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        val tokenStore = TokenStore(this)
        ApiClient.init(tokenStore)
    }
}