package com.piu.vpn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piu.vpn.ui.theme.VpnGreen
import com.piu.vpn.ui.theme.VpnRed
import com.piu.vpn.ui.viewmodel.AccountViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    viewModel: AccountViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadAccount()
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    LaunchedEffect(state.logoutSuccess) {
        if (state.logoutSuccess) {
            viewModel.resetLogoutSuccess()
            onBack()
        }
    }
    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            viewModel.resetDeleteSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("👤", fontSize = 48.sp)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (state.isGuest) "Guest" else (state.user?.email ?: ""),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (state.isGuest)
                        "Install ID: ${state.installId.take(16)}…"
                    else
                        "Member since ${state.user?.createdAt?.take(10) ?: ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )

                Spacer(Modifier.height(24.dp))

                // Entitlement card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("24h Status", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        if (state.entitlement?.active == true) {
                            Text(
                                "ကျန်ချိန်: ${formatDuration(state.entitlement!!.remainingSeconds)}",
                                color = VpnGreen,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        } else {
                            Text(
                                "မရှိသေးပါ",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (state.isGuest) {
                    BenefitsList()
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToAuth,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    ) {
                        Text("Login / Register")
                    }
                } else {
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Logout")
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = VpnRed),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Delete Account")
                    }
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = onNavigateToPrivacy) {
                        Text("Privacy", fontSize = 12.sp)
                    }
                    Text("·", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    TextButton(onClick = onNavigateToTerms) {
                        Text("Terms", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout လုပ်မှာ သေချာလား?") },
            text = { Text("သင့်အကောင့်မှ ထွက်ပါမည်။ ၂၄ နာရီ အကျိုးအမြတ်များ ဆက်လက်ရရှိပါမည်။") },
            confirmButton = {
                Button(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            },
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletePassword = ""
            },
            icon = { Text("⚠️", fontSize = 40.sp) },
            title = { Text("Account ဖျက်မှာ သေချာလား?") },
            text = {
                Column {
                    Text("ဒါကို ပြန်ပြင်၍ မ ရပါ။ Data အားလုံး ဖျက်ပါမည်။", color = VpnRed)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Password ထည့်ပါ") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pwd = deletePassword
                        showDeleteDialog = false
                        deletePassword = ""
                        viewModel.deleteAccount(pwd)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VpnRed),
                    enabled = deletePassword.length >= 8,
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deletePassword = ""
                }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun BenefitsList() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Account ဖွင့်ရင် ရမည့် အကျိုးအမြတ်:",
            style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        BenefitRow("✓ App ဖျက်/ပြန်ထည့်ရင် ၂၄ နာရီ မ ပျက်")
        BenefitRow("✓ Device ၂ လုံးအထိ တစ်ပြိုင်နက် သုံးနိုင်")
        BenefitRow("✓ Account recovery")
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text(text, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return "${h}h ${m}m"
}