package com.piu.vpn

import android.app.Application
import com.piu.vpn.data.api.ApiClient
import com.piu.vpn.data.local.TokenStore

class VpnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val tokenStore = TokenStore(this)
        ApiClient.init(tokenStore)
    }
}