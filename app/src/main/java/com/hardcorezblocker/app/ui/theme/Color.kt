package com.hardcorezblocker.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean
)

val LightAppColors = AppColors(
    background = Color(0xFFF1F5F9),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EEF6),
    border = Color(0xFFCBD5E1),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF334155),
    textMuted = Color(0xFF64748B),
    isDark = false
)

val DarkAppColors = AppColors(
    background = Color(0xFF090D16),
    surface = Color(0xFF111827),
    surfaceElevated = Color(0xFF172033),
    surfaceVariant = Color(0xFF1E293B),
    border = Color(0xFF283548),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF64748B),
    isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

val DarkBackground: Color
    @Composable get() = LocalAppColors.current.background

val DarkSurface: Color
    @Composable get() = LocalAppColors.current.surface

val DarkSurfaceElevated: Color
    @Composable get() = LocalAppColors.current.surfaceElevated

val DarkSurfaceVariant: Color
    @Composable get() = LocalAppColors.current.surfaceVariant

val DarkBorder: Color
    @Composable get() = LocalAppColors.current.border

val TextPrimary: Color
    @Composable get() = LocalAppColors.current.textPrimary

val TextSecondary: Color
    @Composable get() = LocalAppColors.current.textSecondary

val TextMuted: Color
    @Composable get() = LocalAppColors.current.textMuted

val PrimaryBlue = Color(0xFF0284C7)
val CyberBlue = Color(0xFF0369A1)
val AccentIndigo = Color(0xFF6366F1)
val ShieldActive = Color(0xFF059669)
val ShieldInactive = Color(0xFF94A3B8)
val DangerRed = Color(0xFFDC2626)
val WarningAmber = Color(0xFFD97706)
val PromoPurple = Color(0xFF7C3AED)
