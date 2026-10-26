package com.piu.vpn.vpn

import android.content.Intent
import android.net.VpnService
import androidx.activity.ComponentActivity

/**
 * VPN permission တောင်းခံခြင်းနှင့် Service ကို စတင်/ရပ်ဆိုင်းခြင်းကို စီမံသည်။
 */
class VpnConnectionManager(private val activity: ComponentActivity) {

    private var pendingConfig: String? = null
    private var permissionLauncher: ((Intent) -> Unit)? = null
    private var permissionResultHandler: (Boolean) -> Unit = {}

    fun setPermissionLauncher(launcher: (Intent) -> Unit) {
        permissionLauncher = launcher
    }

    fun registerLauncher(onResult: (Boolean) -> Unit) {
        permissionResultHandler = onResult
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            pendingConfig?.let { startService(it) }
        }
        permissionResultHandler(granted)
        pendingConfig = null
    }

    /**
     * WireGuard config string ကို ပေးပြီး VPN ကို စတင်ပါ။
     * Permission မရသေးရင် system dialog ပြပါမယ်။
     */
    fun connect(config: String) {
        val prepareIntent = VpnService.prepare(activity)
        if (prepareIntent != null) {
            pendingConfig = config
            checkNotNull(permissionLauncher) { "VPN permission launcher is not registered" }(prepareIntent)
        } else {
            startService(config)
        }
    }

    fun disconnect() {
        val intent = Intent(activity, WireGuardVpnService::class.java).apply {
            action = WireGuardVpnService.ACTION_STOP
        }
        activity.startService(intent)
    }

    private fun startService(config: String) {
        val intent = Intent(activity, WireGuardVpnService::class.java).apply {
            action = WireGuardVpnService.ACTION_START
            putExtra(WireGuardVpnService.EXTRA_CONFIG, config)
        }
        activity.startService(intent)
    }
}