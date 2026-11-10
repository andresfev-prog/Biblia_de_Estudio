package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

// Tipografías maestras empaquetadas en res/font
val AktivGroteskFontFamily = FontFamily(
    Font(R.font.aktiv_grotesk, weight = FontWeight.Normal)
)

val UntitledSerifFontFamily = FontFamily(
    Font(R.font.untitled_serif, weight = FontWeight.Normal)
)

val AppIconsFontFamily = FontFamily(
    Font(R.font.appicons, weight = FontWeight.Normal)
)

// Tipografía base UI que hereda toda la aplicación: Aktiv Grotesk
val AppGlobalFontFamily = AktivGroteskFontFamily

// Proveedor oficial de Google Fonts para Compose (soporte adicional)
val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val GoogleSansFlexFont = GoogleFont("Google Sans Flex")
val GoogleSansFlexFontFamily = FontFamily(
    Font(googleFont = GoogleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Bold)
)

val LexendFont = GoogleFont("Lexend")
val NunitoFont = GoogleFont("Nunito")

val LexendFontFamily = FontFamily(
    Font(googleFont = LexendFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = LexendFont, fontProvider = googleFontProvider, weight = FontWeight.Bold)
)
val NunitoFontFamily = FontFamily(
    Font(googleFont = NunitoFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = NunitoFont, fontProvider = googleFontProvider, weight = FontWeight.Bold)
)

fun resolveBibleFontFamily(fontName: String): FontFamily {
    return when (fontName.trim().lowercase()) {
        "untitled serif", "untitled", "serif" -> UntitledSerifFontFamily
        "aktiv grotesk", "aktiv", "grotesk" -> AktivGroteskFontFamily
        "lexend" -> LexendFontFamily
        "nunito" -> NunitoFontFamily
        "google sans" -> GoogleSansFlexFontFamily
        else -> UntitledSerifFontFamily
    }
}

// Tipografía base que hereda toda la aplicación
val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = AppGlobalFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

val Typography = AppTypography
