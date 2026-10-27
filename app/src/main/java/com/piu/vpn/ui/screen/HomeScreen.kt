package com.piu.vpn.ui.screens

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piu.vpn.ui.state.VpnStatus
import com.piu.vpn.ui.state.VpnUiState
import com.piu.vpn.ui.theme.VpnAmber
import com.piu.vpn.ui.theme.VpnBlue
import com.piu.vpn.ui.theme.VpnGray
import com.piu.vpn.ui.theme.VpnGreen
import com.piu.vpn.ui.theme.VpnRed
import com.piu.vpn.ui.viewmodel.VpnViewModel
import com.piu.vpn.vpn.VpnConnectionManager

@Composable
fun HomeScreen(
    onNavigateToAccount: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: VpnViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val vpnManager = remember(activity) {
        activity?.let { VpnConnectionManager(it) }
    }
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        vpnManager?.onPermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(vpnManager, vpnPermissionLauncher) {
        vpnManager?.setPermissionLauncher { intent ->
            vpnPermissionLauncher.launch(intent)
        }
        vpnManager?.registerLauncher { granted ->
            if (!granted) viewModel.onVpnPermissionDenied()
        }
        vpnManager?.let(viewModel::attachVpnManager)
    }

    //Rewarded ad
    LaunchedEffect(state.adRequested) {
        if (state.adRequested && activity != null) {
            viewModel.showRewardedAd(activity){ errorMsg -> 
            //show error via snackbar or dialog (for now log)
            android.util.Log.e("RewardedAd", "Failed: $errorMsg")
            }
            viewModel.onAdRequestedConsumed()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.adError) {
        val msg = state.adError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.consumeAdError()
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onAccountClick = onNavigateToAccount,
                onSettingsClick = onNavigateToSettings,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when {
                    state.isAdLoading -> {
                        LinearProgressIndicator(
                            progress = { state.adLoadProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "လုပ်ဆောင်နေဆဲ (ခန့်မှန်း) ${state.adLoadProgress}%",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                    state.isAdReady -> {
                        Text(
                            text = "ချိတ်ဆက်လို့ရပါပြီ",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                    state.adLoadFailed -> {
                        Text(
                            text = "ပြန်ကြိုးစားပါ",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                com.piu.vpn.ads.BannerAdSlot()
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Status Icon
            StatusIcon(state.status)

            Spacer(Modifier.height(16.dp))

            // Status Text
            Text(
                text = statusLabel(state.status),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor(state.status),
            )

            // IP + Server (connected ဖြစ်မှ)
            if (state.status is VpnStatus.Connected) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${state.installId.take(8)}… · Singapore",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            Spacer(Modifier.height(40.dp))

            // Connect Button
            ConnectButton(
                status = state.status,
                enabled = state.status is VpnStatus.Connected ||
                    state.entitlement?.active == true ||
                    !state.isAdLoading,
                onClick = viewModel::onConnectClick,
            )

            if (state.status is VpnStatus.Connected) {
                Spacer(Modifier.height(40.dp))
                EntitlementCard(
                    state = state,
                    onWatchAd = viewModel::onWatchAdClick,
                )
            }
        }
    }
}

// ============================================================
// Components
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                "VPN",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        },
        actions = {
            TextButton(onClick = onAccountClick) {
                Text("👤", fontSize = 20.sp)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
    )
}

@Composable
private fun StatusIcon(status: VpnStatus) {
    val color = statusColor(status)
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = when (status) {
                is VpnStatus.Connected -> "🛡"
                is VpnStatus.Connecting -> "🔄"
                is VpnStatus.Error -> "⚠️"
                else -> "🔒"
            },
            fontSize = 64.sp,
        )
    }
}

@Composable
private fun ConnectButton(
    status: VpnStatus,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val isConnecting = status is VpnStatus.Connecting
    val bgColor = when (status) {
        is VpnStatus.Connected -> VpnGreen
        is VpnStatus.Connecting -> VpnAmber
        is VpnStatus.Error -> VpnRed
        else -> VpnBlue
    }
    val label = when (status) {
        is VpnStatus.Connected -> "DISCONNECT"
        is VpnStatus.Connecting -> "CONNECTING…"
        is VpnStatus.Error -> "RETRY"
        else -> "CONNECT"
    }

    Button(
        onClick = onClick,
        enabled = enabled && !isConnecting,
        modifier = Modifier.size(200.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚡️", fontSize = 40.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EntitlementCard(
    state: VpnUiState,
    onWatchAd: () -> Unit,
) {
    val ent = state.entitlement
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (ent?.active == true) {
                Text(
                    text = "⏱️ ကျန်ချိန်: ${formatDuration(ent.remainingSeconds)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "ကြော်ငြာထပ်ကြည့်ရင် ကျန်ချိန်ပေါ် ၂၄ နာရီ ထပ်ပေါင်းမယ်",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            } else {
                Text(
                    text = "🎁 ၂၄ နာရီ အခမဲ့",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "ကြော်ငြာတစ်ခု ကြည့်ပြီး စတင်အသုံးပြုပါ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onWatchAd) {
                Text("▶️ ကြော်ငြာကြည့်မယ်")
            }
        }
    }
}

// ============================================================
// Helpers
// ============================================================

private fun statusLabel(status: VpnStatus): String = when (status) {
    is VpnStatus.Connected -> "ချိတ်ဆက်ပြီး"
    is VpnStatus.Connecting -> "ချိတ်ဆက်နေသည်…"
    is VpnStatus.Error -> "အမှား"
    is VpnStatus.Ready -> "အသင့်"
    else -> "မ ချိတ်ဆက်ရသေး"
}

private fun statusColor(status: VpnStatus): Color = when (status) {
    is VpnStatus.Connected -> VpnGreen
    is VpnStatus.Connecting -> VpnAmber
    is VpnStatus.Error -> VpnRed
    else -> VpnGray
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return "${h} နာရီ ${m} မိနစ်"
}
