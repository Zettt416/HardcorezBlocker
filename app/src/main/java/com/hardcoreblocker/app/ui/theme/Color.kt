package com.hardcoreblocker.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Model warna dinamis yang beradaptasi dengan Light Mode dan Dark Mode.
 */
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

// --- Light Color Palette (Bersih, Cerah, Elegan - Google Messages / Nothing OS style) ---
val LightAppColors = AppColors(
    background = Color(0xFFF6F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    border = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    isDark = false
)

// --- Dark Color Palette (Deep OLED Slate - Kontras Tinggi, Nyaman di Malam Hari) ---
val DarkAppColors = AppColors(
    background = Color(0xFF0B0F17),
    surface = Color(0xFF131A26),
    surfaceElevated = Color(0xFF1B2433),
    surfaceVariant = Color(0xFF243044),
    border = Color(0xFF2A364F),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

// --- Dynamic Color Accessors (Kompatibel dengan semua komponen yang sudah ada) ---
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

// --- Aksen Semantik Konsisten (Aman & Kontras Tinggi di Light maupun Dark Mode) ---
val PrimaryBlue = Color(0xFF0284C7)
val CyberBlue = Color(0xFF0369A1)
val AccentIndigo = Color(0xFF6366F1)
val ShieldActive = Color(0xFF059669)
val ShieldInactive = Color(0xFF94A3B8)
val DangerRed = Color(0xFFDC2626)
val WarningAmber = Color(0xFFD97706)
val PromoPurple = Color(0xFF7C3AED)
