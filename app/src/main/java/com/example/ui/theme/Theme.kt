package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object AppThemeIds {
    const val CRISP_LIGHT = "CRISP_LIGHT"
    const val WARM_PARCHMENT = "WARM_PARCHMENT"
    const val SERENE_OLIVE = "SERENE_OLIVE"
    const val MIDNIGHT_BLUE = "MIDNIGHT_BLUE"
    const val DEEP_DARK = "DEEP_DARK"
    const val OBSIDIAN_ONYX = "OBSIDIAN_ONYX"

    val lightThemes = listOf(CRISP_LIGHT, WARM_PARCHMENT, SERENE_OLIVE)
    val darkThemes = listOf(MIDNIGHT_BLUE, DEEP_DARK, OBSIDIAN_ONYX)
}

data class AppColors(
    val themeId: String,
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val titleColor: Color,
    val accent: Color,
    val onAccent: Color,
    val verseNumberColor: Color,
    val pillBg: Color,
    val pillText: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val border: Color,
    val divider: Color,
    val surfaceVariant: Color,
    val comBadgeBg: Color,
    val comBadgeBorder: Color,
    val comBadgeText: Color,
    val refBadgeBg: Color,
    val refBadgeBorder: Color,
    val refBadgeText: Color,
    val isDark: Boolean
)

val CrispLightAppColors = AppColors(
    themeId = AppThemeIds.CRISP_LIGHT,
    background = PureWhite,
    surface = LightCardSurface,
    cardBg = LightCardSurface,
    textPrimary = PureBlack,
    textSecondary = TextLightSecondary,
    titleColor = DeepNavy,
    accent = DeepNavy,
    onAccent = PureWhite,
    verseNumberColor = DeepNavy,
    pillBg = DeepNavy,
    pillText = PureWhite,
    primaryContainer = CrispPrimaryContainer,
    onPrimaryContainer = DeepNavy,
    border = LightBorder,
    divider = LightDivider,
    surfaceVariant = LightSurfaceVariant,
    comBadgeBg = Color(0xFFE0F2FE),
    comBadgeBorder = Color(0xFF0284C7),
    comBadgeText = Color(0xFF0369A1),
    refBadgeBg = Color(0xFFD1FAE5),
    refBadgeBorder = Color(0xFF059669),
    refBadgeText = Color(0xFF047857),
    isDark = false
)

val WarmParchmentAppColors = AppColors(
    themeId = AppThemeIds.WARM_PARCHMENT,
    background = WarmParchmentBg,
    surface = WarmParchmentSurface,
    cardBg = WarmParchmentSurface,
    textPrimary = WarmParchmentTextPrimary,
    textSecondary = WarmParchmentTextSecondary,
    titleColor = WarmParchmentAccent,
    accent = WarmParchmentAccent,
    onAccent = WarmParchmentOnAccent,
    verseNumberColor = WarmParchmentAccent,
    pillBg = WarmParchmentAccent,
    pillText = WarmParchmentOnAccent,
    primaryContainer = WarmParchmentPrimaryContainer,
    onPrimaryContainer = WarmParchmentAccent,
    border = WarmParchmentBorder,
    divider = WarmParchmentDivider,
    surfaceVariant = WarmParchmentSurfaceVariant,
    comBadgeBg = Color(0xFFE4EFF6),
    comBadgeBorder = Color(0xFF2A6B8F),
    comBadgeText = Color(0xFF1E5270),
    refBadgeBg = Color(0xFFE1EFE7),
    refBadgeBorder = Color(0xFF2D6E4E),
    refBadgeText = Color(0xFF1F543A),
    isDark = false
)

val SereneOliveAppColors = AppColors(
    themeId = AppThemeIds.SERENE_OLIVE,
    background = SereneOliveBg,
    surface = SereneOliveSurface,
    cardBg = SereneOliveSurface,
    textPrimary = SereneOliveTextPrimary,
    textSecondary = SereneOliveTextSecondary,
    titleColor = SereneOliveAccent,
    accent = SereneOliveAccent,
    onAccent = SereneOliveOnAccent,
    verseNumberColor = SereneOliveAccent,
    pillBg = SereneOliveAccent,
    pillText = SereneOliveOnAccent,
    primaryContainer = SereneOlivePrimaryContainer,
    onPrimaryContainer = SereneOliveAccent,
    border = SereneOliveBorder,
    divider = SereneOliveDivider,
    surfaceVariant = SereneOliveSurfaceVariant,
    comBadgeBg = Color(0xFFE1EEF5),
    comBadgeBorder = Color(0xFF2B6D8E),
    comBadgeText = Color(0xFF1D5573),
    refBadgeBg = Color(0xFFDCF0E5),
    refBadgeBorder = Color(0xFF2B6E4B),
    refBadgeText = Color(0xFF1F5338),
    isDark = false
)

val MidnightBlueAppColors = AppColors(
    themeId = AppThemeIds.MIDNIGHT_BLUE,
    background = DarkMidnightBg,
    surface = DarkSlateSurface,
    cardBg = DarkSlateSurface,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    titleColor = DarkTextPrimary,
    accent = DarkCobaltAccent,
    onAccent = DarkMidnightOnAccent,
    verseNumberColor = DarkTextSecondary,
    pillBg = DarkCobaltAccent,
    pillText = DarkMidnightOnAccent,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkCobaltAccent,
    border = DarkBorder,
    divider = DarkDivider,
    surfaceVariant = DarkSurfaceVariant,
    comBadgeBg = Color(0xFF0284C7).copy(alpha = 0.22f),
    comBadgeBorder = Color(0xFF38BDF8),
    comBadgeText = Color(0xFF38BDF8),
    refBadgeBg = Color(0xFF047857).copy(alpha = 0.22f),
    refBadgeBorder = Color(0xFF34D399),
    refBadgeText = Color(0xFF34D399),
    isDark = true
)

val DeepDarkAppColors = AppColors(
    themeId = AppThemeIds.DEEP_DARK,
    background = DeepDarkBg,
    surface = DeepDarkSurface,
    cardBg = DeepDarkSurface,
    textPrimary = DeepDarkTextPrimary,
    textSecondary = DeepDarkTextSecondary,
    titleColor = DeepDarkTextPrimary,
    accent = DeepDarkAccent,
    onAccent = DeepDarkOnAccent,
    verseNumberColor = DeepDarkTextSecondary,
    pillBg = DeepDarkAccent,
    pillText = DeepDarkOnAccent,
    primaryContainer = DeepDarkPrimaryContainer,
    onPrimaryContainer = DeepDarkAccent,
    border = DeepDarkBorder,
    divider = DeepDarkDivider,
    surfaceVariant = DeepDarkSurfaceVariant,
    comBadgeBg = Color(0xFF282723),
    comBadgeBorder = Color(0xFF6E6B64),
    comBadgeText = Color(0xFFD6D2C9),
    refBadgeBg = Color(0xFF22211E),
    refBadgeBorder = Color(0xFF585650),
    refBadgeText = Color(0xFFB9B5AC),
    isDark = true
)

val ObsidianOnyxAppColors = AppColors(
    themeId = AppThemeIds.OBSIDIAN_ONYX,
    background = ObsidianOnyxBg,
    surface = ObsidianOnyxSurface,
    cardBg = ObsidianOnyxSurface,
    textPrimary = ObsidianOnyxTextPrimary,
    textSecondary = ObsidianOnyxTextSecondary,
    titleColor = ObsidianOnyxTextPrimary,
    accent = ObsidianOnyxAccent,
    onAccent = ObsidianOnyxOnAccent,
    verseNumberColor = ObsidianOnyxTextSecondary,
    pillBg = ObsidianOnyxAccent,
    pillText = ObsidianOnyxOnAccent,
    primaryContainer = ObsidianOnyxPrimaryContainer,
    onPrimaryContainer = ObsidianOnyxAccent,
    border = ObsidianOnyxBorder,
    divider = ObsidianOnyxDivider,
    surfaceVariant = ObsidianOnyxSurfaceVariant,
    comBadgeBg = Color(0xFF0284C7).copy(alpha = 0.20f),
    comBadgeBorder = Color(0xFF38BDF8),
    comBadgeText = Color(0xFF38BDF8),
    refBadgeBg = Color(0xFF047857).copy(alpha = 0.20f),
    refBadgeBorder = Color(0xFF34D399),
    refBadgeText = Color(0xFF34D399),
    isDark = true
)

val LightAppColors = CrispLightAppColors
val DarkAppColors = MidnightBlueAppColors

val LocalAppColors = staticCompositionLocalOf { CrispLightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

private val CrispLightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = PureWhite,
    primaryContainer = CrispPrimaryContainer,
    onPrimaryContainer = DeepNavy,
    secondary = DeepNavy,
    onSecondary = PureWhite,
    background = PureWhite,
    onBackground = PureBlack,
    surface = LightCardSurface,
    onSurface = PureBlack,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextLightSecondary,
    outline = LightBorder,
    outlineVariant = LightDivider
)

private val WarmParchmentColorScheme = lightColorScheme(
    primary = WarmParchmentAccent,
    onPrimary = WarmParchmentOnAccent,
    primaryContainer = WarmParchmentPrimaryContainer,
    onPrimaryContainer = WarmParchmentAccent,
    secondary = WarmParchmentAccent,
    onSecondary = WarmParchmentOnAccent,
    background = WarmParchmentBg,
    onBackground = WarmParchmentTextPrimary,
    surface = WarmParchmentSurface,
    onSurface = WarmParchmentTextPrimary,
    surfaceVariant = WarmParchmentSurfaceVariant,
    onSurfaceVariant = WarmParchmentTextSecondary,
    outline = WarmParchmentBorder,
    outlineVariant = WarmParchmentBorder
)

private val SereneOliveColorScheme = lightColorScheme(
    primary = SereneOliveAccent,
    onPrimary = SereneOliveOnAccent,
    primaryContainer = SereneOlivePrimaryContainer,
    onPrimaryContainer = SereneOliveAccent,
    secondary = SereneOliveAccent,
    onSecondary = SereneOliveOnAccent,
    background = SereneOliveBg,
    onBackground = SereneOliveTextPrimary,
    surface = SereneOliveSurface,
    onSurface = SereneOliveTextPrimary,
    surfaceVariant = SereneOliveSurfaceVariant,
    onSurfaceVariant = SereneOliveTextSecondary,
    outline = SereneOliveBorder,
    outlineVariant = SereneOliveBorder
)

private val MidnightBlueColorScheme = darkColorScheme(
    primary = DarkCobaltAccent,
    onPrimary = DarkMidnightOnAccent,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkCobaltAccent,
    secondary = DarkCobaltAccent,
    onSecondary = DarkMidnightOnAccent,
    background = DarkMidnightBg,
    onBackground = DarkTextPrimary,
    surface = DarkSlateSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder
)

private val DeepDarkColorScheme = darkColorScheme(
    primary = DeepDarkAccent,
    onPrimary = DeepDarkOnAccent,
    primaryContainer = DeepDarkPrimaryContainer,
    onPrimaryContainer = DeepDarkAccent,
    secondary = DeepDarkAccent,
    onSecondary = DeepDarkOnAccent,
    background = DeepDarkBg,
    onBackground = DeepDarkTextPrimary,
    surface = DeepDarkSurface,
    onSurface = DeepDarkTextPrimary,
    surfaceVariant = DeepDarkSurfaceVariant,
    onSurfaceVariant = DeepDarkTextSecondary,
    outline = DeepDarkBorder,
    outlineVariant = DeepDarkBorder
)

private val ObsidianOnyxColorScheme = darkColorScheme(
    primary = ObsidianOnyxAccent,
    onPrimary = ObsidianOnyxOnAccent,
    primaryContainer = ObsidianOnyxPrimaryContainer,
    onPrimaryContainer = ObsidianOnyxAccent,
    secondary = ObsidianOnyxAccent,
    onSecondary = ObsidianOnyxOnAccent,
    background = ObsidianOnyxBg,
    onBackground = ObsidianOnyxTextPrimary,
    surface = ObsidianOnyxSurface,
    onSurface = ObsidianOnyxTextPrimary,
    surfaceVariant = ObsidianOnyxSurfaceVariant,
    onSurfaceVariant = ObsidianOnyxTextSecondary,
    outline = ObsidianOnyxBorder,
    outlineVariant = ObsidianOnyxBorder
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun isDarkThemeEffective(
    themeMode: String,
    systemInDarkTheme: Boolean
): Boolean = when (themeMode.trim().uppercase()) {
    "LIGHT" -> false
    "DARK" -> true
    else -> systemInDarkTheme // Soporta "AUTO" y valor previo "AUTOMATIC"
}

fun resolveAppThemeId(
    themeMode: String = "AUTO",
    selectedLightTheme: String = AppThemeIds.CRISP_LIGHT,
    selectedDarkTheme: String = AppThemeIds.MIDNIGHT_BLUE,
    systemInDarkTheme: Boolean = false
): String {
    val useDark = isDarkThemeEffective(themeMode, systemInDarkTheme)
    return if (useDark) {
        when (selectedDarkTheme) {
            AppThemeIds.DEEP_DARK,
            AppThemeIds.OBSIDIAN_ONYX,
            AppThemeIds.MIDNIGHT_BLUE -> selectedDarkTheme
            else -> AppThemeIds.MIDNIGHT_BLUE
        }
    } else {
        when (selectedLightTheme) {
            AppThemeIds.WARM_PARCHMENT,
            AppThemeIds.SERENE_OLIVE,
            AppThemeIds.CRISP_LIGHT -> selectedLightTheme
            else -> AppThemeIds.CRISP_LIGHT
        }
    }
}

fun resolveAppColors(
    themeMode: String = "AUTO",
    selectedLightTheme: String = AppThemeIds.CRISP_LIGHT,
    selectedDarkTheme: String = AppThemeIds.MIDNIGHT_BLUE,
    systemInDarkTheme: Boolean = false
): AppColors = when (
    resolveAppThemeId(
        themeMode = themeMode,
        selectedLightTheme = selectedLightTheme,
        selectedDarkTheme = selectedDarkTheme,
        systemInDarkTheme = systemInDarkTheme
    )
) {
    AppThemeIds.WARM_PARCHMENT -> WarmParchmentAppColors
    AppThemeIds.SERENE_OLIVE -> SereneOliveAppColors
    AppThemeIds.MIDNIGHT_BLUE -> MidnightBlueAppColors
    AppThemeIds.DEEP_DARK -> DeepDarkAppColors
    AppThemeIds.OBSIDIAN_ONYX -> ObsidianOnyxAppColors
    else -> CrispLightAppColors
}

@Composable
fun BibleStudyTheme(
    themeMode: String = "AUTO",
    selectedLightTheme: String = AppThemeIds.CRISP_LIGHT,
    selectedDarkTheme: String = AppThemeIds.MIDNIGHT_BLUE,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val resolvedThemeId = resolveAppThemeId(
        themeMode = themeMode,
        selectedLightTheme = selectedLightTheme,
        selectedDarkTheme = selectedDarkTheme,
        systemInDarkTheme = systemInDark
    )

    val appColors = when (resolvedThemeId) {
        AppThemeIds.WARM_PARCHMENT -> WarmParchmentAppColors
        AppThemeIds.SERENE_OLIVE -> SereneOliveAppColors
        AppThemeIds.MIDNIGHT_BLUE -> MidnightBlueAppColors
        AppThemeIds.DEEP_DARK -> DeepDarkAppColors
        AppThemeIds.OBSIDIAN_ONYX -> ObsidianOnyxAppColors
        else -> CrispLightAppColors
    }

    val colorScheme = when (resolvedThemeId) {
        AppThemeIds.WARM_PARCHMENT -> WarmParchmentColorScheme
        AppThemeIds.SERENE_OLIVE -> SereneOliveColorScheme
        AppThemeIds.MIDNIGHT_BLUE -> MidnightBlueColorScheme
        AppThemeIds.DEEP_DARK -> DeepDarkColorScheme
        AppThemeIds.OBSIDIAN_ONYX -> ObsidianOnyxColorScheme
        else -> CrispLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !appColors.isDark
                insetsController.isAppearanceLightNavigationBars = !appColors.isDark
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
