package com.piu.vpn

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.piu.vpn.ads.AppOpenAdManager
import com.piu.vpn.ui.screens.SplashScreen
import com.piu.vpn.navigation.VpnNavHost
import com.piu.vpn.ui.theme.MyVPNTheme

class MainActivity : ComponentActivity() {

    private var deepLinkToken by mutableStateOf<String?>(null)
    private lateinit var appOpenAdManager: com.piu.vpn.ads.AppOpenAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appOpenAdManager = AppOpenAdManager(applicationContext)
        appOpenAdManager.load()

        handleIntent(intent)

        setContent {
            val prefs = remember { com.piu.vpn.data.prefs.UserPreferences.get(this) }
            val theme by prefs.theme.collectAsState(initial = "system")

            val darkTheme = when (theme) {
                "dark" -> true
                "light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyVPNTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(onFinished = {
                            showSplash = false
                            appOpenAdManager.showIfReady(this) { }
                        })
                    } else {
                        VpnNavHost(
                            initialResetToken = deepLinkToken,
                            onResetTokenConsumed = { deepLinkToken = null },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Show App Open if ready + frequency cap allows
        appOpenAdManager.showIfReady(this) {
            // dismissed — no-op
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "vpn" && data.host == "reset-password") {
            deepLinkToken = data.getQueryParameter("token")
        }
    }
}