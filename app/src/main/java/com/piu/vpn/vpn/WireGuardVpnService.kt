package com.piu.vpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.piu.vpn.MainActivity
import com.piu.vpn.R
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WireGuardVpnService : GoBackend.VpnService() {

    companion object {
        private const val TAG = "WireGuardVpnService"
        private const val NOTIF_ID = 1
        private const val CHANNEL_ID = "vpn_channel"

        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val EXTRA_CONFIG = "config"
    }

    private var backend: GoBackend? = null
    private var tunnel: WgTunnel? = null
    private var serviceJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        backend = GoBackend(this)
        createNotificationChannel()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val configStr = intent.getStringExtra(EXTRA_CONFIG) ?: return START_NOT_STICKY
                startVpn(configStr)
            }
            ACTION_STOP -> stopVpn()
        }
        return START_STICKY
    }

    private fun startVpn(configStr: String) {
        // Foreground notification (Android 8+ အတွက် မဖြစ်မနေ)
        startForeground(NOTIF_ID, buildNotification("ချိတ်ဆက်နေသည်..."))

        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val config = Config.parse(configStr.byteInputStream())
                val wgTunnel = WgTunnel("piu-vpn")
                tunnel = wgTunnel

                // Backend ကို သုံးပြီး tunnel ကို ချိတ်
                backend?.setState(wgTunnel, Tunnel.State.UP, config)

                Log.d(TAG, "Tunnel UP")
                updateNotification("ချိတ်ဆက်ပြီး")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start VPN", e)
                stopVpn()
            }
        }
    }

    private fun stopVpn() {
        serviceJob?.cancel()
        serviceJob = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                tunnel?.let { backend?.setState(it, Tunnel.State.DOWN, null) }
            } catch (e: Exception) {
                Log.e(TAG, "Stop error", e)
            } finally {
                tunnel = null
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onRevoke() {
        // အခြား VPN app က ဝင်လာရင် ဒါမှမဟုတ် user က ပိတ်ရင်
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        backend = null
        super.onDestroy()
    }

    // ============================================================
    // Notification
    // ============================================================

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN",
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PIU VPN")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, buildNotification(text))
    }
}