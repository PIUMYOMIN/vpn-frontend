package com.piu.vpn.vpn

import com.wireguard.android.backend.Tunnel

/**
 * WireGuard Tunnel ကို ကိုယ်စားပြုတဲ့ class။
 * Library က ဒီ object ကို အသုံးပြုပြီး tunnel အခြေအနေကို စီမံပါတယ်။
 */
class WgTunnel(private val name: String) : Tunnel {

    override fun getName(): String = name

    override fun onStateChange(newState: Tunnel.State) {
        // နောက်ပိုင်း ViewModel ကို update လုပ်ဖို့ ဒီနေရာမှာ callback ထည့်နိုင်ပါတယ်
        android.util.Log.d("WgTunnel", "State changed: $newState")
    }
}