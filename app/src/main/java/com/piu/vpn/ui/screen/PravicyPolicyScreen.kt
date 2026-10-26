package com.piu.vpn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text("Privacy Policy", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Last updated: 2026-10-22",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(Modifier.height(20.dp))

            Section("1. ကျွန်ုပ်တို့ စုဆောင်းသော အချက်အလက်")
            Body("• Email (အကောင့်ဖွင့်ပါက)\n" +
                    "• Install ID (guest အနေနဲ့ သုံးပါက)\n" +
                    "• Device နှင့် app အချက်အလက် (advertising ID အပါ)\n" +
                    "• Server log (IP, timestamp — ၇ ရက်သာ)")

            Section("2. VPN Traffic")
            Body("ကျွန်ုပ်တို့သည် သင့်ရဲ့ VPN traffic ကို " +
                    "ဘယ်တော့မှ log မ လုပ်ပါ။ " +
                    "သင် ဘယ် website သွားသည်ကို မ သိပါ။")

            Section("3. ကြော်ငြာ (AdMob)")
            Body("ကျွန်ုပ်တို့သည် Google AdMob ကို သုံးပါသည်။ " +
                    "AdMob သည် device advertising ID ကို စုဆောင်းနိုင်ပါသည်။ " +
                    "VPN traffic ကို ad network များဆီ မ မျှဝေပါ။")

            Section("4. Data သိမ်းဆည်းမှု ကာလ")
            Body("• Guest install: ၃၀ ရက် မလှုပ်ရှားရင် ဖျက်\n" +
                    "• Entitlements: သက်တမ်းကုန်ရင် ဖျက်\n" +
                    "• Refresh tokens: revoke ပြီး ၇ ရက်အကြာ ဖျက်")

            Section("5. သင့်အခွင့်အရေးများ")
            Body("• Account ဖျက်နိုင်သည်\n" +
                    "• Data မိတ္တူ တောင်းနိုင်သည်\n" +
                    "• support@yourdomain.com သို့ ဆက်သွယ်နိုင်သည်")

            Section("6. ဆက်သွယ်ရန်")
            Body("support@yourdomain.com")

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun Body(text: String) {
    Text(text, fontSize = 14.sp, lineHeight = 22.sp)
}