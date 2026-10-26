package com.piu.vpn.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Account : Screen("account")
    object Settings : Screen("settings")
    object Auth : Screen("auth")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password?token={token}"){
        fun createRoute(token: String) = "reset_password?token=$token"
    }
}