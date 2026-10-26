package com.piu.vpn.ui.state

import com.piu.vpn.data.Entitlement
import com.piu.vpn.data.User
import com.piu.vpn.data.VpnStats

// VPN (Home)
sealed class VpnStatus {
    object NoEntitlement : VpnStatus()
    object Ready : VpnStatus()
    object Connecting : VpnStatus()
    data class Connected(val stats: VpnStats) : VpnStatus()
    data class Error(val message: String) : VpnStatus()
}

data class VpnUiState(
    val status: VpnStatus = VpnStatus.NoEntitlement,
    val entitlement: Entitlement? = null,
    val adRequested: Boolean = false,
    val isGuest: Boolean = true,
    val installId: String = "",
    val user: User? = null,
    val adError: String? = null,
)

// Auth
data class AuthUiState(
    val isRegisterMode: Boolean = false,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

// Forgot Password
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val sent: Boolean = false,
)

// Reset Password
data class ResetPasswordUiState(
    val token: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

// Account
data class AccountUiState(
    val user: User? = null,
    val isGuest: Boolean = true,
    val installId: String = "",
    val entitlement: Entitlement? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val logoutSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
)

// Settings
data class SettingsUiState(
    val language: String = "my",
    val theme: String = "system",
    val appVersion: String = "1.0.0",
)
