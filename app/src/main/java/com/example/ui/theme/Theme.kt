package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val titleColor: Color,
    val accent: Color,
    val verseNumberColor: Color,
    val pillBg: Color,
    val pillText: Color,
    val border: Color,
    val divider: Color,
    val surfaceVariant: Color,
    val isDark: Boolean
)

val LightAppColors = AppColors(
    background = PureWhite,
    surface = PureWhite,
    cardBg = PureWhite,
    textPrimary = PureBlack,
    textSecondary = TextLightSecondary,
    titleColor = DeepNavy,
    accent = DeepNavy,
    verseNumberColor = DeepNavy,
    pillBg = DeepNavy,
    pillText = PureWhite,
    border = LightBorder,
    divider = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFFF1F5F9),
    isDark = false
)

val DarkAppColors = AppColors(
    background = DarkMidnightBg,
    surface = DarkSlateSurface,
    cardBg = DarkSlateSurface,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    titleColor = DarkTextPrimary,
    accent = DarkCobaltAccent,
    verseNumberColor = DarkTextSecondary,
    pillBg = DarkCobaltAccent,
    pillText = DarkMidnightBg,
    border = DarkBorder,
    divider = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF162032),
    isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkCobaltAccent,
    onPrimary = DarkMidnightBg,
    primaryContainer = Color(0xFF0F2744),
    onPrimaryContainer = DarkCobaltAccent,
    secondary = DarkCobaltAccent,
    onSecondary = DarkMidnightBg,
    background = DarkMidnightBg,
    onBackground = DarkTextPrimary,
    surface = DarkSlateSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF162032),
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFE8EEF5),
    onPrimaryContainer = DeepNavy,
    secondary = DeepNavy,
    onSecondary = PureWhite,
    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = TextLightSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFF1F5F9)
)

@Composable
fun BibleStudyTheme(
    themeMode: String = "AUTOMATIC",
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemInDark
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val appColors = if (isDark) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
