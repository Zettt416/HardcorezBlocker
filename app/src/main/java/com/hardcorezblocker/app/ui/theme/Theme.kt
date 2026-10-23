package com.hardcorezblocker.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.hardcorezblocker.app.data.model.AppLanguage
import com.hardcorezblocker.app.data.model.AppThemeMode
import com.hardcorezblocker.app.ui.i18n.LocalAppStrings
import com.hardcorezblocker.app.ui.i18n.getAppStrings

@Composable
fun HardcorezBlockerTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    language: AppLanguage = AppLanguage.INDONESIAN,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val appColors = if (isDark) DarkAppColors else LightAppColors

    val materialColors = if (isDark) {
        darkColorScheme(
            primary = PrimaryBlue,
            onPrimary = Color.White,
            primaryContainer = appColors.surfaceVariant,
            onPrimaryContainer = appColors.textPrimary,
            secondary = AccentIndigo,
            onSecondary = Color.White,
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceVariant,
            onSurfaceVariant = appColors.textSecondary,
            error = DangerRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = PrimaryBlue,
            onPrimary = Color.White,
            primaryContainer = appColors.surfaceVariant,
            onPrimaryContainer = appColors.textPrimary,
            secondary = AccentIndigo,
            onSecondary = Color.White,
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceVariant,
            onSurfaceVariant = appColors.textSecondary,
            error = DangerRed,
            onError = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = appColors.background.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = appColors.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val strings = getAppStrings(language)
    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAppStrings provides strings
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
