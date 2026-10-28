package com.piu.vpn.ui.viewmodel

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import com.piu.vpn.ads.RewardedAdManager
import com.piu.vpn.data.Entitlement
import com.piu.vpn.data.VpnStats
import com.piu.vpn.data.api.ApiClient
import com.piu.vpn.data.repository.ApiResult
import com.piu.vpn.data.repository.VpnRepository
import com.piu.vpn.ui.state.VpnStatus
import com.piu.vpn.ui.state.VpnUiState
import com.piu.vpn.vpn.VpnConnectionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Instant

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val vpnRepo = VpnRepository()
    private val rewardedAd = RewardedAdManager(application.applicationContext)

    private val _uiState = MutableStateFlow(VpnUiState())
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()
    private var vpnManager: VpnConnectionManager? = null
    private var connectAfterAd = false
    private var adProgressJob: Job? = null

    private val ownerTag: String
        get() {
            val userId = jwtUserId()
            return if (!userId.isNullOrBlank()) {
                "user:$userId"
            } else {
                "install:${ApiClient.tokenStore.installId}"
            }
        }

    init {
        _uiState.update {
            it.copy(
                installId = ApiClient.tokenStore.installId,
                isGuest = !ApiClient.tokenStore.hasToken(),
            )
        }
        rewardedAd.onStateChanged = { isLoading, isReady ->
            _uiState.update {
                it.copy(
                    isAdLoading = isLoading,
                    isAdReady = isReady,
                    adLoadProgress = if (isReady) 100 else if (isLoading) it.adLoadProgress else 0,
                    adLoadFailed = false,
                )
            }
            if (isLoading) startAdLoadProgress() else adProgressJob?.cancel()
        }
        rewardedAd.onLoadFailed = {
            _uiState.update {
                it.copy(isAdLoading = false, isAdReady = false, adLoadProgress = 0, adLoadFailed = true)
            }

            // Only an AdMob load failure requests the server-controlled rare fallback.
            viewModelScope.launch {
                when (val result = vpnRepo.requestFallbackEntitlement()) {
                    is ApiResult.Success -> if (result.data.active) {
                        applyEntitlement(
                            result.data.active,
                            result.data.expiresAt,
                            result.data.remainingSeconds,
                            result.data.grantedBy,
                        )
                        connect()
                    }
                    else -> Unit
                }
            }
        }
        rewardedAd.load(ownerTag)
        startEntitlementPoller()
        startRemainingTicker()
    }

    fun attachVpnManager(manager: VpnConnectionManager) {
        vpnManager = manager
    }

    fun onVpnPermissionDenied() {
        _uiState.update { it.copy(status = VpnStatus.Error("VPN permission was denied")) }
    }

    fun onConnectClick() {
        val state = _uiState.value
        when (state.status) {
            is VpnStatus.Connected -> disconnect()
            is VpnStatus.Connecting -> { /* ignore */ }
            else -> {
                if (hasActiveGrant(state.entitlement)) {
                    connect()
                } else if (state.isAdReady && !state.adRequested) {
                    requestRewardedAd(connectAfter = true)
                } else if (!state.isAdLoading) {
                    rewardedAd.load(ownerTag)
                }
            }
        }
    }

    fun onWatchAdClick() {
        if (_uiState.value.isAdReady) {
            requestRewardedAd(connectAfter = true)
        } else if (!_uiState.value.isAdLoading) {
            rewardedAd.load(ownerTag)
        }
    }

    fun onAdRequestedConsumed() {
        _uiState.update { it.copy(adRequested = false) }
    }

    private fun requestRewardedAd(connectAfter: Boolean) {
        connectAfterAd = connectAfter
        _uiState.update {
            it.copy(adRequested = true, adError = null)
        }
    }

    fun consumeAdError() {
        _uiState.update { it.copy(adError = null) }
    }

    fun showRewardedAd(
        activity: android.app.Activity,
        onFailed: (String) -> Unit,
    ) {
        if (!_uiState.value.isAdReady) {
            rewardedAd.load(ownerTag)
            return
        }
        val tag = ownerTag
        rewardedAd.show(
            activity = activity,
            customData = tag,
            onReward = {
                viewModelScope.launch {
                    val shouldConnect = connectAfterAd
                    connectAfterAd = false
                    var lastNetworkError = false
                    repeat(8) {
                        delay(1000)
                        lastNetworkError = !fetchEntitlement()
                        if (hasActiveGrant(_uiState.value.entitlement)) {
                            if (shouldConnect && _uiState.value.status !is VpnStatus.Connected) {
                                connect()
                            }
                            return@launch
                        }
                    }

                    // Fallback: If ad reward was earned and connect was requested, attempt connection
                    if (shouldConnect && _uiState.value.status !is VpnStatus.Connected) {
                        connect()
                        delay(1000)
                        if (_uiState.value.status is VpnStatus.Connected || _uiState.value.status is VpnStatus.Connecting) {
                            return@launch
                        }
                    }

                    val msg = if (lastNetworkError) {
                        "Server မ ရောက်ပါ — ဖုန်းနဲ့ Ubuntu Wi-Fi တူလား စစ်ပါ"
                    } else {
                        "ကြော်ငြာ grant မ ရသေးပါ — AdMob SSV callback ကို စစ်ပါ"
                    }
                    _uiState.update { it.copy(adError = msg) }
                    onFailed(msg)
                }
            },
            onDismissed = { },
            onFailed = { msg ->
                connectAfterAd = false
                _uiState.update { it.copy(adError = msg) }
                onFailed(msg)
            },
        )
    }

    private fun startAdLoadProgress() {
        adProgressJob?.cancel()
        adProgressJob = viewModelScope.launch {
            while (_uiState.value.isAdLoading) {
                delay(300)
                _uiState.update { state ->
                    if (state.isAdLoading) {
                        state.copy(adLoadProgress = (state.adLoadProgress + 1).coerceAtMost(90))
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun refreshEntitlement() {
        viewModelScope.launch { fetchEntitlement() }
    }

    private suspend fun fetchEntitlement(): Boolean {
        return when (val result = vpnRepo.getEntitlement()) {
            is ApiResult.Success -> {
                applyEntitlement(
                    result.data.active,
                    result.data.expiresAt,
                    result.data.remainingSeconds,
                    result.data.grantedBy,
                )
                true
            }
            else -> false
        }
    }

    private fun applyEntitlement(
        active: Boolean,
        expiresAt: String?,
        remainingSeconds: Long,
        grantedBy: String?,
    ) {
        val remaining = remainingFromExpires(expiresAt).takeIf { it > 0 } ?: remainingSeconds
        val isActive = active && remaining > 0
        _uiState.update {
            val nextStatus = when {
                it.status is VpnStatus.Connected || it.status is VpnStatus.Connecting -> it.status
                isActive -> VpnStatus.Ready
                else -> VpnStatus.NoEntitlement
            }
            it.copy(
                entitlement = Entitlement(
                    active = isActive,
                    expiresAt = expiresAt,
                    remainingSeconds = remaining,
                    grantedBy = grantedBy,
                ),
                isGuest = !ApiClient.tokenStore.hasToken(),
                status = nextStatus,
            )
        }
    }

    private fun connect() {
        viewModelScope.launch {
            _uiState.update { it.copy(status = VpnStatus.Connecting) }
            when (val result = vpnRepo.getVpnConfig()) {
                is ApiResult.Success -> {
                    vpnManager?.connect(result.data.config)
                    _uiState.update {
                        it.copy(
                            status = VpnStatus.Connected(
                                stats = VpnStats(connectedAt = System.currentTimeMillis()),
                            ),
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(status = VpnStatus.Error(result.message)) }
                    if (result.code == 403) {
                        requestRewardedAd(connectAfter = true)
                    }
                }
                ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(status = VpnStatus.Error("Network error — ပြန်ကြိုးစားပါ"))
                    }
                }
            }
        }
    }

    private fun disconnect() {
        vpnManager?.disconnect()
        val ready = hasActiveGrant(_uiState.value.entitlement)
        _uiState.update {
            it.copy(status = if (ready) VpnStatus.Ready else VpnStatus.NoEntitlement)
        }
    }

    private fun startEntitlementPoller() {
        viewModelScope.launch {
            fetchEntitlement()
            while (true) {
                delay(60_000)
                fetchEntitlement()
            }
        }
    }

    private fun startRemainingTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                _uiState.update { state ->
                    val ent = state.entitlement ?: return@update state
                    val remaining = remainingFromExpires(ent.expiresAt)
                    if (remaining == ent.remainingSeconds && (remaining > 0) == ent.active) {
                        return@update state
                    }
                    val active = remaining > 0
                    val status = when {
                        !active && state.status is VpnStatus.Ready -> VpnStatus.NoEntitlement
                        else -> state.status
                    }
                    state.copy(
                        entitlement = ent.copy(active = active, remainingSeconds = remaining),
                        status = status,
                    )
                }
            }
        }
    }

    private fun hasActiveGrant(ent: Entitlement?): Boolean {
        if (ent == null) return false
        if (remainingFromExpires(ent.expiresAt) > 0) return true
        return ent.active && ent.remainingSeconds > 0
    }

    private fun remainingFromExpires(expiresAt: String?): Long {
        if (expiresAt.isNullOrBlank()) return 0
        return try {
            val end = Instant.parse(expiresAt)
            (end.epochSecond - Instant.now().epochSecond).coerceAtLeast(0)
        } catch (_: Exception) {
            0
        }
    }

    private fun jwtUserId(): String? {
        val token = ApiClient.tokenStore.accessToken ?: return null
        val payload = token.split(".").getOrNull(1) ?: return null
        return try {
            val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
            val json = String(Base64.decode(padded, Base64.URL_SAFE), Charsets.UTF_8)
            JSONObject(json).optString("uid").ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        rewardedAd.destroy()
    }
}
