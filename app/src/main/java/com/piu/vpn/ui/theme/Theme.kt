package com.piu.vpn.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = VpnBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = VpnBlueLight,
    onPrimaryContainer = VpnBlueDark,

    secondary = VpnGreen,
    onSecondary = androidx.compose.ui.graphics.Color.White,

    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,

    error = VpnRed,
    onError = androidx.compose.ui.graphics.Color.White,
)

private val DarkColors = darkColorScheme(
    primary = VpnBlueLight,
    onPrimary = VpnBlueDark,
    primaryContainer = VpnBlueDark,
    onPrimaryContainer = VpnBlueLight,

    secondary = VpnGreen,
    onSecondary = androidx.compose.ui.graphics.Color.Black,

    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,

    error = VpnRed,
    onError = androidx.compose.ui.graphics.Color.White,
)

@Composable
fun MyVPNTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VpnTypography,
        content = content,
    )
}