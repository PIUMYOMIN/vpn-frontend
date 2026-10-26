package com.piu.vpn.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.navArgument
import com.piu.vpn.ui.screens.AccountScreen
import com.piu.vpn.ui.screens.AuthScreen
import com.piu.vpn.ui.screens.ForgotPasswordScreen
import com.piu.vpn.ui.screens.HomeScreen
import com.piu.vpn.ui.screens.PrivacyPolicyScreen
import com.piu.vpn.ui.screens.ResetPasswordScreen
import com.piu.vpn.ui.screens.SettingsScreen
import com.piu.vpn.ui.screens.TermsOfServiceScreen

@Composable
fun VpnNavHost(
    initialResetToken: String? = null,
    onResetTokenConsumed: ()->Unit = {},
    ){
    val navController = rememberNavController()

    //Deep Link ကို သုံး
    LaunchedEffect(initialResetToken){
        if (!initialResetToken.isNullOrBlank()){
            navController.navigate(Screen.ResetPassword.createRoute(initialResetToken))
            onResetTokenConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAccount = { navController.navigate(Screen.Account.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.Account.route) {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAuth = { navController.navigate(Screen.Auth.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToTerms = { navController.navigate(Screen.TermsOfService.route) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToTerms = { navController.navigate(Screen.TermsOfService.route) },
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    // Account screen ကို ပြန် (Auth screen ကို pop)
                    navController.popBackStack()
                },
                onPrivacyClick = { navController.navigate(Screen.PrivacyPolicy.route) },
                onTermsClick = { navController.navigate(Screen.TermsOfService.route) },
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
            )
        }

        // ============= Forgot Password =============
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onEnterTokenManually = {
                    navController.navigate(Screen.ResetPassword.createRoute(""))
                },
            )
        }

        // ============= Reset Password =============
        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(
                navArgument("token") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
            ),
        ) { backStack ->
            val token = backStack.arguments?.getString("token") ?: ""
            ResetPasswordScreen(
                token = token,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    // Success → login screen သို့ ပြန်
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.TermsOfService.route) {
            TermsOfServiceScreen(onBack = { navController.popBackStack() })
        }
    }
}