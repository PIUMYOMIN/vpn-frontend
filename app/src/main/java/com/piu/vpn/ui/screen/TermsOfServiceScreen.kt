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
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms of Service") },
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
            Text("Terms of Service", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Last updated: 2026-10-22",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(Modifier.height(20.dp))

            Section("1. ဝန်ဆောင်မှု ဖော်ပြချက်")
            Body("ကျွန်ုပ်တို့သည် VPN ဝန်ဆောင်မှုကို " +
                    "ကြော်ငြာ ကြည့်ခြင်းဖြင့် ၂၄ နာရီ အခမဲ့ " +
                    "အသုံးပြုနိုင်သော ပုံစံဖြင့် ပေးအပ်ပါသည်။")

            Section("2. အသုံးပြုသူ တာဝန်")
            Body("သင် သည် အောက်ပါအချက်များကို မ လုပ်ရပါ:\n" +
                    "• တရားမ ဝင်သော လုပ်ဆောင်ချက်များ\n" +
                    "• အခြားသူများကို ထိခိုက်စေသော လုပ်ဆောင်ချက်များ\n" +
                    "• Spam, malware ဖြန့်ဝေခြင်း\n" +
                    "• ကျွန်ုပ်တို့၏ စနစ်ကို ဖျက်ဆီးရန် ကြိုးစားခြင်း")

            Section("3. အကောင့်")
            Body("• သင့်အကောင့် လုံခြုံရေးကို သင် တာဝန်ယူရပါမည်\n" +
                    "• Password ကို မ မျှဝေပါနှင့်\n" +
                    "• Account ဖျက်လိုပါက app ထဲမှ ဖျက်နိုင်သည်")

            Section("4. ကြော်ငြာ")
            Body("ဝန်ဆောင်မှုကို အခမဲ့ ထားရှိနိုင်ရန် " +
                    "Google AdMob ကြော်ငြာများ ပြသပါသည်။ " +
                    "ကြော်ငြာ ကြည့်ခြင်းဖြင့် ၂၄ နာရီ သုံးခွင့် ရပါသည်။")

            Section("5. ဝန်ဆောင်မှု ရပ်ဆိုင်းခြင်း")
            Body("ကျွန်ုပ်တို့သည် အကြောင်းပြချက် မရှိဘဲ " +
                    "ဝန်ဆောင်မှုကို ရပ်ဆိုင်းနိုင်သည်။ " +
                    "အလွဲသုံးစားမှု တွေ့ရှိပါက အကောင့်ကို ပိတ်နိုင်သည်။")

            Section("6. တာဝန် ကန့်သတ်ချက်")
            Body("ဝန်ဆောင်မှုကို \"ရှိသည့်အတိုင်း\" ပေးအပ်ပါသည်။ " +
                    "အဆင်မ ပြေမှုများအတွက် ကျွန်ုပ်တို့ တာဝန်မ ယူပါ။")

            Section("7. ဥပဒေ")
            Body("ဤ စည်းကမ်းချက်များကို မြန်မာနိုင်ငံ ဥပဒေဖြင့် " +
                    "အုပ်ချုပ်ပါသည်။")

            Section("8. ဆက်သွယ်ရန်")
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